package com.github.aivanovski.testwithme.web.presentation.controller

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.web.data.repository.ProjectRepository
import com.github.aivanovski.testwithme.web.entity.ErrorResponse
import com.github.aivanovski.testwithme.web.entity.User
import com.github.aivanovski.testwithme.web.extensions.toErrorResponse
import com.github.aivanovski.testwithme.web.api.response.ProjectsItemDto
import com.github.aivanovski.testwithme.web.api.response.ProjectsResponse

class ProjectController(
    private val projectRepository: ProjectRepository
) {

    fun getProjects(
        user: User
    ): Either<ErrorResponse, ProjectsResponse> = either {
        val projects = projectRepository.getByUserUid(user.uid)
            .mapLeft { error -> error.toErrorResponse() }
            .bind()

        val items = projects
            .map { project ->
                ProjectsItemDto(
                    uid = project.uid.toString(),
                    name = project.name
                )
            }

        ProjectsResponse(items)
    }
}