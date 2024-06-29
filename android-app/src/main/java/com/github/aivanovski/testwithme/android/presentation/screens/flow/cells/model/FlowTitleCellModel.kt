package com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model

import androidx.compose.runtime.Immutable
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellModel

@Immutable
data class FlowTitleCellModel(
    override val id: String,
    val flowName: String,
    val projectName: String
) : BaseCellModel(id)