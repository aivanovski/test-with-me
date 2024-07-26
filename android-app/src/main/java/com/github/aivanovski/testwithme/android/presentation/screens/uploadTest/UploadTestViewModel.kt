package com.github.aivanovski.testwithme.android.presentation.screens.uploadTest

import androidx.lifecycle.viewModelScope
import com.github.aivanovski.testwithme.android.R
import com.github.aivanovski.testwithme.android.domain.resources.ResourceProvider
import com.github.aivanovski.testwithme.android.entity.Group
import com.github.aivanovski.testwithme.android.entity.db.ProjectEntry
import com.github.aivanovski.testwithme.android.presentation.core.BaseViewModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.screen.TerminalState
import com.github.aivanovski.testwithme.android.presentation.core.cells.screen.toTerminalState
import com.github.aivanovski.testwithme.android.presentation.core.compose.dialogs.model.MessageDialogButton
import com.github.aivanovski.testwithme.android.presentation.core.compose.dialogs.model.MessageDialogState
import com.github.aivanovski.testwithme.android.presentation.core.navigation.Router
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.RootIntent.SetTopBarState
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.TopBarState
import com.github.aivanovski.testwithme.android.presentation.screens.uploadTest.model.UploadTestIntent
import com.github.aivanovski.testwithme.android.presentation.screens.uploadTest.model.UploadTestScreenArgs
import com.github.aivanovski.testwithme.android.presentation.screens.uploadTest.model.UploadTestScreenData
import com.github.aivanovski.testwithme.android.presentation.screens.uploadTest.model.UploadTestState
import com.github.aivanovski.testwithme.android.utils.formatErrorMessage
import com.github.aivanovski.testwithme.extensions.unwrap
import com.github.aivanovski.testwithme.extensions.unwrapError
import com.github.aivanovski.testwithme.web.api.request.PostFlowRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class UploadTestViewModel(
    private val interactor: UploadTestInteractor,
    private val resourceProvider: ResourceProvider,
    private val rootViewModel: RootViewModel,
    private val router: Router,
    private val args: UploadTestScreenArgs
) : BaseViewModel() {

    val state = MutableStateFlow(newInitialState())
    private val intents = Channel<UploadTestIntent>()
    private var isInitialized = false
    private var data: UploadTestScreenData? = null

    override fun start() {
        super.start()

        rootViewModel.sendIntent(SetTopBarState(createInitialTopBarState()))

        if (!isInitialized) {
            isInitialized = true

            viewModelScope.launch {
                intents.receiveAsFlow()
                    .onStart { emit(UploadTestIntent.Initialize) }
                    .flatMapLatest { intent -> handleIntent(intent, state.value) }
                    .collect { newState ->
                        state.value = newState
                    }
            }
        }
    }

    fun sendIntent(intent: UploadTestIntent) {
        viewModelScope.launch {
            intents.send(intent)
        }
    }

    private fun handleIntent(
        intent: UploadTestIntent,
        state: UploadTestState
    ): Flow<UploadTestState> {
        return when (intent) {
            UploadTestIntent.Initialize -> loadData()
            UploadTestIntent.OnUploadButtonClick -> uploadTest(state)
            is UploadTestIntent.OnProjectSelected -> onProjectSelected(intent, state)
            is UploadTestIntent.OnGroupSelected -> onGroupSelected(intent, state)
            is UploadTestIntent.OnDialogActionClick -> {
                onDialogClick(intent)
                emptyFlow()
            }
        }
    }

    private fun onDialogClick(intent: UploadTestIntent.OnDialogActionClick) {
        when (intent.actionId) {
            ACTION_OK -> {
                router.exit()
            }
        }
    }

    private fun onProjectSelected(
        intent: UploadTestIntent.OnProjectSelected,
        state: UploadTestState
    ): Flow<UploadTestState> {
        val data = this.data ?: return emptyFlow()

        val projects = data.projects.formatProjects()
        val selectedProject = data.projects.findProjectByName(intent.projectName)
            ?: return emptyFlow()

        val groups = data.groups
            .filter { group -> group.projectUid == selectedProject.uid }
            .formatGroups()

        return flowOf(
            state.copy(
                projects = projects,
                selectedProject = selectedProject.name,
                groups = groups,
                selectedGroup = groups.first()
            )
        )
    }

    private fun onGroupSelected(
        intent: UploadTestIntent.OnGroupSelected,
        state: UploadTestState
    ): Flow<UploadTestState> {
        val data = this.data ?: return emptyFlow()

        val groups = data.groups.formatGroups()
        val selectedGroup = data.groups.findGroupByName(intent.groupName)
            ?: return emptyFlow()

        return flowOf(
            state.copy(
                groups = groups,
                selectedGroup = selectedGroup.name
            )
        )
    }

    private fun uploadTest(initialState: UploadTestState): Flow<UploadTestState> {
        val data = this.data ?: return emptyFlow()
        val project = getSelectedProject() ?: return emptyFlow()

        val group = getSelectedGroup()

        return flow {
            emit(initialState.copy(terminalState = TerminalState.Loading))

            val request = PostFlowRequest(
                projectId = project.uid,
                groupId = group?.uid,
                path = null,
                base64Content = data.base64Content
            )

            val uploadResult = interactor.uploadFlow(
                flowUid = args.flowUid,
                request = request
            )
            if (uploadResult.isLeft()) {
                val terminalState = uploadResult.unwrapError()
                    .formatErrorMessage(resourceProvider)
                    .toTerminalState()

                emit(initialState.copy(terminalState = terminalState))
                return@flow
            }

            val dialogState = createUploadSuccessDialog()
            emit(initialState.copy(dialogState = dialogState))
        }
    }

    private fun loadData(): Flow<UploadTestState> {
        return flow {
            emit(UploadTestState(terminalState = TerminalState.Loading))

            val loadDataResult = interactor.loadData(args.flowUid)
            if (loadDataResult.isLeft()) {
                val terminalState = loadDataResult.unwrapError()
                    .formatErrorMessage(resourceProvider)
                    .toTerminalState()

                emit(UploadTestState(terminalState = terminalState))
                return@flow
            }


            data = loadDataResult.unwrap()
            val data = loadDataResult.unwrap()

            if (data.projects.isEmpty()) {
                val message = resourceProvider.getString(R.string.no_projects_message)
                emit(UploadTestState(terminalState = TerminalState.Empty(message)))
                return@flow
            }

            val selectedProject = data.projects.first()

            val groups = data.groups
                .filter { group -> group.projectUid == selectedProject.uid }
                .formatGroups()

            emit(
                UploadTestState(
                    projects = data.projects.formatProjects(),
                    groups = groups,
                    selectedProject = selectedProject.name,
                    selectedGroup = groups.first()
                )
            )
        }
    }

    private fun List<ProjectEntry>.formatProjects(): List<String> {
        return this.map { project -> project.name }
    }

    private fun List<Group>.formatGroups(): List<String> {
        return this.map { group -> group.name }
            .toMutableList()
            .apply {
                add(0, resourceProvider.getString(R.string.root))
            }
    }

    private fun getSelectedProject(): ProjectEntry? {
        val data = this.data ?: return null

        val name = state.value.selectedProject

        return data.projects.findProjectByName(name)
    }

    private fun getSelectedGroup(): Group? {
        val data = this.data ?: return null

        val name = state.value.selectedGroup

        return if (name == resourceProvider.getString(R.string.root)) {
            null
        } else {
            data.groups.findGroupByName(name)
        }
    }

    private fun List<Group>.findGroupByName(name: String): Group? {
        return this.firstOrNull { group -> group.name == name }
    }

    private fun List<ProjectEntry>.findProjectByName(name: String): ProjectEntry? {
        return this.firstOrNull { project -> project.name == name }
    }

    private fun createInitialTopBarState(): TopBarState {
        return TopBarState(
            title = resourceProvider.getString(R.string.upload_test),
            isBackVisible = true
        )
    }

    private fun createUploadSuccessDialog(): MessageDialogState {
        return MessageDialogState(
            title = null,
            message = resourceProvider.getString(R.string.test_successfully_uploaded_message),
            isCancellable = false,
            actionButton = MessageDialogButton.ActionButton(
                title = resourceProvider.getString(R.string.ok),
                actionId = ACTION_OK
            )
        )
    }

    private fun newInitialState(): UploadTestState {
        return UploadTestState(terminalState = TerminalState.Loading)
    }

    companion object {
        private const val ACTION_OK = 1
    }
}