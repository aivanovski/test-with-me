package com.github.aivanovski.testwithme.web.presentation.controller

import arrow.core.Either
import com.github.aivanovski.testwithme.web.data.repository.ProjectRepository
import com.github.aivanovski.testwithme.web.entity.ErrorResponse
import com.github.aivanovski.testwithme.web.entity.User
import com.github.aivanovski.testwithme.web.extensions.toErrorResponse
import com.github.aivanovski.testwithme.extensions.unwrap
import com.github.aivanovski.testwithme.web.api.response.ProjectsItemDto
import com.github.aivanovski.testwithme.web.api.response.ProjectsResponse

class ProjectController(
    private val projectRepository: ProjectRepository
) {

    fun getProjects(
        user: User
    ): Either<ErrorResponse, ProjectsResponse> {
        val getProjects = projectRepository.getProjects()
        if (getProjects.isLeft()) {
            return getProjects.toErrorResponse()
        }

        val projects = getProjects.unwrap()
            .map { project ->
                ProjectsItemDto(
                    uid = project.uid,
                    name = project.name
                )
            }

        return Either.Right(ProjectsResponse(projects))
    }
}