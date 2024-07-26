package com.github.aivanovski.testwithme.web.entity.exception

import com.github.aivanovski.testwithme.web.presentation.Errors.INVALID_CREDENTIALS
import com.github.aivanovski.testwithme.web.presentation.Errors.INVALID_TOKEN
import com.github.aivanovski.testwithme.web.presentation.Errors.TOKEN_IS_EXPIRED

sealed class AuthException(message: String) : AppException(message)

class InvalidTokenException : AuthException(INVALID_TOKEN)
class ExpiredTokenException : AuthException(TOKEN_IS_EXPIRED)
class InvalidCredentialsException : AuthException(INVALID_CREDENTIALS)

