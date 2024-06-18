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
                uid = "UID:com.ivanovsky.passnotes:about_navigate-back.yaml",
                projectUid = "UID:com.ivanovsky.passnotes",
                name = "Navigate back from about screen",
                resource = "tests/about_navigate-back.yaml"
            ),
            Flow(
                uid = "UID:com.ivanovsky.passnotes:about_open-screen.yaml",
                projectUid = "UID:com.ivanovsky.passnotes",
                name = "Open about screen",
                resource = "tests/about_open-screen.yaml"
            ),
            Flow(
                uid = "UID:com.ivanovsky.passnotes:new_db_create-new.yaml",
                projectUid = "UID:com.ivanovsky.passnotes",
                name = "Create new database",
                resource = "tests/new_db_create-new.yaml"
            ),
            Flow(
                uid = "UID:com.ivanovsky.passnotes:unlock_open-database.yaml",
                projectUid = "UID:com.ivanovsky.passnotes",
                name = "Unlock database",
                resource = "tests/unlock_open-database.yaml"
            ),
            Flow(
                uid = "UID:com.ivanovsky.passnotes:unlock_remove-file.yaml",
                projectUid = "UID:com.ivanovsky.passnotes",
                name = "Remove selected database file",
                resource = "tests/unlock_remove-file.yaml"
            ),
            Flow(
                uid = "UID:com.ivanovsky.passnotes:all.yaml",
                projectUid = "UID:com.ivanovsky.passnotes",
                name = "All",
                resource = "tests/all.yaml"
            )
        )
    }
}