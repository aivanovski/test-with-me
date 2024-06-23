package com.github.aivanovski.testwithme.web.entity

data class Project(
    val id: Long? = null,
    val uid: Uid,
    val userUid: Uid,
    val name: String
)