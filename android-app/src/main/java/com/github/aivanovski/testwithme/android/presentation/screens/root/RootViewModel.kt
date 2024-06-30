package com.github.aivanovski.testwithme.android.presentation.screens.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.aivanovski.testwithme.android.R
import com.github.aivanovski.testwithme.android.data.Settings
import com.github.aivanovski.testwithme.android.di.GlobalInjector
import com.github.aivanovski.testwithme.android.domain.resources.ResourceProvider
import com.github.aivanovski.testwithme.android.presentation.StartArgs
import com.github.aivanovski.testwithme.android.presentation.core.navigation.Router
import com.github.aivanovski.testwithme.android.presentation.screens.Screen
import com.github.aivanovski.testwithme.android.presentation.screens.flow.model.FlowScreenArgs
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.RootIntent
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.TopBarState
import com.github.aivanovski.testwithme.utils.StringUtils
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.parameter.parametersOf

class RootViewModel(
    private val resourceProvider: ResourceProvider,
    private val settings: Settings,
    private val router: Router,
    private val args: StartArgs
) : ViewModel() {

    val topBarState = MutableStateFlow(newDefaultState())

    fun getStartScreens(): List<Screen> {
        val isLoggedIn = (settings.authToken != null)

        val screens = mutableListOf<Screen>()

        if (isLoggedIn) {
            screens.add(Screen.FlowList)
            if (args.flowUid != null) {
                screens.add(
                    Screen.Flow(
                        args = FlowScreenArgs(
                            flowUid = args.flowUid,
                            screenTitle = StringUtils.EMPTY
                        )
                    )
                )
            }
        } else {
            screens.add(Screen.Login)
        }

        return screens
    }

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
        private val router: Router,
        private val args: StartArgs
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GlobalInjector.get<RootViewModel>(
                params = parametersOf(router, args)
            ) as T
        }
    }
}