package com.github.aivanovski.testwithme.web.domain.usecases

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.web.data.repository.FlowRepository
import com.github.aivanovski.testwithme.web.data.repository.ProjectRepository
import com.github.aivanovski.testwithme.web.data.repository.UserRepository
import com.github.aivanovski.testwithme.web.entity.Flow
import com.github.aivanovski.testwithme.web.entity.Project
import com.github.aivanovski.testwithme.web.entity.Uid
import com.github.aivanovski.testwithme.web.entity.User
import com.github.aivanovski.testwithme.web.entity.exception.AppException

class InitializeDefaultDataUseCase(
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val flowRepository: FlowRepository
) {

    fun initializeDefaultDataIfNeed(): Either<AppException, Unit> = either {
        val allUsers = userRepository.getUsers().bind()
        var defaultUser = allUsers.firstOrNull { user -> user.uid == DEFAULT_USER.uid }
        if (defaultUser == null) {
            userRepository.add(DEFAULT_USER).bind()
        }

        val allProjects = projectRepository.getAll().bind()
        var defaultProject = allProjects.firstOrNull { project ->
            project.uid == DEFAULT_PROJECT.uid
        }
        if (defaultProject == null) {
            projectRepository.add(DEFAULT_PROJECT).bind()
        }

        val allFlowsMap = flowRepository.getFlowsByUserUid(DEFAULT_USER.uid).bind()
            .associateBy { flow -> flow.uid }

        for (flow in DEFAULT_FLOWS) {
            if (!allFlowsMap.containsKey(flow.uid)) {
                flowRepository.add(flow).bind()
            }
        }
    }

    companion object {

        private val USER_UID = Uid.userUid(1)
        private val PROJECT_UID = Uid.projectUid(1)

        private val DEFAULT_USER = User(
            uid = USER_UID,
            name = "admin",
            password = "abc123"
        )

        private val DEFAULT_PROJECT = Project(
            uid = PROJECT_UID,
            userUid = USER_UID,
            name = "KeePassVault"
        )

        private val DEFAULT_FLOWS = listOf(
            Flow(
                uid = Uid.flowUid(1),
                projectUid = PROJECT_UID,
                name = "Navigate back from about screen",
                path = "keepassvault/about_navigate-back.yaml"
            ),
            Flow(
                uid = Uid.flowUid(2),
                projectUid = PROJECT_UID,
                name = "Open about screen",
                path = "keepassvault/about_open-screen.yaml"
            ),
            Flow(
                uid = Uid.flowUid(3),
                projectUid = PROJECT_UID,
                name = "Create new database",
                path = "keepassvault/new_db_create-new.yaml"
            ),
            Flow(
                uid = Uid.flowUid(4),
                projectUid = PROJECT_UID,
                name = "Unlock database",
                path = "keepassvault/unlock_open-database.yaml"
            ),
            Flow(
                uid = Uid.flowUid(5),
                projectUid = PROJECT_UID,
                name = "Remove selected database file",
                path = "keepassvault/unlock_remove-file.yaml"
            ),
            Flow(
                uid = Uid.flowUid(6),
                projectUid = PROJECT_UID,
                name = "All",
                path = "keepassvault/all.yaml"
            )
        )
    }
}