package com.github.aivanovski.testwithme.web.entity

data class FlowRun(
    val id: Long? = null,
    val uid: CompoundUid,
    val flowUid: Uid,
    val userUid: Uid,
    val timestamp: Timestamp,
    val durationInMillis: Long,
    val isSuccess: Boolean,
    val result: String
)