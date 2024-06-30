package com.github.aivanovski.testwithme.android.presentation.screens.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.aivanovski.testwithme.android.R
import com.github.aivanovski.testwithme.android.di.GlobalInjector
import com.github.aivanovski.testwithme.android.domain.ErrorInteractor
import com.github.aivanovski.testwithme.android.domain.resources.ResourceProvider
import com.github.aivanovski.testwithme.android.presentation.core.IntentProviderImpl
import com.github.aivanovski.testwithme.android.presentation.core.compose.dialogs.model.MessageDialogButton
import com.github.aivanovski.testwithme.android.presentation.core.compose.dialogs.model.MessageDialogState
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
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.RootIntent.SetTopBarState
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.TopBarState
import com.github.aivanovski.testwithme.extensions.unwrap
import com.github.aivanovski.testwithme.extensions.unwrapError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okhttp3.internal.notify
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.util.UUID

class FlowViewModel(
    private val interactor: FlowInteractor,
    private val errorInteractor: ErrorInteractor,
    private val modelFactory: FlowCellModelFactory,
    private val viewModelFactory: FlowCellViewModelFactory,
    private val resourceProvider: ResourceProvider,
    private val rootViewModel: RootViewModel,
    private val router: Router,
    private val args: FlowScreenArgs
) : ViewModel() {

    val state = MutableStateFlow<FlowState>(FlowState.NotInitialized)
    private val intents = Channel<FlowIntent>()
    private val intentProvider = IntentProviderImpl()
    private var lastJobUid: String? = null

    fun start() {
        rootViewModel.sendIntent(
            SetTopBarState(
                createTopBarState(flowName = args.screenTitle)
            )
        )

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

    private fun handleIntent(
        intent: FlowIntent,
        state: FlowState
    ): Flow<FlowState> {
        return when (intent) {
            FlowIntent.Initialize -> loadData()
            FlowIntent.OnDismissErrorDialog -> onDismissErrorDialog(state)
            FlowIntent.OnDismissFlowDialog -> onDismissFlowDialog(state)
            is FlowIntent.OnFlowDialogActionClick -> onFlowDialogActionClick(intent, state)
            is FlowIntent.RunFlow -> startFlow(intent, state)
        }
    }

    private fun subscribeToEvents() {
        intentProvider.subscribe(this) { intent ->
            when (intent) {
                is FlowTitleCellIntent.OnRunButtonClick -> {
                    sendIntent(FlowIntent.RunFlow(intent.id))
                }

                is HistoryItemCellIntent.OnItemClick -> onHistoryItemClicked(intent.id)
            }
        }
    }

    private fun onHistoryItemClicked(runUid: String) {
        Timber.d("onHistoryItemClicked: runUid=$runUid")
    }

    private fun unsubscribe() {
        intentProvider.clear()
    }


    private fun startFlow(
        intent: FlowIntent.RunFlow,
        state: FlowState
    ): Flow<FlowState> {
        if (state !is FlowState.Data) return emptyFlow()

        val isDriverRunning = interactor.isDriverServiceEnabled()
        if (!isDriverRunning) {
            return flowOf(
                state.copy(
                    flowDialogState = createDriverNotRunningDialogState()
                )
            )
        }

        val jobUid = newJobUid()
        lastJobUid = jobUid

        return flow {
            emit(
                state.copy(
                    flowDialogState = createPrepareDialogState()
                )
            )

            val startResult = interactor.startFlow(intent.flowUid, jobUid)
            if (startResult.isLeft()) {
                emit(
                    state.copy(
                        errorDialogMessage = errorInteractor.formatMessage(startResult.unwrapError())
                    )
                )
                return@flow
            }

            delay(2000L) // TODO: refactor

            emit(
                state.copy(
                    flowDialogState = createWaitingForLaunchDialogState()
                )
            )
        }
    }

    private fun loadData(): Flow<FlowState> {
        return flow {
            emit(FlowState.Loading)

            val loadDataResult = interactor.loadData(args.flowUid)
            if (loadDataResult.isRight()) {
                val data = loadDataResult.unwrap()

                val models = modelFactory.createCellModels(
                    project = data.project,
                    flow = data.flow,
                    runs = data.runs,
                    users = data.users
                )

                rootViewModel.sendIntent(
                    SetTopBarState(
                        createTopBarState(
                            flowName = data.flow.entry.name
                        )
                    )
                )

                emit(
                    FlowState.Data(
                        viewModels = viewModelFactory.createCellViewModels(
                            models,
                            intentProvider
                        ),
                        errorDialogMessage = null,
                        flowDialogState = null,
                        isLaunchServices = false
                    )
                )
            } else {
                loadDataResult.unwrapError().printStackTrace()
            }
        }
    }

    private fun onDismissErrorDialog(
        state: FlowState
    ): Flow<FlowState> {
        val data = state.asData() ?: return emptyFlow()

        return flowOf(
            data.copy(
                errorDialogMessage = null
            )
        )
    }

    private fun onDismissFlowDialog(
        state: FlowState
    ): Flow<FlowState> {
        val data = state.asData() ?: return emptyFlow()

        return flowOf(
            data.copy(
                flowDialogState = null
            )
        )
    }

    private fun onFlowDialogActionClick(
        intent: FlowIntent.OnFlowDialogActionClick,
        state: FlowState
    ): Flow<FlowState> {
        val data = state.asData() ?: return emptyFlow()

        return when (intent.actionId) {
            LAUNCH_SERVICES_DIALOG_ACTION -> {
                flowOf(
                    data.copy(
                        isLaunchServices = true
                    )
                )
            }

            CANCEL_FLOW_DIALOG_ACTION -> {
                flow {
                    val jobUid = lastJobUid
                    if (jobUid != null) {
                        val cancelResult = interactor.cancelJob(jobUid)
                        if (cancelResult.isRight()) {
                            emit(
                                data.copy(
                                    flowDialogState = null
                                )
                            )
                        } else {
                            emit(
                                data.copy(
                                    flowDialogState = null,
                                    errorDialogMessage = errorInteractor.formatMessage(
                                        cancelResult.unwrapError()
                                    )
                                )
                            )
                        }

                        lastJobUid = null
                        return@flow
                    }

                    data.copy(
                        flowDialogState = null
                    )
                }
            }

            else -> emptyFlow()
        }
    }

    private fun createTopBarState(
        flowName: String
    ): TopBarState {
        return TopBarState(
            title = flowName,
            isBackVisible = true
        )
    }

    private fun createPrepareDialogState(): MessageDialogState {
        return MessageDialogState(
            title = null,
            message = resourceProvider.getString(R.string.prepare_flow_message),
            isCancellable = false,
            actionButton = MessageDialogButton.ActionButton(
                title = resourceProvider.getString(R.string.cancel),
                actionId = CANCEL_FLOW_DIALOG_ACTION
            )
        )
    }

    private fun createDriverNotRunningDialogState(): MessageDialogState {
        return MessageDialogState(
            title = resourceProvider.getString(R.string.driver_is_not_enabled_title),
            message = resourceProvider.getString(R.string.driver_is_not_enabled_message),
            isCancellable = true,
            actionButton = MessageDialogButton.ActionButton(
                title = resourceProvider.getString(R.string.services),
                actionId = LAUNCH_SERVICES_DIALOG_ACTION
            )
        )
    }

    private fun createWaitingForLaunchDialogState(): MessageDialogState {
        return MessageDialogState(
            title = null,
            message = resourceProvider.getString(R.string.awaiting_start_message),
            isCancellable = false,
            actionButton = MessageDialogButton.ActionButton(
                title = resourceProvider.getString(R.string.cancel),
                actionId = CANCEL_FLOW_DIALOG_ACTION
            )
        )
    }

    private fun newJobUid(): String = UUID.randomUUID().toString()

    private fun FlowState.asData(): FlowState.Data? {
        return this as? FlowState.Data
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

    companion object {
        private const val CANCEL_FLOW_DIALOG_ACTION = 1
        private const val LAUNCH_SERVICES_DIALOG_ACTION = 2
    }
}