package com.github.aivanovski.testwithme.android.presentation.screens.flowList

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import com.arkivanov.decompose.ComponentContext
import com.github.aivanovski.testwithme.android.presentation.core.navigation.ScreenComponent
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootScreenComponent

class FlowListScreenComponent(
    rootComponent: RootScreenComponent
) : ScreenComponent, ComponentContext by rootComponent {

    private val viewModel: FlowListViewModel by lazy {
        ViewModelProvider(
            owner = rootComponent.viewModelStoreOwner,
            factory = FlowListViewModel.Factory(rootComponent.router)
        )[FlowListViewModel::class]
    }

    @Composable
    override fun render() {
        FlowListScreen(viewModel)
    }
}