package com.github.aivanovski.testwithme.web.data.repository

import arrow.core.Either
import com.github.aivanovski.testwithme.web.entity.User
import com.github.aivanovski.testwithme.web.entity.exception.AppException

interface UserRepository {
    fun getUserUid(uid: String): Either<AppException, User>
    fun getUserByName(username: String): Either<AppException, User>
}