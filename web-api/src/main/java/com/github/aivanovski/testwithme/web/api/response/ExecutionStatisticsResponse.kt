package com.github.aivanovski.testwithme.web.api.response

import com.github.aivanovski.testwithme.web.api.ExecutionStatisticItemDto
import kotlinx.serialization.Serializable

@Serializable
data class ExecutionStatisticsResponse(
    val stats: List<ExecutionStatisticItemDto>
)