package com.github.aivanovski.testwithme.web.presentation.controller

import arrow.core.Either
import com.github.aivanovski.testwithme.web.domain.service.AuthService
import com.github.aivanovski.testwithme.web.entity.Credentials
import com.github.aivanovski.testwithme.web.entity.ErrorResponse
import com.github.aivanovski.testwithme.web.entity.exception.InvalidCredentialsException
import com.github.aivanovski.testwithme.web.api.request.LoginRequest
import com.github.aivanovski.testwithme.web.api.response.LoginResponse
import io.ktor.http.HttpStatusCode

class LoginController(
    private val authService: AuthService
) {

    fun login(request: LoginRequest): Either<ErrorResponse, LoginResponse> {
        val credentials = request.toCredentials()

        if (!authService.isCredentialsValid(credentials)) {
            return Either.Left(
                ErrorResponse.fromException(
                    status = HttpStatusCode.Unauthorized,
                    exception = InvalidCredentialsException(),
                )
            )
        }

        val token = authService.getOrCreateToken(credentials)

        return Either.Right(LoginResponse(token))
    }

    private fun LoginRequest.toCredentials(): Credentials {
        return Credentials(
            username = username,
            password = password
        )
    }
}