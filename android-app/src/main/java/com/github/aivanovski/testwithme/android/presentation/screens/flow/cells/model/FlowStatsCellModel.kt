package com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model

import androidx.compose.runtime.Immutable
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellModel

@Immutable
data class FlowStatsCellModel(
    override val id: String,
    val total: Int,
    val passed: Int,
    val failed: Int,
    val rate: Int
) : BaseCellModel(id)