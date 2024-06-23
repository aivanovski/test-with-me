package com.github.aivanovski.testwithme.web.api

import kotlinx.serialization.Serializable

@Serializable
data class ExecutionStatisticItemDto(
    val flowUid: String,
    val userUid: String,
    val executionTime: String,
    val isSuccess: Boolean
)