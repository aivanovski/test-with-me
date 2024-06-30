package com.github.aivanovski.testwithme.web.presentation.controller

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.web.data.repository.FlowRepository
import com.github.aivanovski.testwithme.web.entity.ErrorResponse
import com.github.aivanovski.testwithme.web.entity.User
import com.github.aivanovski.testwithme.web.entity.exception.InvalidParameterException
import com.github.aivanovski.testwithme.web.extensions.toErrorResponse
import com.github.aivanovski.testwithme.web.presentation.routes.Api.ID
import com.github.aivanovski.testwithme.web.api.FlowItemDto
import com.github.aivanovski.testwithme.web.api.FlowsItemDto
import com.github.aivanovski.testwithme.web.api.response.FlowResponse
import com.github.aivanovski.testwithme.web.api.response.FlowsResponse
import com.github.aivanovski.testwithme.web.data.file.FlowContentProvider
import com.github.aivanovski.testwithme.web.entity.Uid
import com.github.aivanovski.testwithme.web.entity.exception.FlowNotFoundByUidException
import com.github.aivanovski.testwithme.web.extensions.encodeToBase64

class FlowController(
    private val flowRepository: FlowRepository,
    private val flowContentProvider: FlowContentProvider
) {

    fun getFlow(
        user: User,
        flowUid: String
    ): Either<ErrorResponse, FlowResponse> = either {
        val uid = Uid.parse(flowUid).getOrNull()
            ?: raise(InvalidParameterException(ID).toErrorResponse())

        val allFlows = flowRepository.getFlowsByUserUid(user.uid)
            .mapLeft { error -> error.toErrorResponse() }
            .bind()

        val flows = allFlows.filter { flow -> flow.uid == uid }
        if (flows.isEmpty()) {
            raise(FlowNotFoundByUidException(flowUid).toErrorResponse())
        }

        val flow = flows.first()
        val rawContent = flowContentProvider.getContent(flow.path)
            .mapLeft { error -> error.toErrorResponse() }
            .bind()

        FlowResponse(
            FlowItemDto(
                id = flow.uid.toString(),
                projectId = flow.projectUid.toString(),
                groupId = flow.groupUid?.toString(),
                name = flow.name,
                base64Content = rawContent.encodeToBase64()
            )
        )
    }

    fun getFlows(
        user: User
    ): Either<ErrorResponse, FlowsResponse> = either {
        val flows = flowRepository.getFlowsByUserUid(user.uid)
            .mapLeft { error -> error.toErrorResponse() }
            .bind()

        val items = flows.map { flow ->
            FlowsItemDto(
                id = flow.uid.toString(),
                projectId = flow.projectUid.toString(),
                groupId = flow.groupUid?.toString(),
                name = flow.name
            )
        }

        FlowsResponse(items)
    }
}