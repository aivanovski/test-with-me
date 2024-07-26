package com.github.aivanovski.testwithme.android.presentation.core.navigation

import com.arkivanov.decompose.router.stack.items
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.github.aivanovski.testwithme.android.presentation.screens.Screen
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootScreenComponent
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class RouterImpl(
    private val rootComponent: RootScreenComponent,
    private val onExitNavigation: () -> Unit
) : Router {

    private val resultListeners: MutableMap<String, ResultListener> = ConcurrentHashMap()

    override fun setRoot(screen: Screen) {
        rootComponent.navigation.replaceAll(screen)
        resultListeners.clear()
    }

    override fun navigateTo(screen: Screen) {
        rootComponent.navigation.push(screen)
    }

    override fun exit() {
        val key = rootComponent.childStack.items.lastOrNull()
            ?.configuration
            ?.let { screen ->
                screen::class
            }
            ?.key()

        Timber.d(
            "exit: screenKey=%s, hasListener=%s, listeners.size=%s",
            key,
            resultListeners.containsKey(key),
            resultListeners.size
        )

        resultListeners.remove(key)

        rootComponent.navigation.pop { isSuccess ->
            if (!isSuccess) {
                onExitNavigation.invoke()
            }
        }
    }

    override fun setResultListener(
        screenType: KClass<out Screen>,
        onResult: ResultListener
    ) {
        val key = screenType.key()
        resultListeners[key] = onResult
    }

    override fun setResult(
        screenType: KClass<out Screen>,
        result: Any
    ) {
        val key = screenType.key()

        Timber.d(
            "setResult: screenKey=%s, hasListener=%s, listeners.size=%s",
            key,
            resultListeners.containsKey(key),
            resultListeners.size
        )

        resultListeners.remove(key)?.invoke(result)
    }

    private fun KClass<*>.key(): String {
        val typeName = this.java.name
        val startIndex = typeName.indexOf(Screen::class.java.simpleName)

        return if (startIndex in typeName.indices) {
            typeName.substring(startIndex)
        } else {
            typeName
        }
    }
}