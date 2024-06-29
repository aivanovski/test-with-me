package com.github.aivanovski.testwithme.android.presentation.screens.login

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.subscribe
import com.github.aivanovski.testwithme.android.presentation.core.decompose.ViewModelStoreOwnerImpl
import com.github.aivanovski.testwithme.android.presentation.core.navigation.Router
import com.github.aivanovski.testwithme.android.presentation.core.navigation.ScreenComponent
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootViewModel

class LoginScreenComponent(
    component: ComponentContext,
    private val rootViewModel: RootViewModel,
    private val router: Router,
) : ScreenComponent,
    ComponentContext by component,
    ViewModelStoreOwner by ViewModelStoreOwnerImpl() {

    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(
            owner = this,
            factory = LoginViewModel.Factory(
                rootViewModel = rootViewModel,
                router = router
            )
        )[LoginViewModel::class]
    }

    init {
        lifecycle.subscribe(
            onCreate = {
                viewModel.start()
            },
            onDestroy = {
                viewModel.clear()
            }
        )
    }

    @Composable
    override fun render() {
        LoginScreen(viewModel)
    }
}