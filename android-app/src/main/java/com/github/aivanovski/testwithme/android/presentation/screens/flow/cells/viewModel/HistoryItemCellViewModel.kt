package com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.viewModel

import androidx.compose.runtime.Immutable
import com.github.aivanovski.testwithme.android.presentation.core.IntentProvider
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.HistoryItemCellIntent
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.HistoryItemCellModel

@Immutable
class HistoryItemCellViewModel(
    override val model: HistoryItemCellModel,
    private val intentProvider: IntentProvider
) : BaseCellViewModel(model) {

    fun sendIntent(intent: HistoryItemCellIntent) {
        intentProvider.sendEvent(intent)
    }
}