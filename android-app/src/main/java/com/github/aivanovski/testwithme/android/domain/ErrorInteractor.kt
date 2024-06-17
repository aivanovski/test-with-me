package com.github.aivanovski.testwithme.android.domain

import com.github.aivanovski.testwithme.android.domain.resources.ResourceProvider

class ErrorInteractor(
    private val resourceProvider: ResourceProvider
) {

    fun getMessage(exception: Exception): String {
        return "Error has been occurred"
    }
}