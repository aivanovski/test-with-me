package com.github.aivanovski.testwithme.android.presentation.screens.flowList.model

import androidx.compose.runtime.Immutable

@Immutable
data class FlowItem(
    val uid: String,
    val name: String
)