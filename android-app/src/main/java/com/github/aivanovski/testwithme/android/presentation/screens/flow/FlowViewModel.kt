package com.github.aivanovski.testwithme.android.presentation.screens.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.aivanovski.testwithme.android.di.GlobalInjector
import com.github.aivanovski.testwithme.android.presentation.core.navigation.Router
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootViewModel
import org.koin.core.parameter.parametersOf

class FlowViewModel(
    private val interactor: FlowInteractor,
    private val rootViewModel: RootViewModel,
    private val router: Router
) : ViewModel() {

    class Factory(
        private val rootViewModel: RootViewModel,
        private val router: Router
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GlobalInjector.get<FlowViewModel>(
                params = parametersOf(rootViewModel, router)
            ) as T
        }
    }
}