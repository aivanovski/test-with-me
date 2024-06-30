package com.github.aivanovski.testwithme.android.presentation.screens.flow

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.android.data.repository.FlowRepository
import com.github.aivanovski.testwithme.android.data.repository.FlowRunRepository
import com.github.aivanovski.testwithme.android.data.repository.ProjectRepository
import com.github.aivanovski.testwithme.android.data.repository.UserRepository
import com.github.aivanovski.testwithme.android.domain.driver.AccessibilityDriverService
import com.github.aivanovski.testwithme.android.domain.driver.model.DriverState
import com.github.aivanovski.testwithme.android.domain.flow.FlowRunnerInteractor
import com.github.aivanovski.testwithme.android.entity.JobStatus
import com.github.aivanovski.testwithme.android.entity.exception.AppException
import com.github.aivanovski.testwithme.android.entity.exception.FailedToFindProjectByUidException
import com.github.aivanovski.testwithme.android.presentation.screens.flow.model.FlowData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FlowInteractor(
    private val testInteractor: FlowRunnerInteractor,
    private val flowRepository: FlowRepository,
    private val projectRepository: ProjectRepository,
    private val flowRunRepository: FlowRunRepository,
    private val userRepository: UserRepository
) {

    fun isDriverServiceEnabled(): Boolean {
        return AccessibilityDriverService.getState() == DriverState.RUNNING
    }

    suspend fun loadData(
        flowUid: String
    ): Either<AppException, FlowData> = withContext(Dispatchers.IO) {
        either {
            val flow = flowRepository.getFlowByUid(flowUid).bind()

            val projectUid = flow.entry.projectUid
            val project = projectRepository.getProjectByUid(projectUid).bind()
                ?: raise(FailedToFindProjectByUidException(projectUid))

            val executions = flowRunRepository.getRemoteRuns(flowUid).bind()

            val users = userRepository.getUsers().bind()

            FlowData(
                flow = flow,
                project = project,
                runs = executions,
                users = users
            )
        }
    }

    suspend fun startFlow(
        flowUid: String,
        jobUid: String?
    ): Either<AppException, String> = withContext(Dispatchers.IO) {
        either {
            val jobs = testInteractor.getJobs().bind()

            val pendingJobs = jobs.filter { job -> job.status == JobStatus.PENDING }
            pendingJobs.forEach { job ->
                testInteractor.removeJob(job.uid)
            }

            val jobId = testInteractor.addFlowToJobQueue(flowUid, jobUid).bind()

            jobId
        }
    }

    suspend fun cancelJob(
        jobUid: String
    ): Either<AppException, Unit> = withContext(Dispatchers.IO) {
        either {
            val job = testInteractor.getJobByUid(jobUid).bind()

            testInteractor.updateJob(
                job.copy(
                    status = JobStatus.CANCELLED
                )
            ).bind()
        }
    }
}