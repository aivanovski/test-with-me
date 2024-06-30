package com.github.aivanovski.testwithme.android.presentation.screens.flow.cells

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import com.github.aivanovski.testwithme.android.R
import com.github.aivanovski.testwithme.android.domain.resources.ResourceProvider
import com.github.aivanovski.testwithme.android.entity.FlowRun
import com.github.aivanovski.testwithme.android.entity.FlowWithSteps
import com.github.aivanovski.testwithme.android.entity.Project
import com.github.aivanovski.testwithme.android.entity.User
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.BaseCellViewModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.model.HeaderCellModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.model.SpaceCellModel
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.ElementMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.QuarterMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.SmallMargin
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.EmptyHistoryCellModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.FlowStatsCellModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.FlowTitleCellModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.HistoryItemCellModel
import com.github.aivanovski.testwithme.utils.StringUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class FlowCellModelFactory(
    private val resourceProvider: ResourceProvider
) {

    fun createCellModels(
        project: Project,
        flow: FlowWithSteps,
        runs: List<FlowRun>,
        users: List<User>
    ): List<BaseCellModel> {
        val models = mutableListOf<BaseCellModel>()

        models.add(SpaceCellModel(height = ElementMargin))

        models.add(
            FlowTitleCellModel(
                id = flow.entry.uid,
                flowName = flow.entry.name,
                projectName = project.name
            )
        )

        if (runs.isNotEmpty()) {
            models.addAll(createStatsModels(runs))
            models.addAll(createHistoryModels(runs, users))
        } else {
            models.add(
                EmptyHistoryCellModel(
                    message = resourceProvider.getString(R.string.no_runs)
                )
            )
        }

        return models
    }

    private fun createStatsModels(
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
            FlowStatsCellModel(
                id = CellId.RUN_STATISTICS,
                total = totalRuns,
                passed = passedRuns,
                failed = failedRuns,
                rate = successRate
            )
        )

        return models
    }

    private fun createHistoryModels(
        runs: List<FlowRun>,
        users: List<User>
    ): List<BaseCellModel> {
        val models = mutableListOf<BaseCellModel>()

        if (runs.isNotEmpty()) {
        }
        val userMap = users
            .associateBy { user -> user.uid }

        val sortedRuns = runs.sortedByDescending { run -> run.executionTime }
        val visibleRuns = sortedRuns.take(10)

        models.add(
            HeaderCellModel(
                id = CellId.HISTORY_HEADER,
                title = resourceProvider.getString(R.string.recent),
                icon = if (sortedRuns.size > visibleRuns.size) {
                    Icons.AutoMirrored.Outlined.ArrowForwardIos
                } else {
                    null
                }
            )
        )

        for ((idx, run) in visibleRuns.withIndex()) {
            if (idx > 0) {
                models.add(SpaceCellModel(height = SmallMargin))
            }

            val userName = userMap[run.userUid]
                ?.name
                ?: StringUtils.EMPTY

            val icon = if (run.isSuccess) {
                Icons.Outlined.CheckCircle
            } else {
                Icons.Outlined.ErrorOutline
            }

            models.add(
                HistoryItemCellModel(
                    id = CellId.RUN.format(idx),
                    isSuccessful = run.isSuccess,
                    icon = icon,
                    title = formatRunTime(run.executionTime),
                    description = resourceProvider.getString(R.string.by_with_str, userName)
                )
            )
        }

        models.add(SpaceCellModel(height = ElementMargin))

        return models
    }

    private fun formatRunTime(time: Long): String {
        val timeSinceRun = System.currentTimeMillis() - time

        return when {
            timeSinceRun < ONE_MINUTE -> {
                resourceProvider.getString(R.string.moments_ago)
            }

            timeSinceRun < ONE_HOUR -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(timeSinceRun)
                resourceProvider.getString(R.string.time_ago, minutes, "minutes")
            }

            timeSinceRun < ONE_DAY -> {
                val hours = TimeUnit.MILLISECONDS.toHours(timeSinceRun)
                resourceProvider.getString(R.string.time_ago, hours, "hours")
            }

            timeSinceRun < ONE_WEEK -> {
                val days = TimeUnit.MILLISECONDS.toDays(timeSinceRun)
                resourceProvider.getString(R.string.time_ago, days, "days")
            }

            else -> DATE_FORMAT.format(Date(time))
        }
    }

    object CellId {
        val TITLE = "title"
        val RUN_STATISTICS = "run-stats"
        val HISTORY_HEADER = "header"
        val RUN = "run_%s"
    }

    companion object {
        private val ONE_MINUTE = TimeUnit.MINUTES.toMillis(1)
        private val ONE_HOUR = TimeUnit.HOURS.toMillis(1)
        private val ONE_DAY = TimeUnit.DAYS.toMillis(1)
        private val ONE_WEEK = TimeUnit.DAYS.toMillis(7)
        private val DATE_FORMAT = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    }
}