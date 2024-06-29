package com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.viewModel

import androidx.compose.runtime.Immutable
import com.github.aivanovski.testwithme.android.presentation.core.IntentProvider
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.FlowTitleCellIntent
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.FlowTitleCellModel

@Immutable
class FlowTitleCellViewModel(
    override val model: FlowTitleCellModel,
    private val intentProvider: IntentProvider
) : BaseCellViewModel(model) {

    fun sendIntent(intent: FlowTitleCellIntent) {
        intentProvider.sendEvent(intent)
    }
}