package com.github.aivanovski.testwithme.android.presentation.screens.flowList

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.aivanovski.testwithme.android.presentation.core.compose.CenteredBox
import com.github.aivanovski.testwithme.android.presentation.core.compose.ErrorMessage
import com.github.aivanovski.testwithme.android.presentation.core.compose.ProgressIndicator
import com.github.aivanovski.testwithme.android.presentation.core.compose.ThemedScreenPreview
import com.github.aivanovski.testwithme.android.presentation.core.compose.dialogs.newErrorMessage
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.LightTheme
import com.github.aivanovski.testwithme.android.presentation.screens.flowList.model.FlowItem
import com.github.aivanovski.testwithme.android.presentation.screens.flowList.model.FlowListIntent
import com.github.aivanovski.testwithme.android.presentation.screens.flowList.model.FlowListState

@Composable
fun FlowListScreen(viewModel: FlowListViewModel) {
    val state by viewModel.state.collectAsState()

    FlowListScreen(
        state = state,
        onIntent = viewModel::sendIntent
    )
}

@Composable
private fun FlowListScreen(
    state: FlowListState,
    onIntent: (intent: FlowListIntent) -> Unit
) {
    when (state) {
        FlowListState.NotInitialized -> {}

        FlowListState.Loading -> {
            ProgressIndicator()
        }

        is FlowListState.Error -> {
            CenteredBox {
                ErrorMessage(
                    message = state.message
                )
            }
        }

        is FlowListState.Data -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(state.items) { item ->
                    FlowItemCell(
                        item = item,
                        onIntent = onIntent
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ErrorLightPreview() {
    ThemedScreenPreview(theme = LightTheme) {
        FlowListScreen(
            state = newErrorState(),
            onIntent = {}
        )

    }
}

@Preview
@Composable
fun DataLightPreview() {
    ThemedScreenPreview(theme = LightTheme) {
        FlowListScreen(
            state = newDataState(),
            onIntent = {}
        )
    }
}

private fun newDataState(): FlowListState =
    FlowListState.Data(
        items = listOf(
            FlowItem(
                uid = "uid1",
                name = "unlock-database.yaml"
            ),
            FlowItem(
                uid = "uid2",
                name = "create-database.yaml"
            )
        )
    )

@Composable
private fun newErrorState(): FlowListState {
    return FlowListState.Error(
        message = newErrorMessage()
    )
}

