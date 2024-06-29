package com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellModel

data class HistoryItemCellModel(
    override val id: String,
    val isSuccessful: Boolean,
    val icon: ImageVector,
    val title: String,
    val description: String
) : BaseCellModel(id)