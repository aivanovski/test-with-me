package com.github.aivanovski.testwithme.android.presentation.screens.login

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import com.arkivanov.decompose.ComponentContext
import com.github.aivanovski.testwithme.android.presentation.core.navigation.ScreenComponent
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootScreenComponent

class LoginScreenComponent(
    rootComponent: RootScreenComponent
) : ScreenComponent, ComponentContext by rootComponent {

    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(
            owner = rootComponent.viewModelStoreOwner,
            factory = LoginViewModel.Factory()
        )[LoginViewModel::class]
    }

    @Composable
    override fun render() {
        LoginScreen(viewModel)
    }
}