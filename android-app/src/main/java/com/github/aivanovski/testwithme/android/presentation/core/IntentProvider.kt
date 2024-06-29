package com.github.aivanovski.testwithme.android.presentation.core

import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellIntent

interface IntentProvider {

    fun subscribe(
        subscriber: Any,
        listener: (intent: BaseCellIntent) -> Unit
    ) {
    }

    fun unsubscribe(subscriber: Any) {}
    fun sendEvent(intent: BaseCellIntent) {}
    fun clear() {}
}