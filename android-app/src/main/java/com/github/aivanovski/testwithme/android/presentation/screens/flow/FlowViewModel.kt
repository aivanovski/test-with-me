package com.github.aivanovski.testwithme.android.presentation.screens.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.aivanovski.testwithme.android.di.GlobalInjector
import com.github.aivanovski.testwithme.android.presentation.core.IntentProviderImpl
import com.github.aivanovski.testwithme.android.presentation.core.navigation.Router
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.FlowCellModelFactory
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.FlowCellViewModelFactory
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.FlowTitleCellIntent
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.HistoryItemCellIntent
import com.github.aivanovski.testwithme.android.presentation.screens.flow.model.FlowIntent
import com.github.aivanovski.testwithme.android.presentation.screens.flow.model.FlowScreenArgs
import com.github.aivanovski.testwithme.android.presentation.screens.flow.model.FlowState
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.RootIntent
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.TopBarState
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
import timber.log.Timber

class FlowViewModel(
    private val interactor: FlowInteractor,
    private val modelFactory: FlowCellModelFactory,
    private val viewModelFactory: FlowCellViewModelFactory,
    private val rootViewModel: RootViewModel,
    private val router: Router,
    private val args: FlowScreenArgs
) : ViewModel() {

    val state = MutableStateFlow<FlowState>(FlowState.NotInitialized)
    private val intents = Channel<FlowIntent>()
    private val intentProvider = IntentProviderImpl()

    fun start() {
        rootViewModel.sendIntent(RootIntent.SetTopBarState(createTopBarState()))

        if (state.value == FlowState.NotInitialized) {
            viewModelScope.launch {
                intents.receiveAsFlow()
                    .onStart { emit(FlowIntent.Initialize) }
                    .flatMapLatest { intent -> handleIntent(intent, state.value) }
                    .collect { newState ->
                        state.value = newState
                    }
            }

            subscribeToEvents()
        } else {
            sendIntent(FlowIntent.Initialize)
        }
    }

    fun stop() {
    }

    fun clear() {
        onCleared()
        unsubscribe()
    }

    fun sendIntent(intent: FlowIntent) {
        viewModelScope.launch {
            intents.send(intent)
        }
    }

    fun handleIntent(
        intent: FlowIntent,
        state: FlowState
    ): Flow<FlowState> {
        return when (intent) {
            FlowIntent.Initialize -> loadData()
        }
    }

    private fun subscribeToEvents() {
        intentProvider.subscribe(this) { intent ->
            when (intent) {
                is FlowTitleCellIntent.OnRunButtonClick -> onRunButtonClicked(intent.id)
                is HistoryItemCellIntent.OnItemClick -> onHistoryItemClicked(intent.id)
            }
        }
    }

    private fun onRunButtonClicked(flowUid: String) {
        Timber.d("onRunButtonClicked: flowUid=$flowUid")
    }

    private fun onHistoryItemClicked(runUid: String) {
        Timber.d("onHistoryItemClicked: runUid=$runUid")
    }

    private fun unsubscribe() {
        intentProvider.clear()
    }

    private fun loadData(): Flow<FlowState> {
        return flow {
            emit(FlowState.Loading)

            val loadDataResult = interactor.loadData(args.flowUid)
            if (loadDataResult.isRight()) {
                val (flow, project, runs) = loadDataResult.unwrap()

                val models = modelFactory.createCellModels(project, flow, runs)
                val viewModels = viewModelFactory.createCellViewModels(models, intentProvider)

                emit(
                    FlowState.Data(
                        viewModels = viewModels
                    )
                )
            } else {
                loadDataResult.unwrapError().printStackTrace()
            }
        }
    }

    private fun createTopBarState(): TopBarState {
        return TopBarState(
            title = args.screenTitle,
            isBackVisible = true
        )
    }

    class Factory(
        private val rootViewModel: RootViewModel,
        private val router: Router,
        private val args: FlowScreenArgs
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GlobalInjector.get<FlowViewModel>(
                params = parametersOf(rootViewModel, router, args)
            ) as T
        }
    }
}