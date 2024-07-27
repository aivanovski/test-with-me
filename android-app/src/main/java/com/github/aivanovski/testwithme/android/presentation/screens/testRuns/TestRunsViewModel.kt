package com.github.aivanovski.testwithme.android.presentation.screens.testRuns

import androidx.lifecycle.viewModelScope
import com.github.aivanovski.testwithme.android.R
import com.github.aivanovski.testwithme.android.domain.resources.ResourceProvider
import com.github.aivanovski.testwithme.android.entity.SourceType
import com.github.aivanovski.testwithme.android.presentation.core.BaseViewModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellIntent
import com.github.aivanovski.testwithme.android.presentation.core.cells.model.IconThreeTextCellIntent
import com.github.aivanovski.testwithme.android.presentation.core.cells.screen.TerminalState
import com.github.aivanovski.testwithme.android.presentation.core.cells.screen.toTerminalState
import com.github.aivanovski.testwithme.android.presentation.core.navigation.Router
import com.github.aivanovski.testwithme.android.presentation.screens.Screen
import com.github.aivanovski.testwithme.android.presentation.screens.flow.model.FlowScreenArgs
import com.github.aivanovski.testwithme.android.presentation.screens.flow.model.FlowScreenMode
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.BottomBarState
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.MenuState
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.RootIntent.SetBottomBarState
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.RootIntent.SetMenuState
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.RootIntent.SetTopBarState
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.TopBarState
import com.github.aivanovski.testwithme.android.presentation.screens.testRun.model.TestRunScreenArgs
import com.github.aivanovski.testwithme.android.presentation.screens.testRuns.cells.TestRunsCellFactory
import com.github.aivanovski.testwithme.android.presentation.screens.testRuns.model.TestRunsData
import com.github.aivanovski.testwithme.android.presentation.screens.testRuns.model.TestRunsIntent
import com.github.aivanovski.testwithme.android.presentation.screens.testRuns.model.TestRunsState
import com.github.aivanovski.testwithme.android.utils.formatError
import com.github.aivanovski.testwithme.extensions.unwrap
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TestRunsViewModel(
    private val interactor: TestRunsInteractor,
    private val cellFactory: TestRunsCellFactory,
    private val resourceProvider: ResourceProvider,
    private val rootViewModel: RootViewModel,
    private val router: Router
) : BaseViewModel() {

    val state = MutableStateFlow(TestRunsState())
    private val intents = Channel<TestRunsIntent>()
    private var isSubscribed = false
    private var data: TestRunsData? = null

    override fun start() {
        super.start()

        rootViewModel.sendIntent(SetTopBarState(createInitialTopBarState()))
        rootViewModel.sendIntent(SetBottomBarState(createBottomBarState()))
        rootViewModel.sendIntent(SetMenuState(MenuState.LOG_OUT))

        if (!isSubscribed) {
            isSubscribed = true

            viewModelScope.launch {
                intents.receiveAsFlow()
                    .onStart { emit(TestRunsIntent.Initialize) }
                    .flatMapLatest { intent -> handleIntent(intent, state.value) }
                    .collect { newState ->
                        state.value = newState
                    }
            }
        }
    }

    override fun handleCellIntent(intent: BaseCellIntent) {
        when (intent) {
            is IconThreeTextCellIntent.OnClick -> navigateToTestRunScreen(jobUid = intent.id)
        }
    }

    private fun handleIntent(
        intent: TestRunsIntent,
        state: TestRunsState
    ): Flow<TestRunsState> {
        return when (intent) {
            TestRunsIntent.Initialize -> loadData()
        }
    }

    private fun navigateToTestRunScreen(jobUid: String) {
        val data = data ?: return

        val job = data.jobHistory
            .firstOrNull { job -> job.uid == jobUid }
            ?: return

        val flow = data.allFlows
            .firstOrNull { flow -> flow.entry.uid == job.flowUid }
            ?: return

        when (flow.entry.sourceType) {
            SourceType.LOCAL -> {
                router.navigateTo(
                    Screen.TestRun(
                        TestRunScreenArgs(
                            jobUid = jobUid,
                            screenTitle = flow.entry.name
                        )
                    )
                )
            }

            SourceType.REMOTE -> {
                router.navigateTo(
                    Screen.Flow(
                        FlowScreenArgs(
                            mode = FlowScreenMode.Flow(flow.entry.uid),
                            screenTitle = flow.entry.name
                        )
                    )
                )
            }
        }
    }

    private fun loadData(): Flow<TestRunsState> {
        return flow {
            emit(TestRunsState(terminalState = TerminalState.Loading))

            val loadDataResult = interactor.loadData()
            if (loadDataResult.isLeft()) {
                val terminalState = loadDataResult
                    .formatError(resourceProvider)
                    .toTerminalState()

                emit(TestRunsState(terminalState = terminalState))
                return@flow
            }

            data = loadDataResult.unwrap()
            val data = loadDataResult.unwrap()

            if (data.localRuns.isNotEmpty()) {
                val viewModels = cellFactory.createCellViewModels(
                    data = data,
                    intentProvider = intentProvider
                )
                emit(TestRunsState(viewModels = viewModels))
            } else {
                val message = resourceProvider.getString(R.string.no_tests)
                emit(TestRunsState(terminalState = TerminalState.Empty(message)))
            }
        }
    }

    private fun createInitialTopBarState(): TopBarState {
        return TopBarState(
            title = resourceProvider.getString(R.string.test_runs),
            isBackVisible = false
        )
    }

    private fun createBottomBarState(): BottomBarState {
        return rootViewModel.bottomBarState.value.copy(
            isVisible = true,
            selectedIndex = 1
        )
    }
}