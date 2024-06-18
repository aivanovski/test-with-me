package com.github.aivanovski.testwithme.android.presentation.core.navigation

import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.github.aivanovski.testwithme.android.presentation.screens.Screen
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootScreenComponent

class RouterImpl(
    private val rootComponent: RootScreenComponent
) : Router {

    override fun navigateTo(screen: Screen) {
        rootComponent.navigation.push(screen)
    }

    override fun exit() {
        rootComponent.navigation.pop()
    }
}