package com.github.aivanovski.testwithme.android.presentation.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
inline fun <T> rememberCallback(
    crossinline block: (T) -> Unit
): (T) -> Unit {
    return remember { { value -> block.invoke(value) } }
}

@Composable
inline fun rememberOnClickedCallback(
    crossinline block: () -> Unit
): () -> Unit {
    return remember { { block.invoke() } }
}

// TODO: lifecycle subscription should be moved to component
@Composable
fun SubscribeLifecycleEffect(
    onStart: () -> Unit,
    onDestroy: () -> Unit
) {
    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    onStart.invoke()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    onDestroy.invoke()
                }

                else -> {}
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}