package com.github.aivanovski.testwithme.android.presentation.screens.root.model

import androidx.compose.runtime.Immutable

@Immutable
data class RootState(
    val title: String,
    val isBackVisible: Boolean
)