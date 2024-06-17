package com.github.aivanovski.testwithme.android.presentation.screens.root

import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.github.aivanovski.testwithme.android.presentation.core.viewmodel.ViewModelStoreOwnerImpl
import com.github.aivanovski.testwithme.android.presentation.screens.Screen
import com.github.aivanovski.testwithme.android.presentation.screens.login.LoginScreenComponent

class RootScreenComponent(
    componentContext: ComponentContext
) : ComponentContext by componentContext {

    val navigation = StackNavigation<Screen>()

    val childStack = childStack(
        source = navigation,
        serializer = Screen.serializer(),
        initialStack = { listOf(Screen.Login) },
        childFactory = { screen, _ -> createScreenComponent(screen) }
    )

    val viewModelStoreOwner: ViewModelStoreOwner
        get() {
            return instanceKeeper.getOrCreate(::ViewModelStoreOwnerImpl)
        }

    private fun createScreenComponent(screen: Screen): ComponentContext {
        return when (screen) {
            is Screen.Login -> LoginScreenComponent(this)
        }
    }
}