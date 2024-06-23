package com.github.aivanovski.testwithme.web.entity

data class User(
    val id: Long? = null,
    val uid: Uid,
    val name: String,
    val password: String
)