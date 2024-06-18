package com.github.aivanovski.testwithme.android.presentation.screens.flowList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.aivanovski.testwithme.android.di.GlobalInjector
import com.github.aivanovski.testwithme.android.domain.ErrorInteractor
import com.github.aivanovski.testwithme.android.presentation.core.navigation.Router
import com.github.aivanovski.testwithme.android.presentation.screens.flowList.model.FlowItem
import com.github.aivanovski.testwithme.android.presentation.screens.flowList.model.FlowListIntent
import com.github.aivanovski.testwithme.android.presentation.screens.flowList.model.FlowListState
import com.github.aivanovski.testwithme.extensions.unwrap
import com.github.aivanovski.testwithme.extensions.unwrapError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

class FlowListViewModel(
    private val interactor: FlowListInteractor,
    private val errorInteractor: ErrorInteractor,
    private val router: Router
) : ViewModel() {

    val state = MutableStateFlow<FlowListState>(FlowListState.NotInitialized)

    private val intents = Channel<FlowListIntent>()

    fun start() {
        if (state.value != FlowListState.NotInitialized) {
            return
        }

        viewModelScope.launch {
            intents.receiveAsFlow()
                .onStart { emit(FlowListIntent.Initialize) }
                .flatMapLatest { intent -> handleIntent(intent, state.value) }
                .collect { newState ->
                    state.value = newState
                }
        }
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
            is FlowListIntent.OnFlowClicked -> TODO()
        }
    }

    private fun loadData(): Flow<FlowListState> {
        return flow {
            emit(FlowListState.Loading)

            val getFlowsResult = interactor.getFlows()
            if (getFlowsResult.isRight()) {
                val items = getFlowsResult.unwrap()
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
                        message = errorInteractor.getMessage(getFlowsResult.unwrapError())
                    )
                )
            }
        }
    }

    class Factory(private val router: Router) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GlobalInjector.get<FlowListViewModel>(
                params = parametersOf(router)
            ) as T
        }
    }
}