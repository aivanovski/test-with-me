package com.github.aivanovski.testwithme.android.presentation.screens

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {

    @Serializable
    object Login : Screen
}