package com.github.aivanovski.testwithme.web.data.repository

import arrow.core.Either
import com.github.aivanovski.testwithme.web.data.database.ProjectDao
import com.github.aivanovski.testwithme.web.entity.Project
import com.github.aivanovski.testwithme.web.entity.Uid
import com.github.aivanovski.testwithme.web.entity.exception.AppException

class ProjectRepositoryImpl(
    private val dao: ProjectDao
) : ProjectRepository {

    override fun getAll(): Either<AppException, List<Project>> {
        return Either.Right(dao.getAll())
    }

    override fun getByUserUid(
        userUid: Uid
    ): Either<AppException, List<Project>> {
        return Either.Right(dao.getByUserUid(userUid))
    }

    override fun findByUid(
        uid: Uid
    ): Either<AppException, Project?> {
        return Either.Right(dao.getByUid(uid))
    }

    override fun add(project: Project): Either<AppException, Project> {
        return Either.Right(dao.insert(project))
    }
}