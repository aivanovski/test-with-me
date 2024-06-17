package com.github.aivanovski.testwithme.android.presentation.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
inline fun <T> rememberCallback(
    crossinline block: (T) -> Unit
): (T) -> Unit {
    return remember { { value -> block.invoke(value) } }
}