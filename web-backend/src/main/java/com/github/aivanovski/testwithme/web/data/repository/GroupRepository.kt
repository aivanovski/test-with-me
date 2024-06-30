package com.github.aivanovski.testwithme.web.data.repository

import arrow.core.Either
import arrow.core.flatten
import arrow.core.raise.either
import com.github.aivanovski.testwithme.web.data.database.GroupDao
import com.github.aivanovski.testwithme.web.data.database.ProjectDao
import com.github.aivanovski.testwithme.web.entity.Group
import com.github.aivanovski.testwithme.web.entity.Uid
import com.github.aivanovski.testwithme.web.entity.exception.AppException

class GroupRepository(
    private val groupDao: GroupDao,
    private val projectDao: ProjectDao
) {

    fun getByUserUid(
        userUid: Uid
    ): Either<AppException, List<Group>> = either {
        projectDao.getByUserUid(userUid)
            .map { project ->
                groupDao.getByProjectUid(project.uid)
            }
            .flatten()
    }

    fun insert(
        group: Group
    ): Either<AppException, Group> = either {
        groupDao.insert(group)
    }
}