package com.github.aivanovski.testwithme.android.presentation.screens.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.aivanovski.testwithme.android.R
import com.github.aivanovski.testwithme.android.di.GlobalInjector
import com.github.aivanovski.testwithme.android.domain.resources.ResourceProvider
import com.github.aivanovski.testwithme.android.presentation.core.navigation.Router
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.RootIntent
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.TopBarState
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.parameter.parametersOf

class RootViewModel(
    private val resourceProvider: ResourceProvider,
    private val router: Router
) : ViewModel() {

    val topBarState = MutableStateFlow(newDefaultState())

    fun sendIntent(intent: RootIntent) {
        handleIntent(intent)
    }

    private fun handleIntent(intent: RootIntent) {
        when (intent) {
            is RootIntent.SetTopBarState -> {
                topBarState.value = intent.state
            }

            RootIntent.NavigateBack -> {
                router.exit()
            }
        }
    }

    private fun newDefaultState(): TopBarState {
        return TopBarState(
            title = resourceProvider.getString(R.string.app_name),
            isBackVisible = false
        )
    }

    class Factory(
        private val router: Router
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GlobalInjector.get<RootViewModel>(
                params = parametersOf(router)
            ) as T
        }
    }
}