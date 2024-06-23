package com.github.aivanovski.testwithme.web.data.repository

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.web.data.database.FlowDao
import com.github.aivanovski.testwithme.web.data.database.ProjectDao
import com.github.aivanovski.testwithme.web.entity.Flow
import com.github.aivanovski.testwithme.web.entity.Uid
import com.github.aivanovski.testwithme.web.entity.exception.AppException

class FlowRepositoryImpl(
    private val flowDao: FlowDao,
    private val projectDao: ProjectDao
) : FlowRepository {

    override fun findByFlowUid(
        uid: Uid
    ): Either<AppException, Flow?> = either {
        flowDao.getByUid(uid)
    }

    override fun getFlowsByUserUid(
        userUid: Uid
    ): Either<AppException, List<Flow>> = either {
        val projectUids = projectDao.getByUserUid(userUid)
            .map { project -> project.uid }
            .toSet()

        val flows = flowDao.getAll()
            .filter { flow -> flow.projectUid in projectUids }

        return Either.Right(flows)
    }

    override fun add(flow: Flow): Either<AppException, Flow> {
        return Either.Right(flowDao.insert(flow))
    }
}