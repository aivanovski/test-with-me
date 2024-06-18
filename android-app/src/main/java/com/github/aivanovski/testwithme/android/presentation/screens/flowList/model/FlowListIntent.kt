package com.github.aivanovski.testwithme.android.presentation.screens.flowList.model

sealed interface FlowListIntent {

    object Initialize : FlowListIntent

    data class OnFlowClicked(
        val uid: String
    ) : FlowListIntent
}