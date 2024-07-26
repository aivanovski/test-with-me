package com.github.aivanovski.testwithme.android.presentation.screens.uploadTest

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.android.data.repository.FlowRepository
import com.github.aivanovski.testwithme.android.data.repository.GroupRepository
import com.github.aivanovski.testwithme.android.data.repository.JobRepository
import com.github.aivanovski.testwithme.android.data.repository.ProjectRepository
import com.github.aivanovski.testwithme.android.entity.SourceType
import com.github.aivanovski.testwithme.android.entity.exception.AppException
import com.github.aivanovski.testwithme.android.presentation.screens.uploadTest.model.UploadTestScreenData
import com.github.aivanovski.testwithme.android.utils.Base64Utils
import com.github.aivanovski.testwithme.web.api.request.PostFlowRequest
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import timber.log.Timber

class UploadTestInteractor(
    private val projectRepository: ProjectRepository,
    private val groupRepository: GroupRepository,
    private val flowRepository: FlowRepository,
    private val jobRepository: JobRepository
) {

    suspend fun loadData(
        flowUid: String
    ): Either<AppException, UploadTestScreenData> = withContext(IO) {
        either {
            val content = flowRepository.getCachedFlowContent(flowUid).bind()
                ?: raise(AppException("Failed to get flow content: uid=$flowUid"))

            val projects = projectRepository.getProjects().bind()
            val projectUids = projects
                .map { project -> project.uid }
                .toSet()

            val groups = groupRepository.getGroups()
                .bind()
                .filter { group -> group.projectUid in projectUids }

            UploadTestScreenData(
                projects = projects,
                groups = groups,
                content = content,
                base64Content = Base64Utils.encode(content)
            )
        }
    }

    suspend fun uploadFlow(
        flowUid: String,
        request: PostFlowRequest
    ): Either<AppException, Unit> = withContext(IO) {
        either {
            val response = flowRepository.uploadFlowContent(request).bind()
            val flow = flowRepository.getCachedFlowByUid(flowUid).bind()

            val newFlowUid = response.id
            Timber.d("uploadFlow: newFlowUid=${newFlowUid}")

            val updatedFlow = flow.entry.copy(
                uid = newFlowUid,
                projectUid = request.projectId.orEmpty(),
                groupUid = request.groupId,
                sourceType = SourceType.REMOTE
            )

            flowRepository.updateCachedFlow(updatedFlow)

            val jobEntries = jobRepository.getAllHistory()
                .filter { job -> job.flowUid == flowUid }

            for (job in jobEntries) {
                val updatedJob = job.copy(
                    flowUid = newFlowUid
                )

                jobRepository.updateHistory(updatedJob)
            }

            Timber.d("uploadFlow: jobs=$jobEntries")
        }
    }
}