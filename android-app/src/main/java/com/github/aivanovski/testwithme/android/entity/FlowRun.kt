package com.github.aivanovski.testwithme.android.entity

data class FlowRun(
    val flowUid: String,
    val userUid: String,
    val executionTime: Long,
    val isSuccess: Boolean
)