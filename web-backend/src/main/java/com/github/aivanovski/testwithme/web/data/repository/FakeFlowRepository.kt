package com.github.aivanovski.testwithme.web.data.repository

import arrow.core.Either
import com.github.aivanovski.testwithme.web.entity.Flow
import com.github.aivanovski.testwithme.web.entity.User
import com.github.aivanovski.testwithme.web.entity.exception.AppException

class FakeFlowRepository : FlowRepository {

    override fun getFlows(user: User): Either<AppException, List<Flow>> {

        return Either.Right(FLOWS)
    }

    companion object {

        private val FLOWS = listOf(
            Flow(
                uid = "UID:com.ivanovsky.passnotes:unlock",
                projectUid = "UID:com.ivanovsky.passnotes",
                name = "Unlock database",
                resource = "tests/unlock.yaml"
            )
        )
    }
}