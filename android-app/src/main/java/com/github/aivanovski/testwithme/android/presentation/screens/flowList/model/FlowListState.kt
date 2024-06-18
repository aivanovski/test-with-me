package com.github.aivanovski.testwithme.android.presentation.screens.flowList.model

sealed interface FlowListState {

    object NotInitialized : FlowListState

    object Loading : FlowListState

    data class Data(
        val items: List<FlowItem>
    ) : FlowListState

    data class Error(
        val message: String
    ) : FlowListState
}