package com.github.aivanovski.testwithme.web.data.repository

import arrow.core.Either
import com.github.aivanovski.testwithme.web.entity.Project
import com.github.aivanovski.testwithme.web.entity.Uid
import com.github.aivanovski.testwithme.web.entity.exception.AppException

interface ProjectRepository {
    fun getAll(): Either<AppException, List<Project>>
    fun getByUserUid(userUid: Uid): Either<AppException, List<Project>>
    fun findByUid(uid: Uid): Either<AppException, Project?>
    fun add(project: Project): Either<AppException, Project>
}