package com.github.aivanovski.testwithme.web.entity.exception

open class BadRequestException(
    message: String
) : AppException(message)

class InvalidRequestField(
    fieldName: String
) : BadRequestException(
    message = "Invalid request field: $fieldName"
)