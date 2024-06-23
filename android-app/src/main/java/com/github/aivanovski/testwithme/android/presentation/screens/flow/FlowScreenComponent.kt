package com.github.aivanovski.testwithme.android.presentation.screens.flow

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import com.arkivanov.decompose.ComponentContext
import com.github.aivanovski.testwithme.android.presentation.core.navigation.ScreenComponent
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootScreenComponent

class FlowScreenComponent(
    rootComponent: RootScreenComponent
) : ScreenComponent, ComponentContext by rootComponent {

    private val viewModel: FlowViewModel by lazy {
        ViewModelProvider(
            owner = rootComponent.viewModelStoreOwner,
            factory = FlowViewModel.Factory(
                rootViewModel = rootComponent.viewModel,
                router = rootComponent.router
            )
        )[FlowViewModel::class]
    }

    @Composable
    override fun render() {
        FlowScreen(viewModel)
    }
}