package com.github.aivanovski.testwithme.web.presentation.controller

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.web.api.ExecutionStatisticItemDto
import com.github.aivanovski.testwithme.web.api.request.PostExecutionStatRequest
import com.github.aivanovski.testwithme.web.api.response.PostExecutionStatisticResponse
import com.github.aivanovski.testwithme.web.api.response.ExecutionStatisticsResponse
import com.github.aivanovski.testwithme.web.data.repository.ExecutionStatRepository
import com.github.aivanovski.testwithme.web.data.repository.FlowRepository
import com.github.aivanovski.testwithme.web.data.repository.ProjectRepository
import com.github.aivanovski.testwithme.web.entity.ErrorResponse
import com.github.aivanovski.testwithme.web.entity.Timestamp
import com.github.aivanovski.testwithme.web.entity.Uid
import com.github.aivanovski.testwithme.web.entity.User
import com.github.aivanovski.testwithme.web.entity.ExecutionStat
import com.github.aivanovski.testwithme.web.entity.exception.FlowNotFoundByUidException
import com.github.aivanovski.testwithme.web.entity.exception.InvalidRequestField
import com.github.aivanovski.testwithme.web.extensions.toErrorResponse

class ExecutionStatController(
    private val flowRepository: FlowRepository,
    private val projectRepository: ProjectRepository,
    private val executionRepository: ExecutionStatRepository
) {

    fun getExecutionStats(
        user: User
    ): Either<ErrorResponse, ExecutionStatisticsResponse> = either {
        val userFlows = projectRepository.getByUserUid(user.uid)
            .mapLeft { error -> error.toErrorResponse() }
            .bind()

        val allStats = executionRepository.getAll()
            .mapLeft { error -> error.toErrorResponse() }
            .bind()

        val userFlowUids = userFlows.map { flow -> flow.uid }
        val filteredStats = allStats.filter { stat ->
            stat.userUid == user.uid || stat.flowUid in userFlowUids
        }

        val items = filteredStats.map { stat ->
            ExecutionStatisticItemDto(
                flowUid = stat.flowUid.toString(),
                userUid = stat.userUid.toString(),
                executionTime = stat.executionTime.formatForTransport(),
                isSuccess = stat.isSuccess
            )
        }

        ExecutionStatisticsResponse(items)
    }

    fun add(
        user: User,
        request: PostExecutionStatRequest
    ): Either<ErrorResponse, PostExecutionStatisticResponse> = either {
        val flowUid = Uid.parse(request.flowId).getOrNull()
            ?: raise(InvalidRequestField("flowId").toErrorResponse())

        val flow = flowRepository.findByFlowUid(flowUid)
            .mapLeft { error -> error.toErrorResponse() }
            .bind()
            ?: raise(FlowNotFoundByUidException(flowUid.toString()).toErrorResponse())

        val stat = ExecutionStat(
            flowUid = flow.uid,
            userUid = user.uid,
            executionTime = Timestamp.now(),
            isSuccess = request.isSuccess,
            result = request.result
        )

        executionRepository.add(stat)
            .mapLeft { error -> error.toErrorResponse() }
            .bind()

        PostExecutionStatisticResponse(true)
    }
}