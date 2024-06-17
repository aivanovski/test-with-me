package com.github.aivanovski.testwithme.web.data.repository

import arrow.core.Either
import com.github.aivanovski.testwithme.web.entity.Project
import com.github.aivanovski.testwithme.web.entity.exception.AppException

class FakeProjectRepository : ProjectRepository {
    override fun getProjects(): Either<AppException, List<Project>> {
        return Either.Right(listOf(KEEPASS_VAULT_PROJECT))
    }

    companion object {
        val KEEPASS_VAULT_PROJECT = Project(
            uid = "UID:com.ivanovsky.passnotes",
            name = "KeePassVault"
        )
    }
}