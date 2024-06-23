package com.github.aivanovski.testwithme.web.extensions

import arrow.core.Either
import com.github.aivanovski.testwithme.web.entity.ErrorResponse
import com.github.aivanovski.testwithme.web.entity.exception.AppException
import com.github.aivanovski.testwithme.extensions.unwrapError
import com.github.aivanovski.testwithme.web.entity.exception.EntityNotFoundException
import com.github.aivanovski.testwithme.web.entity.exception.ExpiredTokenException
import com.github.aivanovski.testwithme.web.entity.exception.InvalidCredentialsException
import com.github.aivanovski.testwithme.web.entity.exception.InvalidParameterException
import com.github.aivanovski.testwithme.web.entity.exception.InvalidTokenException
import io.ktor.http.HttpStatusCode
import kotlin.Exception

fun Exception.toHttpStatus(): HttpStatusCode? {
    return when (this) {
        is InvalidTokenException -> HttpStatusCode.Unauthorized
        is ExpiredTokenException -> HttpStatusCode.Unauthorized
        is InvalidCredentialsException -> HttpStatusCode.Unauthorized
        is InvalidParameterException -> HttpStatusCode.BadRequest
        is EntityNotFoundException -> HttpStatusCode.NotFound
        else -> null
    }
}

fun Exception.toErrorResponse(): ErrorResponse {
    return ErrorResponse(
        status = this.toHttpStatus() ?: HttpStatusCode.BadRequest,
        exception = if (this is AppException) {
            this
        } else {
            AppException(this)
        },
        message = this.message
    )
}

fun <Value> Either<Exception, Value>.toErrorResponse(): Either.Left<ErrorResponse> {
    return Either.Left(this.unwrapError().toErrorResponse())
}