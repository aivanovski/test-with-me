package com.github.aivanovski.testwithme.android.presentation.screens.flow.model

import androidx.compose.runtime.Immutable
import com.github.aivanovski.testwithme.android.entity.ErrorMessage
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellViewModel
import com.github.aivanovski.testwithme.android.presentation.core.compose.dialogs.model.MessageDialogState

@Immutable
sealed interface FlowState {

    object NotInitialized : FlowState

    object Loading : FlowState

    @Immutable
    data class Data(
        val viewModels: List<BaseCellViewModel>,
        val errorDialogMessage: ErrorMessage?,
        val flowDialogState: MessageDialogState?,
        val isLaunchServices: Boolean
    ) : FlowState
}