package com.github.aivanovski.testwithme.web.entity.exception

open class AppIoException(
    cause: Exception? = null,
    message: String? = null
) : AppException(cause, message)

class FileNotFoundException(
    path: String
) : AppIoException(
    message = "Failed to find file: %s".format(path)
)