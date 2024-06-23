package com.github.aivanovski.testwithme.web.entity.exception

import com.github.aivanovski.testwithme.web.entity.Flow
import com.github.aivanovski.testwithme.web.entity.Project
import com.github.aivanovski.testwithme.web.entity.Uid
import com.github.aivanovski.testwithme.web.entity.User
import com.github.aivanovski.testwithme.web.presentation.Errors.ENTITY_NOT_FOUND

open class EntityNotFoundException(
    entity: String,
    key: String,
    value: String
) : AppException(ENTITY_NOT_FOUND.format(entity, key, value))

class UserNotFoundByNameException(
    name: String
) : EntityNotFoundException(
    entity = User::class.java.simpleName,
    key = "name",
    value = name
)

class FlowNotFoundByUidException(
    uid: String
) : EntityNotFoundException(
    entity = Flow::class.java.simpleName,
    key = "uid",
    value = uid
)

class ProjectNotFoundByUidException(
    uid: Uid
) : EntityNotFoundException(
    entity = Project::class.java.simpleName,
    key = "uid",
    value = uid.toString()
)