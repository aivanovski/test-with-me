package com.github.aivanovski.testwithme.web.data.repository

import arrow.core.Either
import com.github.aivanovski.testwithme.web.entity.User
import com.github.aivanovski.testwithme.web.entity.exception.AppException
import com.github.aivanovski.testwithme.web.entity.exception.EntityNotFoundException

class FakeUserRepository : com.github.aivanovski.testwithme.web.data.repository.UserRepository {

    override fun getUserUid(uid: String): Either<AppException, User> {
        if (uid == com.github.aivanovski.testwithme.web.data.repository.FakeUserRepository.Companion.ADMIN.uid) {
            return Either.Right(com.github.aivanovski.testwithme.web.data.repository.FakeUserRepository.Companion.ADMIN)
        }

        return Either.Left(
            EntityNotFoundException(
                entity = User::class.java.simpleName,
                key = "uid",
                value = uid
            )
        )
    }

    override fun getUserByName(name: String): Either<AppException, User> {
        if (name == com.github.aivanovski.testwithme.web.data.repository.FakeUserRepository.Companion.ADMIN.name) {
            return Either.Right(com.github.aivanovski.testwithme.web.data.repository.FakeUserRepository.Companion.ADMIN)
        }

        return Either.Left(
            EntityNotFoundException(
                entity = User::class.java.simpleName,
                key = "name",
                value = name
            )
        )
    }

    companion object {
        private val ADMIN = User(
            uid = "uid/admin",
            name = "admin"
        )
    }
}