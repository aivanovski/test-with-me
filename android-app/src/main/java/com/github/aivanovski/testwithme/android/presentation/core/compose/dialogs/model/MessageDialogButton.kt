package com.github.aivanovski.testwithme.android.presentation.core.compose.dialogs.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface MessageDialogButton {

    data class ActionButton(
        val title: String,
        val actionId: Int,
    ) : MessageDialogButton
}