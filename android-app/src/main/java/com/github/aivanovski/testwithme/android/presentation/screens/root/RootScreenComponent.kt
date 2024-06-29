package com.github.aivanovski.testwithme.android.presentation.screens.root

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.github.aivanovski.testwithme.android.presentation.core.navigation.RouterImpl
import com.github.aivanovski.testwithme.android.presentation.core.decompose.ViewModelStoreOwnerImpl
import com.github.aivanovski.testwithme.android.presentation.screens.Screen
import com.github.aivanovski.testwithme.android.presentation.screens.flow.FlowScreenComponent
import com.github.aivanovski.testwithme.android.presentation.screens.flowList.FlowListScreenComponent
import com.github.aivanovski.testwithme.android.presentation.screens.login.LoginScreenComponent
import java.util.UUID

class RootScreenComponent(
    componentContext: ComponentContext,
    onExitNavigation: () -> Unit
) : ComponentContext by componentContext,
    ViewModelStoreOwner by ViewModelStoreOwnerImpl() {

    // TODO: ViewModels instances should be retain in case of screen state restoration

    val navigation = StackNavigation<Screen>()
    val router = RouterImpl(
        rootComponent = this,
        onExitNavigation = onExitNavigation
    )

    val viewModel: RootViewModel by lazy {
        ViewModelProvider(
            owner = this,
            factory = RootViewModel.Factory(router)
        )[RootViewModel::class]
    }

    val childStack = childStack(
        source = navigation,
        serializer = Screen.serializer(),
        initialStack = { listOf(Screen.Login) },
        childFactory = { screen, _ -> createScreenComponent(screen) }
    )

    private val backCallback = BackCallback(
        isEnabled = true,
        onBack = {
            router.exit()
        }
    )

    init {
        backHandler.register(backCallback)
    }

    private fun createScreenComponent(screen: Screen): ComponentContext {
        val lifecycle = LifecycleRegistry()
        lifecycle.attachToParent(this.lifecycle)

        return when (screen) {
            is Screen.Login -> {
                LoginScreenComponent(
                    component = childContext(
                        key = UUID.randomUUID().toString(),
                        lifecycle = lifecycle
                    ),
                    rootViewModel = viewModel,
                    router = router
                )
            }

            is Screen.FlowList -> {
                FlowListScreenComponent(
                    component = childContext(
                        key = UUID.randomUUID().toString(),
                        lifecycle = lifecycle
                    ),
                    rootViewModel = viewModel,
                    router = router
                )
            }

            is Screen.Flow -> {
                FlowScreenComponent(
                    component = childContext(
                        key = UUID.randomUUID().toString(),
                        lifecycle = lifecycle
                    ),
                    rootViewModel = viewModel,
                    router = router,
                    args = screen.args
                )
            }
        }
    }

    private fun LifecycleRegistry.attachToParent(parent: Lifecycle) {
        val child = this

        val observer = object : Lifecycle.Callbacks {

            override fun onCreate() {
                child.onCreate()
            }

            override fun onStart() {
                child.onStart()
            }

            override fun onResume() {
                child.onResume()
            }

            override fun onPause() {
                child.onPause()
            }

            override fun onStop() {
                child.onStop()
            }

            override fun onDestroy() {
                parent.unsubscribe(this)
            }
        }

        parent.subscribe(observer)
    }
}