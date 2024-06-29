package com.github.aivanovski.testwithme.android.presentation.screens.flow.model

import androidx.compose.runtime.Immutable
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellViewModel

@Immutable
sealed interface FlowState {

    object NotInitialized : FlowState

    object Loading : FlowState

    @Immutable
    data class Data(
        val viewModels: List<BaseCellViewModel>
    ) : FlowState
}