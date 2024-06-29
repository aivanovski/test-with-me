package com.github.aivanovski.testwithme.android.data.repository

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.android.data.api.ApiClient
import com.github.aivanovski.testwithme.android.entity.Project
import com.github.aivanovski.testwithme.android.entity.exception.AppException

class ProjectRepository(
    private val api: ApiClient
) {

    suspend fun getProjectByUid(
        projectUid: String
    ): Either<AppException, Project?> = either {
        val projects = api.getProjects().bind()

        projects
            .firstOrNull { project -> project.uid == projectUid }
    }
}