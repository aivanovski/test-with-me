package com.github.aivanovski.testwithme.android.entity

sealed class OnStepFinishedAction {
    data class Next(val nextStepUid: String) : OnStepFinishedAction()
    object Complete : OnStepFinishedAction()
    object Retry : OnStepFinishedAction()
    object Stop : OnStepFinishedAction()
}