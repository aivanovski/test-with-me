package com.github.aivanovski.testwithme.web.api.request

import kotlinx.serialization.Serializable

@Serializable
data class PostExecutionStatRequest(
    val flowId: String,
    val isSuccess: Boolean,
    val result: String
)