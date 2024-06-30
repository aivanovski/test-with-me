package com.github.aivanovski.testwithme.web.entity

import java.util.UUID

data class CompoundUid(
    val uid: String
) {

    override fun toString(): String {
        return uid
    }

    companion object {
        fun generateFrom(
            flowUid: Uid,
            userUid: Uid
        ): CompoundUid {
            val uid = UUID.randomUUID().toString()
            return CompoundUid(
                uid = "$flowUid:$userUid:$uid"
            )
        }
    }
}