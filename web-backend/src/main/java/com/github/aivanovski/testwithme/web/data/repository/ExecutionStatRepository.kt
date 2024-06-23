package com.github.aivanovski.testwithme.web.data.repository

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.web.data.database.ExecutionStatDao
import com.github.aivanovski.testwithme.web.data.database.FlowDao
import com.github.aivanovski.testwithme.web.entity.exception.AppException
import com.github.aivanovski.testwithme.web.entity.ExecutionStat
import com.github.aivanovski.testwithme.web.entity.Uid

class ExecutionStatRepository(
    private val statDao: ExecutionStatDao,
    private val flowDao: FlowDao
) {

    fun getAll(): Either<AppException, List<ExecutionStat>> {
        return Either.Right(statDao.getAll())
    }

    fun getByUserUid(
        userUid: Uid
    ): Either<AppException, List<ExecutionStat>> = either {
        statDao.getAll()
            .filter { stat -> stat.userUid == userUid }
    }

    fun getByProjectUid(
        projectUid: Uid
    ): Either<AppException, List<ExecutionStat>> = either {
        val flowUids = flowDao.getAll()
            .filter { flow -> flow.projectUid == projectUid }
            .map { flow -> flow.uid }
            .toSet()

        val stats = statDao.getAll()
            .filter { stat -> stat.flowUid in flowUids }

        stats
    }

    fun add(stat: ExecutionStat): Either<AppException, ExecutionStat> {
        return Either.Right(statDao.insert(stat))
    }
}