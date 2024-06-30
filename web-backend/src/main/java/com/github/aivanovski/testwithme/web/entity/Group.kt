package com.github.aivanovski.testwithme.web.entity

data class Group(
    val id: Long? = null,
    val uid: Uid,
    val parentUid: Uid?,
    val projectUid: Uid,
    val name: String
)