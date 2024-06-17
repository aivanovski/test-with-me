package com.github.aivanovski.testwithme.web.extensions

import arrow.core.Either
import com.github.aivanovski.testwithme.web.entity.ErrorResponse
import com.github.aivanovski.testwithme.web.entity.exception.AppException
import com.github.aivanovski.testwithme.extensions.unwrapError
import io.ktor.http.HttpStatusCode
import kotlin.Exception

fun Exception.toErrorResponse(): ErrorResponse {
    return ErrorResponse.fromException(
        status = HttpStatusCode.BadRequest, // TODO: could depend on exception
        exception = if (this is AppException) {
            this
        } else {
            AppException(this)
        }
    )
}

fun <Value> Either<Exception, Value>.toErrorResponse(): Either.Left<ErrorResponse> {
    return Either.Left(this.unwrapError().toErrorResponse())
}