package com.github.aivanovski.testwithme.android.presentation.core.cells.viewModel

import androidx.compose.runtime.Immutable
import com.github.aivanovski.testwithme.android.presentation.core.IntentProvider
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellViewModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.model.HeaderCellIntent
import com.github.aivanovski.testwithme.android.presentation.core.cells.model.HeaderCellModel

@Immutable
class HeaderCellViewModel(
    override val model: HeaderCellModel,
    private val intentProvider: IntentProvider
) : BaseCellViewModel(model) {

    fun sendIntent(intent: HeaderCellIntent) {
        intentProvider.sendEvent(intent)
    }
}