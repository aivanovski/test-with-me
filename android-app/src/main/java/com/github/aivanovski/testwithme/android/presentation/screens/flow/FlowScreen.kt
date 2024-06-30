package com.github.aivanovski.testwithme.android.presentation.screens.flow

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.github.aivanovski.testwithme.android.presentation.core.cells.ui.newHeaderCellViewModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.ui.newSpaceCellViewModel
import com.github.aivanovski.testwithme.android.presentation.core.compose.ProgressIndicator
import com.github.aivanovski.testwithme.android.presentation.core.compose.ThemedScreenPreview
import com.github.aivanovski.testwithme.android.presentation.core.compose.dialogs.ErrorDialog
import com.github.aivanovski.testwithme.android.presentation.core.compose.dialogs.MessageDialog
import com.github.aivanovski.testwithme.android.presentation.core.compose.dialogs.model.MessageDialogIntent
import com.github.aivanovski.testwithme.android.presentation.core.compose.dialogs.model.MessageDialogState
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.AppTheme
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.ElementMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.GroupMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.HalfMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.LightTheme
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.UiCellFactory
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui.newEmptyHistoryCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui.newFailedHistoryItemCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui.newFlowStatCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui.newSuccessHistoryItemCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui.newTitleCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.model.FlowIntent
import com.github.aivanovski.testwithme.android.presentation.screens.flow.model.FlowState
import timber.log.Timber

@Composable
fun FlowScreen(viewModel: FlowViewModel) {
    val state by viewModel.state.collectAsState()

    FlowScreen(
        state = state,
        onIntent = viewModel::sendIntent
    )
}

@Composable
private fun FlowScreen(
    state: FlowState,
    onIntent: (intent: FlowIntent) -> Unit
) {
    val cellFactory = UiCellFactory()

    Surface(
        color = AppTheme.theme.colors.secondaryBackground
    ) {
        when (state) {
            FlowState.NotInitialized -> {}

            FlowState.Loading -> {
                ProgressIndicator()
            }

            is FlowState.Data -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(state.viewModels) { cellViewModel ->
                        cellFactory.createCell(cellViewModel)
                    }
                }

                if (state.errorDialogMessage != null) {
                    ErrorDialog(
                        message = state.errorDialogMessage,
                        onDismiss = { // TODO: optimize
                            onIntent.invoke(FlowIntent.OnDismissErrorDialog)
                        }
                    )
                }

                if (state.flowDialogState != null) {
                    FlowDialogContent(
                        state = state.flowDialogState,
                        onIntent = onIntent
                    )
                }

                if (state.isLaunchServices) {
                    var isLaunched by remember {
                        mutableStateOf(false)
                    }

                    val context = LocalContext.current
                    LaunchedEffect(isLaunched) {
                        if (!isLaunched) {
                            Timber.d("Launching activity")

                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                            isLaunched = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowDialogContent(
    state: MessageDialogState,
    onIntent: (intent: FlowIntent) -> Unit
) {
    MessageDialog(
        state = state,
        onIntent = { dialogIntent -> // TODO: optimize
            when (dialogIntent) {
                is MessageDialogIntent.OnDismiss -> {
                    onIntent.invoke(FlowIntent.OnDismissFlowDialog)
                }

                is MessageDialogIntent.OnActionButtonClick -> {
                    onIntent.invoke(
                        FlowIntent.OnFlowDialogActionClick(
                            actionId = dialogIntent.actionId
                        )
                    )
                }
            }
        }
    )
}

@Composable
@Preview
fun FlowScreenPreview() {
    ThemedScreenPreview(theme = LightTheme) {
        FlowScreen(
            state = FlowState.Data(
                viewModels = listOf(
                    newSpaceCellViewModel(GroupMargin),
                    newTitleCellViewModel(),
                    newSpaceCellViewModel(ElementMargin),
                    newFlowStatCellViewModel(),
                    newHeaderCellViewModel(),
                    newSuccessHistoryItemCellViewModel(),
                    newSpaceCellViewModel(HalfMargin),
                    newFailedHistoryItemCellViewModel(),
                    newSpaceCellViewModel(HalfMargin),
                    newSuccessHistoryItemCellViewModel(),
                ),
                errorDialogMessage = null,
                flowDialogState = null,
                isLaunchServices = false
            ),
            onIntent = {}
        )
    }
}

@Composable
@Preview
fun FlowScreenWithoutRunsPreview() {
    ThemedScreenPreview(theme = LightTheme) {
        FlowScreen(
            state = FlowState.Data(
                viewModels = listOf(
                    newSpaceCellViewModel(GroupMargin),
                    newTitleCellViewModel(),
                    newEmptyHistoryCellViewModel()
                ),
                errorDialogMessage = null,
                flowDialogState = null,
                isLaunchServices = false
            ),
            onIntent = {}
        )
    }
}