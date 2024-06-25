package com.github.aivanovski.testwithme.web.presentation.controller

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.web.api.FlowRunsItemDto
import com.github.aivanovski.testwithme.web.api.request.PostFlowRunRequest
import com.github.aivanovski.testwithme.web.api.response.PostFlowRunResponse
import com.github.aivanovski.testwithme.web.api.response.FlowRunsResponse
import com.github.aivanovski.testwithme.web.data.repository.FlowRunRepository
import com.github.aivanovski.testwithme.web.data.repository.FlowRepository
import com.github.aivanovski.testwithme.web.data.repository.ProjectRepository
import com.github.aivanovski.testwithme.web.entity.ErrorResponse
import com.github.aivanovski.testwithme.web.entity.Timestamp
import com.github.aivanovski.testwithme.web.entity.Uid
import com.github.aivanovski.testwithme.web.entity.User
import com.github.aivanovski.testwithme.web.entity.FlowRun
import com.github.aivanovski.testwithme.web.entity.exception.FlowNotFoundByUidException
import com.github.aivanovski.testwithme.web.entity.exception.InvalidRequestField
import com.github.aivanovski.testwithme.web.extensions.toErrorResponse

class FlowRunController(
    private val flowRepository: FlowRepository,
    private val projectRepository: ProjectRepository,
    private val flowRunRepository: FlowRunRepository
) {

    fun getFlowRuns(
        user: User
    ): Either<ErrorResponse, FlowRunsResponse> = either {
        val userFlows = projectRepository.getByUserUid(user.uid)
            .mapLeft { error -> error.toErrorResponse() }
            .bind()

        val allStats = flowRunRepository.getAll()
            .mapLeft { error -> error.toErrorResponse() }
            .bind()

        val userFlowUids = userFlows.map { flow -> flow.uid }
        val filteredStats = allStats.filter { stat ->
            stat.userUid == user.uid || stat.flowUid in userFlowUids
        }

        val items = filteredStats.map { flowRun ->
            FlowRunsItemDto(
                flowUid = flowRun.flowUid.toString(),
                userUid = flowRun.userUid.toString(),
                finishedAt = flowRun.timestamp.formatForTransport(),
                finishedAtTimestamp = flowRun.timestamp.milliseconds,
                durationInMillis = flowRun.durationInMillis,
                isSuccess = flowRun.isSuccess
            )
        }

        FlowRunsResponse(items)
    }

    fun add(
        user: User,
        request: PostFlowRunRequest
    ): Either<ErrorResponse, PostFlowRunResponse> = either {
        val flowUid = Uid.parse(request.flowId).getOrNull()
            ?: raise(InvalidRequestField("flowId").toErrorResponse())

        val flow = flowRepository.findByFlowUid(flowUid)
            .mapLeft { error -> error.toErrorResponse() }
            .bind()
            ?: raise(FlowNotFoundByUidException(flowUid.toString()).toErrorResponse())

        val flowRun = FlowRun(
            flowUid = flow.uid,
            userUid = user.uid,
            timestamp = Timestamp.now(),
            isSuccess = request.isSuccess,
            durationInMillis = request.durationInMillis,
            result = request.result
        )

        flowRunRepository.add(flowRun)
            .mapLeft { error -> error.toErrorResponse() }
            .bind()

        PostFlowRunResponse(true)
    }
}