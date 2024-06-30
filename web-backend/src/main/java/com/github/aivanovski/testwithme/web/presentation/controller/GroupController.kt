package com.github.aivanovski.testwithme.web.presentation.controller

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.web.api.GroupsItemDto
import com.github.aivanovski.testwithme.web.api.response.GroupsResponse
import com.github.aivanovski.testwithme.web.data.repository.GroupRepository
import com.github.aivanovski.testwithme.web.entity.ErrorResponse
import com.github.aivanovski.testwithme.web.entity.User
import com.github.aivanovski.testwithme.web.extensions.transformError

class GroupController(
    private val groupRepository: GroupRepository
) {

    fun getGroups(
        user: User
    ): Either<ErrorResponse, GroupsResponse> = either {
        val groups = groupRepository.getByUserUid(user.uid)
            .transformError()
            .bind()

        GroupsResponse(
            groups = groups.map { group ->
                GroupsItemDto(
                    id = group.uid.toString(),
                    parentId = group.parentUid?.toString(),
                    projectId = group.projectUid.toString(),
                    name = group.name
                )
            }
        )
    }
}