package com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model

import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellIntent

sealed interface FlowTitleCellIntent : BaseCellIntent {
    data class OnRunButtonClick(
        val id: String
    ) : FlowTitleCellIntent
}