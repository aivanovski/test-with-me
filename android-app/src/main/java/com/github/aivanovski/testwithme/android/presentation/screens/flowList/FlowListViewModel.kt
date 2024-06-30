package com.github.aivanovski.testwithme.android.presentation.screens.flowList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.aivanovski.testwithme.android.di.GlobalInjector
import com.github.aivanovski.testwithme.android.domain.ErrorInteractor
import com.github.aivanovski.testwithme.android.entity.db.FlowEntry
import com.github.aivanovski.testwithme.android.presentation.core.navigation.Router
import com.github.aivanovski.testwithme.android.presentation.screens.Screen
import com.github.aivanovski.testwithme.android.presentation.screens.flow.model.FlowScreenArgs
import com.github.aivanovski.testwithme.android.presentation.screens.flowList.model.FlowItem
import com.github.aivanovski.testwithme.android.presentation.screens.flowList.model.FlowListIntent
import com.github.aivanovski.testwithme.android.presentation.screens.flowList.model.FlowListState
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.RootIntent
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.TopBarState
import com.github.aivanovski.testwithme.extensions.unwrap
import com.github.aivanovski.testwithme.extensions.unwrapError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

class FlowListViewModel(
    private val interactor: FlowListInteractor,
    private val errorInteractor: ErrorInteractor,
    private val rootViewModel: RootViewModel,
    private val router: Router
) : ViewModel() {

    val state = MutableStateFlow<FlowListState>(FlowListState.NotInitialized)

    private val intents = Channel<FlowListIntent>()
    private var flows: List<FlowEntry> = emptyList()

    fun start() {
        rootViewModel.sendIntent(RootIntent.SetTopBarState(createTopBarState()))

        if (state.value == FlowListState.NotInitialized) {
            viewModelScope.launch {
                intents.receiveAsFlow()
                    .onStart { emit(FlowListIntent.Initialize) }
                    .flatMapLatest { intent -> handleIntent(intent, state.value) }
                    .collect { newState ->
                        state.value = newState
                    }
            }
        }
    }

    fun clear() {
        onCleared()
    }

    fun sendIntent(intent: FlowListIntent) {
        viewModelScope.launch {
            intents.send(intent)
        }
    }

    private fun handleIntent(
        intent: FlowListIntent,
        state: FlowListState
    ): Flow<FlowListState> {
        return when (intent) {
            FlowListIntent.Initialize -> loadData()
            is FlowListIntent.OnFlowClicked -> onFlowClicked(intent)
        }
    }

    private fun onFlowClicked(intent: FlowListIntent.OnFlowClicked): Flow<FlowListState> {
        val flow = flows.firstOrNull { flow -> flow.uid == intent.uid }
            ?: return emptyFlow()

        router.navigateTo(
            Screen.Flow(
                FlowScreenArgs(
                    flowUid = intent.uid,
                    screenTitle = flow.name
                )
            )
        )

        return emptyFlow()
    }

    private fun loadData(): Flow<FlowListState> {
        return flow {
            emit(FlowListState.Loading)

            val getFlowsResult = interactor.getFlows()
            if (getFlowsResult.isRight()) {
                flows = getFlowsResult.unwrap()

                val items = flows
                    .map { flow ->
                        FlowItem(
                            uid = flow.uid,
                            name = flow.name
                        )
                    }

                emit(FlowListState.Data(items))
            } else {
                emit(
                    FlowListState.Error(
                        message = errorInteractor.formatMessage(getFlowsResult.unwrapError())
                    )
                )
            }
        }
    }

    private fun createTopBarState(): TopBarState {
        return TopBarState(
            title = "Flows", // TODO: string
            isBackVisible = false
        )
    }

    class Factory(
        private val rootViewModel: RootViewModel,
        private val router: Router
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GlobalInjector.get<FlowListViewModel>(
                params = parametersOf(rootViewModel, router)
            ) as T
        }
    }
}