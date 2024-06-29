package com.github.aivanovski.testwithme.android.presentation.screens.flowList

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.subscribe
import com.github.aivanovski.testwithme.android.presentation.core.decompose.ViewModelStoreOwnerImpl
import com.github.aivanovski.testwithme.android.presentation.core.navigation.Router
import com.github.aivanovski.testwithme.android.presentation.core.navigation.ScreenComponent
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootViewModel
import timber.log.Timber

class FlowListScreenComponent(
    component: ComponentContext,
    rootViewModel: RootViewModel,
    router: Router
) : ScreenComponent,
    ComponentContext by component,
    ViewModelStoreOwner by ViewModelStoreOwnerImpl() {

    override val viewModelStore: ViewModelStore
        get() = ViewModelStore()

    private val viewModel: FlowListViewModel by lazy {
        ViewModelProvider(
            owner = this,
            factory = FlowListViewModel.Factory(
                rootViewModel = rootViewModel,
                router = router
            )
        )[FlowListViewModel::class]
    }

    init {
        lifecycle.subscribe(
            onStart = {
                viewModel.start()
            },
            onDestroy = {
                viewModel.clear()
            }
        )
    }

    @Composable
    override fun render() {
        FlowListScreen(viewModel)
    }
}