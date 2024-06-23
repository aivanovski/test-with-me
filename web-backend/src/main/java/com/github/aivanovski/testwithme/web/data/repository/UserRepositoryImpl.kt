package com.github.aivanovski.testwithme.web.data.repository

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.web.data.database.UserDao
import com.github.aivanovski.testwithme.web.entity.User
import com.github.aivanovski.testwithme.web.entity.exception.AppException
import com.github.aivanovski.testwithme.web.entity.exception.UserNotFoundByNameException

class UserRepositoryImpl(
    private val dao: UserDao
) : UserRepository {

    override fun getUsers(): Either<AppException, List<User>> {
        return Either.Right(dao.getAll())
    }

    override fun getUserByName(
        name: String
    ): Either<AppException, User> = either {
        val users = dao.findByName(name)
        if (users.isEmpty()) {
            raise(UserNotFoundByNameException(name))
        }

        users.first()
    }

    override fun add(user: User): Either<AppException, User> {
        return Either.Right(dao.insert(user))
    }
}