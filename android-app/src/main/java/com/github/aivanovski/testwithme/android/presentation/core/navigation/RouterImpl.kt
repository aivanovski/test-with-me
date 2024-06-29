package com.github.aivanovski.testwithme.android.presentation.core.navigation

import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.github.aivanovski.testwithme.android.presentation.screens.Screen
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootScreenComponent
import timber.log.Timber

class RouterImpl(
    private val rootComponent: RootScreenComponent,
    private val onExitNavigation: () -> Unit
) : Router {

    override fun setRoot(screen: Screen) {
        rootComponent.navigation.replaceAll(screen)
    }

    override fun navigateTo(screen: Screen) {
        rootComponent.navigation.push(screen)
    }

    override fun exit() {
        rootComponent.navigation.pop {  isSuccess ->
            if (!isSuccess) {
                onExitNavigation.invoke()
            }
            Timber.d("isSuccess=$isSuccess")
        }
    }
}