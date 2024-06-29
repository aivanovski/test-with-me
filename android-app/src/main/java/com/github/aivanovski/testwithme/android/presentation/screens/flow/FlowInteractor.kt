package com.github.aivanovski.testwithme.android.presentation.screens.flow

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.android.data.repository.FlowRunRepository
import com.github.aivanovski.testwithme.android.data.repository.FlowRepository
import com.github.aivanovski.testwithme.android.data.repository.ProjectRepository
import com.github.aivanovski.testwithme.android.entity.exception.AppException
import com.github.aivanovski.testwithme.android.entity.exception.FailedToFindProjectByUidException
import com.github.aivanovski.testwithme.android.presentation.screens.flow.model.FlowData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FlowInteractor(
    private val flowRepository: FlowRepository,
    private val projectRepository: ProjectRepository,
    private val florRunRepository: FlowRunRepository
) {

    suspend fun loadData(
        flowUid: String
    ): Either<AppException, FlowData> = withContext(Dispatchers.IO) {
        either {
            val flow = flowRepository.getFlowByUid(flowUid).bind()

            val projectUid = flow.entry.projectUid
            val project = projectRepository.getProjectByUid(projectUid).bind()
                ?: raise(FailedToFindProjectByUidException(projectUid))

            val executions = florRunRepository.getRemoteRuns(flowUid).bind()

            FlowData(
                flow = flow,
                project = project,
                executions = executions
            )
        }
    }
}