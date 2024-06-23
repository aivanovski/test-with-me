package com.github.aivanovski.testwithme.web.data.repository

import arrow.core.Either
import com.github.aivanovski.testwithme.web.entity.User
import com.github.aivanovski.testwithme.web.entity.exception.AppException

interface UserRepository {
    fun getUsers(): Either<AppException, List<User>>
    fun getUserByName(name: String): Either<AppException, User>
    fun add(user: User): Either<AppException, User>
}