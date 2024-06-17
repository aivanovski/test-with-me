package com.github.aivanovski.testwithme.web.entity.exception

import com.github.aivanovski.testwithme.web.presentation.Errors.ENTITY_NOT_FOUND

class EntityNotFoundException(
    entity: String,
    key: String,
    value: String
) : AppException(ENTITY_NOT_FOUND.format(entity, key, value))