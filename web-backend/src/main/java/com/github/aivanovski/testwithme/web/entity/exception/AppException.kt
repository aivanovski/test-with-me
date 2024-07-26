package com.github.aivanovski.testwithme.web.entity.exception

open class AppException(
    message: String?,
    cause: Exception?
) : Exception(message, cause) {

    constructor(message: String) : this(message, null)
    constructor(cause: Exception) : this(null, cause)
}