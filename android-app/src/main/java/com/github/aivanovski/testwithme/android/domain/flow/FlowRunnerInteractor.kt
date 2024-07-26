package com.github.aivanovski.testwithme.android.domain.flow

import arrow.core.raise.either
import com.github.aivanovski.testwithme.android.data.settings.Settings
import com.github.aivanovski.testwithme.android.data.repository.StepRunRepository
import com.github.aivanovski.testwithme.android.entity.db.StepEntry
import com.github.aivanovski.testwithme.android.entity.OnStepFinishedAction
import com.github.aivanovski.testwithme.android.data.repository.JobRepository
import com.github.aivanovski.testwithme.android.domain.usecases.GetCurrentJobUseCase
import com.github.aivanovski.testwithme.android.domain.usecases.ParseFlowFileUseCase
import com.github.aivanovski.testwithme.android.entity.SourceType
import com.github.aivanovski.testwithme.android.entity.FlowWithSteps
import com.github.aivanovski.testwithme.android.entity.JobData
import com.github.aivanovski.testwithme.android.entity.OnFinishAction
import com.github.aivanovski.testwithme.android.entity.JobStatus
import com.github.aivanovski.testwithme.android.entity.StepVerificationType
import com.github.aivanovski.testwithme.android.entity.db.LocalStepRun
import com.github.aivanovski.testwithme.android.entity.db.JobEntry
import com.github.aivanovski.testwithme.android.entity.exception.AppException
import com.github.aivanovski.testwithme.android.entity.exception.FlowException
import com.github.aivanovski.testwithme.entity.FlowStep
import com.github.aivanovski.testwithme.entity.exception.AssertionException
import com.github.aivanovski.testwithme.entity.exception.FailedToGetUiNodesException
import com.github.aivanovski.testwithme.entity.exception.NodeException
import java.util.UUID
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import arrow.core.Either
import com.github.aivanovski.testwithme.android.data.file.FileCache
import com.github.aivanovski.testwithme.android.data.repository.FlowRepository
import com.github.aivanovski.testwithme.android.data.repository.FlowRunRepository
import com.github.aivanovski.testwithme.android.data.repository.GroupRepository
import com.github.aivanovski.testwithme.android.data.repository.ProjectRepository
import com.github.aivanovski.testwithme.android.domain.dataconverters.convertToFlowEntry
import com.github.aivanovski.testwithme.android.domain.dataconverters.convertToStepEntries
import com.github.aivanovski.testwithme.android.domain.usecases.GetExternalApplicationDataUseCase
import com.github.aivanovski.testwithme.android.entity.ExecutionResult
import com.github.aivanovski.testwithme.android.entity.FlowRunUploadResult
import com.github.aivanovski.testwithme.android.entity.SyncStatus
import com.github.aivanovski.testwithme.android.entity.db.ProjectEntry
import com.github.aivanovski.testwithme.extensions.unwrap
import com.github.aivanovski.testwithme.extensions.unwrapError
import com.github.aivanovski.testwithme.utils.Base64Utils
import com.github.aivanovski.testwithme.utils.StringUtils
import com.github.aivanovski.testwithme.web.api.request.PostFlowRunRequest
import timber.log.Timber

