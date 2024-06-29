package com.github.aivanovski.testwithme.android.presentation.screens

import com.github.aivanovski.testwithme.android.presentation.screens.flow.model.FlowScreenArgs
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {

    @Serializable
    object Login : Screen

    @Serializable
    object FlowList : Screen

    @Serializable
    data class Flow(
        val args: FlowScreenArgs
    ) : Screen
}