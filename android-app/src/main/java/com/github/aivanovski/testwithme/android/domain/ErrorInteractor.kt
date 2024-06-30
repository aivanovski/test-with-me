package com.github.aivanovski.testwithme.android.domain

import com.github.aivanovski.testwithme.android.domain.resources.ResourceProvider
import com.github.aivanovski.testwithme.android.entity.ErrorMessage

class ErrorInteractor(
    private val resourceProvider: ResourceProvider
) {

    fun formatMessage(exception: Exception): ErrorMessage {
        return ErrorMessage(
            message = "Error has been occurred", // TODO: string resource
            cause = exception
        )
    }
}