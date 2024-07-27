package com.github.aivanovski.testwithme.web.domain.usecases

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.utils.StringUtils
import com.github.aivanovski.testwithme.web.data.repository.GroupRepository
import com.github.aivanovski.testwithme.web.data.repository.ProjectRepository
import com.github.aivanovski.testwithme.web.entity.Group
import com.github.aivanovski.testwithme.web.entity.GroupPath
import com.github.aivanovski.testwithme.web.entity.Project
import com.github.aivanovski.testwithme.web.entity.Uid
import com.github.aivanovski.testwithme.web.entity.User
import com.github.aivanovski.testwithme.web.entity.exception.AppException
import com.github.aivanovski.testwithme.web.entity.exception.EntityNotFoundByNameException
import com.github.aivanovski.testwithme.web.entity.exception.EntityNotFoundByUidException
import com.github.aivanovski.testwithme.web.entity.exception.InvalidAccessException
import com.github.aivanovski.testwithme.web.entity.exception.ParsingException
import com.github.aivanovski.testwithme.web.extensions.aggregateGroupsByParent
import java.util.LinkedList

class ResolvePathUseCase(
    private val projectRepository: ProjectRepository,
    private val groupRepository: GroupRepository
) {

    fun resolveProjectAndGroup(
        path: String?,
        projectUid: String?,
        groupUid: String?,
        user: User
    ): Either<AppException, Pair<Project, Group?>> =
        either {
            when {
                !path.isNullOrBlank() -> {
                    val (project, groups) = resolveProjectAndGroupsByPath(
                        path = path,
                        user = user
                    ).bind()

                    project to groups.lastOrNull()
                }

                !projectUid.isNullOrBlank() -> {
                    val project = getProject(projectUid ?: StringUtils.EMPTY, user).bind()
                    val group = getGroup(groupUid, project.uid).bind()

                    project to group
                }

                else -> {
                    raise(AppException("Unable to resolve"))
                }
            }
        }

    private fun getProject(
        projectUid: String,
        user: User
    ): Either<AppException, Project> =
        either {
            val uid = Uid.parse(projectUid).getOrNull()
                ?: raise(ParsingException("Failed to parse uid: $projectUid"))

            val project = projectRepository.findByUid(uid).bind()
                ?: raise(EntityNotFoundByUidException(Project::class, uid))

            if (project.userUid != user.uid) {
                raise(InvalidAccessException("Unable to access the project: ${project.name}"))
            }

            project
        }

    private fun getGroup(
        groupUid: String?,
        projectUid: Uid
    ): Either<AppException, Group?> =
        either {
            if (groupUid.isNullOrBlank()) {
                return@either null
            }

            val uid = Uid.parse(groupUid).getOrNull()
                ?: raise(ParsingException("Failed to parse uid: $groupUid"))

            val group = groupRepository.findByUid(uid).bind()
                ?: raise(EntityNotFoundByUidException(Group::class, uid))

            if (group.projectUid != projectUid) {
                raise(InvalidAccessException("Failed to access the group: ${group.name}"))
            }

            group
        }

    fun resolveProjectAndGroupsByPath(
        path: String,
        user: User
    ): Either<AppException, GroupPath> =
        either {
            val values = path
                .split("/")
                .mapNotNull { value ->
                    if (value.isNotBlank()) {
                        value.trim()
                    } else {
                        null
                    }
                }

            if (values.isEmpty()) {
                raise(AppException("Failed to parse path: $path"))
            }

            val projectName = values.first()
            val groupNames = if (values.size > 1) {
                values.subList(1, values.size)
            } else {
                emptyList()
            }

            val projects = projectRepository.getByUserUid(user.uid).bind()
            val project = resolveProjectByName(
                name = projectName,
                projects = projects
            ).bind()

            val groups = if (groupNames.isNotEmpty()) {
                val groups = groupRepository.getByUserUid(user.uid)
                    .bind()
                    .filter { group -> group.projectUid == project.uid }

                resolveGroupsByName(groupNames, groups).bind()
            } else {
                emptyList()
            }

            GroupPath(
                project = project,
                groups = groups
            )
        }

    private fun resolveProjectByName(
        name: String,
        projects: List<Project>
    ): Either<AppException, Project> =
        either {
            val projectsByName = projects.filter { project -> project.name == name }

            when {
                projectsByName.isEmpty() -> {
                    raise(EntityNotFoundByNameException(Project::class, name))
                }

                projectsByName.size > 1 -> {
                    raise(AppException("Failed to resolve project by name: $name"))
                }

                else -> {
                    projects.first()
                }
            }
        }

    private fun resolveGroupByName(
        name: String,
        groups: List<Group>
    ): Either<AppException, Group> =
        either {
            val groupsByName = groups.filter { group -> group.name == name }

            when {
                groupsByName.isEmpty() -> {
                    raise(EntityNotFoundByNameException(Group::class, name))
                }

                groupsByName.size > 1 -> {
                    raise(AppException("Failed to resolve group by name: $name"))
                }

                else -> {
                    groupsByName.first()
                }
            }
        }

    private fun resolveGroupsByName(
        names: List<String>,
        groups: List<Group>
    ): Either<AppException, List<Group>> =
        either {
            val result = mutableListOf<Group>()
            val queue = LinkedList<String>()
                .apply {
                    addAll(names)
                }

            val parentToChildrenMap = groups.aggregateGroupsByParent()

            var currentGroupUid: String? = null
            while (queue.isNotEmpty()) {
                val name = queue.removeFirst()
                val children = parentToChildrenMap[currentGroupUid] ?: emptyList()
                val group = resolveGroupByName(name, children).bind()
                result.add(group)
                currentGroupUid = group.uid.toString()
            }

            result
        }
}