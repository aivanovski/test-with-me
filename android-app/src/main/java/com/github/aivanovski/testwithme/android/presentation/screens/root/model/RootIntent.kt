package com.github.aivanovski.testwithme.android.presentation.screens.root.model

sealed interface RootIntent {

    object NavigateBack : RootIntent

    data class SetTopBarState(
        val state: TopBarState
    ) : RootIntent
}