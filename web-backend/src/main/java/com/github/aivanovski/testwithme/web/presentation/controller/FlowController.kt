package com.github.aivanovski.testwithme.web.presentation.controller

import arrow.core.Either
import com.github.aivanovski.testwithme.web.data.repository.FlowRepository
import com.github.aivanovski.testwithme.web.entity.ErrorResponse
import com.github.aivanovski.testwithme.web.entity.Flow
import com.github.aivanovski.testwithme.web.entity.User
import com.github.aivanovski.testwithme.web.entity.exception.EntityNotFoundException
import com.github.aivanovski.testwithme.web.entity.exception.InvalidParameterException
import com.github.aivanovski.testwithme.web.extensions.toErrorResponse
import com.github.aivanovski.testwithme.web.presentation.routes.Api.ID
import com.github.aivanovski.testwithme.data.resources.ResourceProvider
import com.github.aivanovski.testwithme.extensions.unwrap
import com.github.aivanovski.testwithme.web.api.FlowItemDto
import com.github.aivanovski.testwithme.web.api.FlowsItemDto
import com.github.aivanovski.testwithme.web.api.response.FlowResponse
import com.github.aivanovski.testwithme.web.api.response.FlowsResponse
import io.ktor.http.HttpStatusCode
import java.util.Base64

class FlowController(
    private val flowRepository: FlowRepository,
    private val resourceProvider: ResourceProvider
) {

    fun getFlow(
        user: User,
        flowUid: String
    ): Either<ErrorResponse, FlowResponse> {
        if (flowUid.isEmpty()) {
            return newInvalidParameterResponse(ID)
        }

        val getFlowsResult = flowRepository.getFlows(user)
        if (getFlowsResult.isLeft()) {
            return getFlowsResult.toErrorResponse()
        }

        val flows = getFlowsResult.unwrap()
            .filter { flow -> flow.uid == flowUid }
        if (flows.isEmpty()) {
            return newFlowNotFoundResponse(flowUid)
        }

        val flow = flows.first()

        val getContent = resourceProvider.read(flow.resource)
        if (getContent.isLeft()) {
            return getContent.toErrorResponse()
        }

        val flowBytes = getContent.unwrap().toByteArray()
        val content = Base64.getEncoder().encode(flowBytes)
            .let { bytes -> String(bytes) }

        return Either.Right(
            FlowResponse(
                FlowItemDto(
                    uid = flow.uid,
                    projectUid = flow.projectUid,
                    name = flow.name,
                    base64Content = content
                )
            )
        )
    }

    fun getFlows(
        user: User
    ): Either<ErrorResponse, FlowsResponse> {
        val getFlowsResult = flowRepository.getFlows(user)
        if (getFlowsResult.isLeft()) {
            return getFlowsResult.toErrorResponse()
        }

        val flows = getFlowsResult.unwrap().map { item ->
            FlowsItemDto(
                uid = item.uid,
                projectUid = item.projectUid,
                name = item.name
            )
        }

        return Either.Right(FlowsResponse(flows))
    }

    private fun newInvalidParameterResponse(
        name: String
    ): Either.Left<ErrorResponse> {
        return Either.Left(
            ErrorResponse.fromException(
                status = HttpStatusCode.BadRequest,
                exception = InvalidParameterException(name)
            )
        )
    }

    private fun newFlowNotFoundResponse(
        uid: String
    ): Either.Left<ErrorResponse> {
        return Either.Left(
            ErrorResponse.fromException(
                status = HttpStatusCode.NotFound,
                exception = EntityNotFoundException(
                    Flow::class.java.simpleName,
                    "uid",
                    uid
                )
            )
        )
    }
}