package com.github.aivanovski.testwithme.android.presentation.screens.flow.cells

import androidx.compose.runtime.Composable
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui.FlowStatsCell
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui.FlowTitleCell
import com.github.aivanovski.testwithme.android.presentation.core.cells.ui.HeaderCell
import com.github.aivanovski.testwithme.android.presentation.core.cells.ui.SpaceCell
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.viewModel.FlowStatsCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.viewModel.FlowTitleCellViewModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.viewModel.HeaderCellViewModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.viewModel.SpaceCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui.EmptyHistoryCell
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui.HistoryItemCell
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.viewModel.EmptyHistoryCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.viewModel.HistoryItemCellViewModel

class UiCellFactory {

    @Composable
    fun createCell(viewModel: BaseCellViewModel) {
        when (viewModel) {
            is FlowTitleCellViewModel -> FlowTitleCell(viewModel)
            is FlowStatsCellViewModel -> FlowStatsCell(viewModel)
            is HeaderCellViewModel -> HeaderCell(viewModel)
            is SpaceCellViewModel -> SpaceCell(viewModel)
            is HistoryItemCellViewModel -> HistoryItemCell(viewModel)
            is EmptyHistoryCellViewModel -> EmptyHistoryCell(viewModel)
            else -> throw IllegalStateException()
        }
    }
}