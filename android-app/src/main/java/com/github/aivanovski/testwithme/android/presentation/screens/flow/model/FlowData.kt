package com.github.aivanovski.testwithme.android.presentation.screens.flow.model

import com.github.aivanovski.testwithme.android.entity.FlowRun
import com.github.aivanovski.testwithme.android.entity.FlowWithSteps
import com.github.aivanovski.testwithme.android.entity.Project

data class FlowData(
    val flow: FlowWithSteps,
    val project: Project,
    val executions: List<FlowRun>
)