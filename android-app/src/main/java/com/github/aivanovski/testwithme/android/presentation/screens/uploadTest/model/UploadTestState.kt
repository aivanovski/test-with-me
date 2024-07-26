package com.github.aivanovski.testwithme.android.presentation.screens.uploadTest.model

import androidx.compose.runtime.Immutable
import com.github.aivanovski.testwithme.android.presentation.core.cells.screen.TerminalState
import com.github.aivanovski.testwithme.android.presentation.core.compose.dialogs.model.MessageDialogState
import com.github.aivanovski.testwithme.utils.StringUtils

@Immutable
data class UploadTestState(
    val terminalState: TerminalState? = null,
    val dialogState: MessageDialogState? = null,
    val projects: List<String> = emptyList(),
    val selectedProject: String = StringUtils.EMPTY,
    val groups: List<String> = emptyList(),
    val selectedGroup: String = StringUtils.EMPTY
)