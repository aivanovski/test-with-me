package com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.viewModel

import androidx.compose.runtime.Immutable
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.FlowStatsCellModel

@Immutable
class FlowStatsCellViewModel(
    override val model: FlowStatsCellModel
) : BaseCellViewModel(model)