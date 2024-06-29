package com.github.aivanovski.testwithme.android.entity.exception

import com.github.aivanovski.testwithme.android.entity.Project
import com.github.aivanovski.testwithme.android.entity.db.FlowEntry

open class DaoException(
    message: String? = null,
    cause: Exception? = null
) : AppException(message, cause)

open class FailedToFindEntityException(
    entityName: String,
    entityField: String,
    fieldValue: String
) : DaoException(
    message = "Unable to find entity %s: %s=%s".format(entityName, entityField, fieldValue),
    cause = null
)

class FailedToFindProjectByUidException(
    uid: String
) : FailedToFindEntityException(
    entityName = Project::class.java.simpleName,
    entityField = "uid",
    fieldValue = uid
)

class FailedToFindFlowByUidException(
    uid: String
) : FailedToFindEntityException(
    entityName = FlowEntry::class.java.simpleName,
    entityField = "uid",
    fieldValue = uid
)