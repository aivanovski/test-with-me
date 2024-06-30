package com.github.aivanovski.testwithme.android.presentation.screens.flow.model

sealed interface FlowIntent {

    object Initialize : FlowIntent

    object OnDismissErrorDialog : FlowIntent

    object OnDismissFlowDialog : FlowIntent

    data class OnFlowDialogActionClick(
        val actionId: Int
    ) : FlowIntent

    data class RunFlow(
        val flowUid: String
    ) : FlowIntent
}