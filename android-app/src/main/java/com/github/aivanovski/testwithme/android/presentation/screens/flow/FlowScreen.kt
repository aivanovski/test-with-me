package com.github.aivanovski.testwithme.android.presentation.screens.flow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.aivanovski.testwithme.android.presentation.core.compose.ProgressIndicator
import com.github.aivanovski.testwithme.android.presentation.core.compose.ThemedScreenPreview
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.LightTheme
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.UiCellFactory
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui.newFlowStatCellViewModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.ui.newHeaderCellViewModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.ui.newSpaceCellViewModel
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.ElementMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.GroupMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.HalfMargin
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui.newFailedHistoryItemCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui.newSuccessHistoryItemCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui.newTitleCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.model.FlowState

@Composable
fun FlowScreen(viewModel: FlowViewModel) {
    val state by viewModel.state.collectAsState()

    FlowScreen(
        state = state
    )
}

@Composable
private fun FlowScreen(
    state: FlowState
) {
    val cellFactory = UiCellFactory()

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
        }
    }
}

@Composable
@Preview
fun FlowScreenLightPreview() {
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
                )
            )
        )
    }
}