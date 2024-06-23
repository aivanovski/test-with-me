package com.github.aivanovski.testwithme.web.data.repository

import arrow.core.Either
import com.github.aivanovski.testwithme.web.entity.Flow
import com.github.aivanovski.testwithme.web.entity.Uid
import com.github.aivanovski.testwithme.web.entity.exception.AppException

interface FlowRepository {
    fun getFlowsByUserUid(userUid: Uid): Either<AppException, List<Flow>>
    fun add(flow: Flow): Either<AppException, Flow>
}