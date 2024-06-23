package com.github.aivanovski.testwithme.web.entity

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.web.entity.exception.AppException
import java.util.UUID

data class Uid(
    val uuid: UUID
) {
    override fun toString(): String {
        return uuid.toString()
    }

    companion object {

        fun fromLong(least: Long, most: Long): Uid {
            return Uid(UUID(least, most))
        }

        fun userUid(value: Long): Uid = fromLong(100, value)

        fun projectUid(value: Long): Uid = fromLong(1000, value)

        fun flowUid(value: Long): Uid = fromLong(10_000, value)

        fun fromString(value: String): Uid {
            return Uid(UUID.fromString(value))
        }

        fun parse(value: String): Either<AppException, Uid> = either {
            try {
                Uid(UUID.fromString(value))
            } catch (exception: IllegalArgumentException) {
                raise(AppException(exception))
            }
        }
    }
}