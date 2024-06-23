package com.github.aivanovski.testwithme.web.entity

data class Flow(
    val id: Long? = null,
    val uid: Uid,
    val projectUid: Uid,
    val name: String,
    val path: String
)