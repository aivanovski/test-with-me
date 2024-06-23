package com.github.aivanovski.testwithme.web.entity

data class ExecutionStat(
    val id: Long? = null,
    val flowUid: Uid,
    val userUid: Uid,
    val executionTime: Timestamp,
    val isSuccess: Boolean,
    val result: String
)