class FlowRunnerInteractor(
    private val settings: Settings,
    private val flowRepository: FlowRepository,
    private val jobRepository: JobRepository,
    private val stepRunRepository: StepRunRepository,
    private val flowRunRepository: FlowRunRepository,
    private val groupRepository: GroupRepository,
    private val projectRepository: ProjectRepository,
    private val getCurrentJobUseCase: GetCurrentJobUseCase,
    private val parseFlowUseCase: ParseFlowFileUseCase,
    private val getAppDataUseCase: GetExternalApplicationDataUseCase,
    private val fileCache: FileCache
) {

    fun saveReport(
        jobUid: String,
        reportContent: String
    ): Either<AppException, Unit> = either {
        fileCache.put(jobUid, reportContent)
    }

    suspend fun getCurrentJobData(): Either<AppException, JobData> = withContext(IO) {
        either {
            val getJobResult = getCurrentJobUseCase.getCurrentJob()
            if (getJobResult.isLeft()) {
                raise(getJobResult.unwrapError())
            }

            val job = getJobResult.unwrap()
                ?: raise(AppException("Unable to find current job"))

            val flow = getCachedFlowByUid(job.flowUid).bind()

            val step = flow.steps.firstOrNull { step -> step.uid == job.currentStepUid }
                ?: raise(AppException("Unable to find step: ${job.currentStepUid}"))

            val executionData = stepRunRepository.getOrCreate(
                jobUid = job.uid,
                flowUid = flow.entry.uid,
                stepUid = step.uid
            ).bind()

            JobData(
                job = job,
                flow = flow,
                currentStep = step,
                executionData = executionData
            )
        }
    }

    suspend fun updateJob(
        entry: JobEntry
    ): Either<AppException, Unit> = withContext(IO) {
        jobRepository.update(entry)
    }

    suspend fun removeJob(
        jobUid: String
    ): Either<AppException, Unit> = withContext(IO) {
        either {
            jobRepository.removeByUid(jobUid)
        }
    }

    suspend fun moveJobToHistory(
        jobUid: String
    ): Either<AppException, Unit> = withContext(IO) {
        either {
            jobRepository.moveToHistory(jobUid)
        }
    }

    suspend fun getJobs(): Either<AppException, List<JobEntry>> = withContext(IO) {
        either {
            jobRepository.getAll()
        }
    }

    suspend fun getCachedProjectByUid(
        projectUid: String
    ): Either<AppException, ProjectEntry> = withContext(IO) {
        projectRepository.getCachedProjectByUid(projectUid)
    }

    suspend fun getCachedFlowByUid(
        flowUid: String
    ): Either<AppException, FlowWithSteps> = withContext(IO) {
        flowRepository.getCachedFlowByUid(flowUid)
    }

    suspend fun findFlowByName(
        projectUid: String,
        groupName: String?,
        name: String
    ): Either<AppException, FlowWithSteps?> = withContext(IO) {
        either {
            val candidates = flowRepository.getFlows()
                .bind()
                .filter { flow ->
                    flow.projectUid == projectUid && flow.name.trim() == name
                }

            val flowUid = when {
                candidates.size == 1 -> {
                    candidates.first().uid
                }

                groupName != null -> {
                    val group = groupRepository.getGroups()
                        .bind()
                        .firstOrNull { group ->
                            group.projectUid == projectUid && group.name.trim() == groupName
                        }
                        ?: raise(AppException("Unable to find group: $groupName"))

                    val flow = candidates.firstOrNull { flow ->
                        flow.groupUid == group.uid
                    }
                        ?: raise(AppException("Unable to find flow by group uid: ${group.uid}"))

                    flow.uid
                }

                candidates.isNotEmpty() -> {
                    candidates.first().uid
                }

                else -> {
                    raise(AppException("Unable to find flow: $name"))
                }
            }

            flowRepository.getFlowByUid(flowUid).bind()
        }
    }

    suspend fun getFlowByUid(
        flowUid: String
    ): Either<AppException, FlowWithSteps> = withContext(IO) {
        flowRepository.getFlowByUid(flowUid)
    }

    suspend fun getExecutionData(
        jobUid: String,
        flowUid: String,
        stepUid: String
    ): Either<AppException, LocalStepRun> = withContext(IO) {
        stepRunRepository.getOrCreate(jobUid, flowUid, stepUid)
    }

    suspend fun getJobByUid(
        jobUid: String
    ): Either<AppException, JobEntry> = withContext(IO) {
        jobRepository.getJobByUid(jobUid)
    }

    suspend fun removeAllJobs(
        exclude: Set<String> = emptySet()
    ): Either<AppException, Unit> = withContext(IO) {
        either {
            val removeUids = jobRepository.getAll()
                .filter { entry -> entry.uid !in exclude }
                .map { entry -> entry.uid }

            for (id in removeUids) {
                jobRepository.removeByUid(id)
            }
        }
    }

    suspend fun saveFlowContent(
        flowUid: String,
        content: String
    ): Either<AppException, Unit> = withContext(IO) {
        flowRepository.saveFlowContent(flowUid, content)
    }

    suspend fun parseFlow(
        base64Content: String
    ): Either<AppException, FlowWithSteps> = withContext(IO) {
        either {
            val yamlFlow = parseFlowUseCase.parseBase64File(base64Content).bind()

            val newUid = UUID.randomUUID().toString()
            val flowUid = "Local:${yamlFlow.name}:$newUid"
            val flow = yamlFlow.convertToFlowEntry(
                flowUid = flowUid,
                projectUid = "Local", // TODO: should be null
                sourceType = SourceType.LOCAL
            )
            val steps = yamlFlow.steps.convertToStepEntries(
                flowUid = flowUid
            )

            FlowWithSteps(
                entry = flow,
                steps = steps
            )
        }
    }

    suspend fun addFlowToJobQueue(
        flow: FlowWithSteps,
    ): Either<AppException, String> = withContext(IO) {
        either {
            val flowUid = flow.entry.uid

            flowRepository.removeFlowData(flowUid).bind()
            flowRepository.save(flow).bind()

            val firstStepUid = flow.steps.firstOrNull()?.uid
                ?: raise(AppException("No steps found"))

            addRunnerEntry(
                flowUid = flowUid,
                stepUid = firstStepUid,
                jobUid = null,
                onFinishAction = OnFinishAction.SHOW_DETAILS
            ).bind()
        }
    }

    suspend fun addFlowToJobQueue(
        flowUid: String,
        jobUid: String?,
        onFinishAction: OnFinishAction
    ): Either<AppException, String> = withContext(IO) {
        either {
            val flow = flowRepository.getFlowByUid(flowUid).bind()

            val firstStepUid = flow.steps.firstOrNull()?.uid
                ?: raise(AppException("No steps found"))

            addRunnerEntry(
                flowUid = flowUid,
                stepUid = firstStepUid,
                jobUid = jobUid,
                onFinishAction = onFinishAction
            ).bind()
        }
    }

    suspend fun uploadJobResult(
        jobUid: String
    ): Either<AppException, FlowRunUploadResult> = withContext(IO) {
        either {
            val job = jobRepository.getJobByUid(jobUid).bind()
            val steps = stepRunRepository.getByJobUid(jobUid).bind()
            val flow = flowRepository.getCachedFlowByUid(job.flowUid).bind()
            val project = getCachedProjectByUid(flow.entry.projectUid).bind()
            val appData = getAppDataUseCase.getApplicationData(project.packageName).bind()
            val reportContent = fileCache.get(job.uid).bind()

            val stepToUpload = steps.firstOrNull { step ->
                step.syncStatus == SyncStatus.WAITING_FOR_SYNC
            }
                ?: raise(AppException("Failed to find step to upload"))

            val isSuccess = (stepToUpload.result?.startsWith("Either.Right(") ?: false)

            val flowRun = PostFlowRunRequest(
                flowId = job.flowUid,
                durationInMillis = job.executionTime ?: 0L,
                isSuccess = isSuccess,
                result = stepToUpload.result ?: StringUtils.EMPTY,
                appVersionName = appData.appVersion.name,
                appVersionCode = appData.appVersion.code.toString(),
                reportBase64Content = Base64Utils.encode(reportContent)
            )

            val uploadResult = uploadFlowRunWithRetry(flowRun).bind()

            Timber.d("uploadResult: result=$uploadResult, jobUid=$jobUid")

            when (uploadResult?.isAccepted) {
                true -> {
                    stepRunRepository.update(
                        stepToUpload.copy(
                            syncStatus = SyncStatus.SYNCED
                        )
                    )
                    jobRepository.moveToHistory(jobUid).bind()

                    uploadResult
                }

                false -> {
                    stepRunRepository.update(
                        stepToUpload.copy(
                            syncStatus = SyncStatus.FAILURE
                        )
                    )

                    uploadResult
                }

                else -> {
                    raise(AppException("Unable to upload flow"))
                }
            }
        }
    }

    private suspend fun uploadFlowRunWithRetry(
        flowRun: PostFlowRunRequest
    ): Either<AppException, FlowRunUploadResult?> = either {
        var result: FlowRunUploadResult? = null
        var uploadCount = 0

        do {
            val uploadResult = flowRunRepository.uploadRun(flowRun)
            if (uploadResult.isRight()) {
                result = uploadResult.unwrap()
            }
            uploadCount++
        } while (uploadCount < 3 && result == null)

        result
    }

    suspend fun onStepFinished(
        jobUid: String,
        entry: StepEntry,
        result: Either<Exception, Any>
    ): Either<AppException, OnStepFinishedAction> = withContext(IO) {
        either {
            when (entry.stepVerificationType) {
                StepVerificationType.LOCAL -> {
                    verifyLocally(jobUid, entry, result)
                        .bind()
                }

                StepVerificationType.REMOTE -> {
                    verifyRemotely(entry, result)
                        .bind()
                }
            }
        }
    }

    private suspend fun verifyLocally(
        jobUid: String,
        stepEntry: StepEntry,
        result: Either<Exception, Any>
    ): Either<AppException, OnStepFinishedAction> = either {
        val nextStepEntry = flowRepository.getNextStep(stepEntry.uid).bind()

        val stepRun = stepRunRepository.getOrCreate(
            jobUid = jobUid,
            flowUid = stepEntry.flowUid,
            stepUid = stepEntry.uid
        ).bind()

        val attemptCount = stepRun.attemptCount + 1
        val isFinishedSuccessfully = result.isRight()
        val isLast = (nextStepEntry == null)
        val isRetry = canRetry(stepEntry, attemptCount, result)

        val action = if (isFinishedSuccessfully) {
            if (isLast) {
                OnStepFinishedAction.Complete
            } else {
                if (nextStepEntry != null) {
                    OnStepFinishedAction.Next(nextStepUid = nextStepEntry.uid)
                } else {
                    OnStepFinishedAction.Stop
                }
            }
        } else {
            if (isRetry) {
                OnStepFinishedAction.Retry
            } else {
                OnStepFinishedAction.Stop
            }
        }

        val syncStatus = if (action == OnStepFinishedAction.Complete ||
            action == OnStepFinishedAction.Stop
        ) {
            SyncStatus.WAITING_FOR_SYNC
        } else {
            SyncStatus.NONE
        }

        val updatedStepRun = stepRun.copy(
            result = result.toString(),
            attemptCount = attemptCount,
            syncStatus = syncStatus
        )
        stepRunRepository.update(updatedStepRun).bind()

        action
    }

    private fun verifyRemotely(
        entry: StepEntry,
        result: Either<Exception, Any>
    ): Either<AppException, OnStepFinishedAction> {
        TODO()
    }

    private suspend fun addRunnerEntry(
        flowUid: String,
        stepUid: String,
        jobUid: String?,
        onFinishAction: OnFinishAction
    ): Either<AppException, String> = withContext(IO) {
        either {
            val uid = jobUid ?: UUID.randomUUID().toString()

            jobRepository.add(
                JobEntry(
                    id = null,
                    flowUid = flowUid,
                    currentStepUid = stepUid,
                    uid = uid,
                    addedTimestamp = System.currentTimeMillis(),
                    executionTime = null,
                    finishedTimestamp = null,
                    executionResult = ExecutionResult.NONE,
                    status = JobStatus.PENDING,
                    onFinishAction = onFinishAction
                )
            )

            settings.startJobUid = uid

            uid
        }
    }

    private fun canRetry(
        entry: StepEntry,
        attemptCount: Int,
        result: Either<Exception, Any>
    ): Boolean {
        if (result.isRight()) {
            return false
        }

        val exception = result.unwrapError()
        val isFlaky = (entry.command.isStepFlaky() || exception.isFlakyException())

        return isFlaky && attemptCount < 3
    }

    private fun FlowStep.isStepFlaky(): Boolean {
        return this is FlowStep.AssertVisible ||
            this is FlowStep.AssertNotVisible ||
            this is FlowStep.TapOn
    }

    private fun Exception.isFlakyException(): Boolean {
        return this is FlowException &&
            (this.cause is NodeException ||
                this.cause is FailedToGetUiNodesException ||
                this.cause is AssertionException)
    }

    companion object {
    }
}