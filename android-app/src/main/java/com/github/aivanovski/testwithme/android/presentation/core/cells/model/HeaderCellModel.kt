package com.github.aivanovski.testwithme.android.presentation.core.cells.model

import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellModel

data class HeaderCellModel(
    override val id: String,
    val title: String,
    val isIconVisible: Boolean
) : BaseCellModel(id)