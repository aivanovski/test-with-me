package com.github.aivanovski.testwithme.android.presentation.screens.flow.cells

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import com.github.aivanovski.testwithme.android.domain.resources.ResourceProvider
import com.github.aivanovski.testwithme.android.entity.FlowRun
import com.github.aivanovski.testwithme.android.entity.FlowWithSteps
import com.github.aivanovski.testwithme.android.entity.Project
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.model.HeaderCellModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.model.SpaceCellModel
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.ElementMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.QuarterMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.SmallMargin
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.FlowStatsCellModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.FlowTitleCellModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.HistoryItemCellModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FlowCellModelFactory(
    private val resourceProvider: ResourceProvider
) {

    fun createCellModels(
        project: Project,
        flow: FlowWithSteps,
        runs: List<FlowRun>
    ): List<BaseCellModel> {
        val models = mutableListOf<BaseCellModel>()

        val totalRuns = runs.size
        val passedRuns = runs.count { run -> run.isSuccess }
        val failedRuns = runs.count { run -> !run.isSuccess }
        val successRate = if (totalRuns != 0) {
            (passedRuns * 100 / totalRuns)
        } else {
            0
        }

        models.add(SpaceCellModel(height = ElementMargin))

        models.add(
            FlowTitleCellModel(
                id = CellId.TITLE,
                flowName = flow.entry.name,
                projectName = project.name
            )
        )

        models.add(SpaceCellModel(height = ElementMargin))

        models.add(
            FlowStatsCellModel(
                id = CellId.RUN_STATISTICS,
                total = totalRuns,
                passed = passedRuns,
                failed = failedRuns,
                rate = successRate
            )
        )

        if (runs.isNotEmpty()) {
            models.add(
                HeaderCellModel(
                    id = CellId.HISTORY_HEADER,
                    title = "History",
                    isIconVisible = true
                )
            )

            for ((idx, run) in runs.withIndex()) {
                if (idx > 0) {
                    models.add(SpaceCellModel(height = QuarterMargin))
                }

                models.add(
                    HistoryItemCellModel(
                        id = "run_$idx",
                        isSuccessful = run.isSuccess,
                        icon = if (run.isSuccess) {
                            Icons.Outlined.CheckCircle
                        } else {
                            Icons.Outlined.ErrorOutline
                        },
                        title = DATE_FORMAT.format(Date(run.executionTime)),
                        description = run.userUid
                    )
                )
            }
        }

        return models
    }

    object CellId {
        val TITLE = "title"
        val RUN_STATISTICS = "run-stats"
        val HISTORY_HEADER = "header"
    }

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    }
}