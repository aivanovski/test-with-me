package com.github.aivanovski.testwithme.android.presentation.core.cells.model

import androidx.compose.ui.graphics.Color
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.model.CornersShape

data class TextWithChipCellModel(
    override val id: String,
    val text: String,
    val chipText: String,
    val chipTextColor: Color,
    val chipColor: Color,
    val shape: CornersShape,
) : BaseCellModel(id)