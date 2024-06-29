package com.github.aivanovski.testwithme.android.presentation.core

import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellIntent
import java.util.concurrent.ConcurrentHashMap

typealias Listener = (intent: BaseCellIntent) -> Unit

class IntentProviderImpl : IntentProvider {

    private val listenerBySubscriberType: MutableMap<String, Listener> = ConcurrentHashMap()

    override fun subscribe(
        subscriber: Any,
        listener: (intent: BaseCellIntent) -> Unit
    ) {
        val subscriberType = subscriber::class.java.name
        listenerBySubscriberType[subscriberType] = listener
    }

    override fun unsubscribe(subscriber: Any) {
        val subscriberType = subscriber::class.java.name
        listenerBySubscriberType.remove(subscriberType)
    }

    override fun sendEvent(intent: BaseCellIntent) {
        for (listener in listenerBySubscriberType.values) {
            listener.invoke(intent)
        }
    }

    override fun clear() {
        listenerBySubscriberType.clear()
    }
}