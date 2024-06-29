package com.github.aivanovski.testwithme.android.presentation.screens.flow.model

import kotlinx.serialization.Serializable

@Serializable
data class FlowScreenArgs(
    val flowUid: String,
    val screenTitle: String
)