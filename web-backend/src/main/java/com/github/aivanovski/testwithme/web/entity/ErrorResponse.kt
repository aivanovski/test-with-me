package com.github.aivanovski.testwithme.web.entity

import com.github.aivanovski.testwithme.web.entity.exception.AppException
import io.ktor.http.HttpStatusCode

data class ErrorResponse(
    val status: HttpStatusCode,
    val exception: AppException,
    val message: String?
) {

    companion object {

        fun fromException(
            status: HttpStatusCode,
            exception: AppException
        ): ErrorResponse =
            ErrorResponse(
                status = status,
                exception = exception,
                message = exception.message
            )
    }
}