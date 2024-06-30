package com.github.aivanovski.testwithme.android.presentation.screens.flowList.model

import com.github.aivanovski.testwithme.android.entity.ErrorMessage

sealed interface FlowListState {

    object NotInitialized : FlowListState

    object Loading : FlowListState

    data class Data(
        val items: List<FlowItem>
    ) : FlowListState

    data class Error(
        val message: ErrorMessage
    ) : FlowListState
}