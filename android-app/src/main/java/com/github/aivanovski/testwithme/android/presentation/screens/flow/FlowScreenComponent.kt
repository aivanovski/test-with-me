package com.github.aivanovski.testwithme.android.presentation.screens.flow

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.subscribe
import com.github.aivanovski.testwithme.android.presentation.core.decompose.ViewModelStoreOwnerImpl
import com.github.aivanovski.testwithme.android.presentation.core.navigation.Router
import com.github.aivanovski.testwithme.android.presentation.core.navigation.ScreenComponent
import com.github.aivanovski.testwithme.android.presentation.screens.flow.model.FlowScreenArgs
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootViewModel

class FlowScreenComponent(
    component: ComponentContext,
    private val rootViewModel: RootViewModel,
    private val router: Router,
    args: FlowScreenArgs
) : ScreenComponent,
    ComponentContext by component,
    ViewModelStoreOwner by ViewModelStoreOwnerImpl() {

    private val viewModel: FlowViewModel by lazy {
        ViewModelProvider(
            owner = this,
            factory = FlowViewModel.Factory(
                rootViewModel = rootViewModel,
                router = router,
                args = args
            )
        )[FlowViewModel::class]
    }

    init {
        lifecycle.subscribe(
            onStart = {
                viewModel.start()
            },
            onStop = {
                viewModel.stop()
            },
            onDestroy = {
                viewModel.clear()
            }
        )
    }

    @Composable
    override fun render() {
        FlowScreen(viewModel)
    }
}