package com.github.aivanovski.testwithme.web.data.repository

import arrow.core.Either
import com.github.aivanovski.testwithme.web.entity.Project
import com.github.aivanovski.testwithme.web.entity.exception.AppException

interface ProjectRepository {
    fun getProjects(): Either<AppException, List<Project>>
}