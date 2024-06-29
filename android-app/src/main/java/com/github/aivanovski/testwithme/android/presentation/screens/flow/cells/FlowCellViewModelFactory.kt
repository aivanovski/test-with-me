package com.github.aivanovski.testwithme.android.presentation.screens.flow.cells

import com.github.aivanovski.testwithme.android.presentation.core.IntentProvider
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellViewModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.CellViewModelFactory
import com.github.aivanovski.testwithme.android.presentation.core.cells.model.HeaderCellModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.model.SpaceCellModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.viewModel.HeaderCellViewModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.viewModel.SpaceCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.FlowStatsCellModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.FlowTitleCellModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.HistoryItemCellModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.viewModel.FlowStatsCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.viewModel.FlowTitleCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.viewModel.HistoryItemCellViewModel

class FlowCellViewModelFactory : CellViewModelFactory {

    override fun createCellViewModel(
        model: BaseCellModel,
        intentProvider: IntentProvider
    ): BaseCellViewModel {
        return when (model) {
            is FlowTitleCellModel -> FlowTitleCellViewModel(model, intentProvider)
            is FlowStatsCellModel -> FlowStatsCellViewModel(model)
            is HeaderCellModel -> HeaderCellViewModel(model, intentProvider)
            is SpaceCellModel -> SpaceCellViewModel(model)
            is HistoryItemCellModel -> HistoryItemCellViewModel(model, intentProvider)
            else -> throwUnsupportedModelException(model)
        }
    }
}