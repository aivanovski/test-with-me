package com.github.aivanovski.testwithme.web.data.repository

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.web.data.database.FlowRunDao
import com.github.aivanovski.testwithme.web.data.database.FlowDao
import com.github.aivanovski.testwithme.web.entity.exception.AppException
import com.github.aivanovski.testwithme.web.entity.FlowRun
import com.github.aivanovski.testwithme.web.entity.Uid

class FlowRunRepository(
    private val flowRunDao: FlowRunDao,
    private val flowDao: FlowDao
) {

    fun getAll(): Either<AppException, List<FlowRun>> {
        return Either.Right(flowRunDao.getAll())
    }

    fun getByUserUid(
        userUid: Uid
    ): Either<AppException, List<FlowRun>> = either {
        flowRunDao.getAll()
            .filter { item -> item.userUid == userUid }
    }

    fun getByProjectUid(
        projectUid: Uid
    ): Either<AppException, List<FlowRun>> = either {
        val flowUids = flowDao.getAll()
            .filter { flow -> flow.projectUid == projectUid }
            .map { flow -> flow.uid }
            .toSet()

        val flowRuns = flowRunDao.getAll()
            .filter { flowRun -> flowRun.flowUid in flowUids }

        flowRuns
    }

    fun add(flowRun: FlowRun): Either<AppException, FlowRun> {
        return Either.Right(flowRunDao.insert(flowRun))
    }
}