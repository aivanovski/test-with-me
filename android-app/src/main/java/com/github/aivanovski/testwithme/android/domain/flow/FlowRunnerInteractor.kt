package com.github.aivanovski.testwithme.android.domain.flow

import arrow.core.raise.either
import com.github.aivanovski.testwithme.android.data.Settings
import com.github.aivanovski.testwithme.android.data.repository.StepRunRepository
import com.github.aivanovski.testwithme.android.entity.db.StepEntry
import com.github.aivanovski.testwithme.android.entity.OnStepFinishedAction
import com.github.aivanovski.testwithme.android.data.repository.JobRepository
import com.github.aivanovski.testwithme.android.domain.usecases.GetCurrentJobUseCase
import com.github.aivanovski.testwithme.android.domain.usecases.ParseFlowFileUseCase
import com.github.aivanovski.testwithme.android.entity.FlowSourceType
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
import com.github.aivanovski.testwithme.android.data.repository.FlowRepository
import com.github.aivanovski.testwithme.android.data.repository.FlowRunRepository
import com.github.aivanovski.testwithme.android.domain.dataconverters.convertToFlowEntry
import com.github.aivanovski.testwithme.android.domain.dataconverters.convertToStepEntries
import com.github.aivanovski.testwithme.android.entity.FlowRunUploadResult
import com.github.aivanovski.testwithme.android.entity.SyncStatus
import com.github.aivanovski.testwithme.extensions.unwrap
import com.github.aivanovski.testwithme.extensions.unwrapError
import com.github.aivanovski.testwithme.utils.StringUtils
import com.github.aivanovski.testwithme.web.api.request.PostFlowRunRequest
import timber.log.Timber

class FlowRunnerInteractor(
    private val settings: Settings,
    private val flowRepository: FlowRepository,
    private val jobRepository: JobRepository,
    private val stepRunRepository: StepRunRepository,
    private val getCurrentJobUseCase: GetCurrentJobUseCase,
    private val parseFlowUseCase: ParseFlowFileUseCase,
    private val flowRunRepository: FlowRunRepository
) {

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

    suspend fun getJobs(): Either<AppException, List<JobEntry>> = withContext(IO) {
        either {
            jobRepository.getAll()
        }
    }

    suspend fun getCachedFlowByUid(
        flowUid: String
    ): Either<AppException, FlowWithSteps> = withContext(IO) {
        flowRepository.getCachedFlowByUid(flowUid)
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
        exclude: Set<String>
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

    suspend fun parseAndAddToJobQueue(
        base64Content: String
    ): Either<AppException, String> = withContext(IO) {
        either {
            val yamlFlow = parseFlowUseCase.parseBase64File(base64Content).bind()

            val flowUid = yamlFlow.name
            val flow = yamlFlow.convertToFlowEntry(
                flowUid = flowUid,
                projectUid = "Local",
                sourceType = FlowSourceType.LOCAL
            )
            val steps = yamlFlow.steps.convertToStepEntries(
                flowUid = flowUid
            )

            flowRepository.removeFlowData(flowUid).bind()
            flowRepository.save(FlowWithSteps(flow, steps)).bind()

            val firstStepUid = steps.firstOrNull()?.uid
                ?: raise(AppException("No steps found"))

            addRunnerEntry(
                flowUid = flowUid,
                stepUid = firstStepUid,
                jobUid = null
            ).bind()
        }
    }

    suspend fun addFlowToJobQueue(
        flowUid: String,
        jobUid: String?
    ): Either<AppException, String> = withContext(IO) {
        either {
            val flow = flowRepository.getFlowByUid(flowUid).bind()

            val firstStepUid = flow.steps.firstOrNull()?.uid
                ?: raise(AppException("No steps found"))

            addRunnerEntry(
                flowUid = flowUid,
                stepUid = firstStepUid,
                jobUid = jobUid
            ).bind()
        }
    }

    suspend fun uploadJobResult(
        jobUid: String
    ): Either<AppException, FlowRunUploadResult> = withContext(IO) {
        either {
            val job = jobRepository.getJobByUid(jobUid).bind()
            val steps = stepRunRepository.getByJobUid(jobUid).bind()

            val stepToUpload = steps.firstOrNull { step ->
                step.syncStatus == SyncStatus.WAITING_FOR_SYNC
            }
                ?: raise(AppException("Failed to find step to upload"))

            val isSuccess = (stepToUpload.result?.startsWith("Either.Right(") ?: false)

            val flowRun = PostFlowRunRequest(
                flowId = job.flowUid,
                durationInMillis = 12_000, // TODO: should be stored in db
                isSuccess = isSuccess,
                result = stepToUpload.result ?: StringUtils.EMPTY
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
                    jobRepository.removeByUid(jobUid)

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
            val uploadResult = flowRunRepository.sendRun(flowRun)
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

//        val flow = flowRepository.getFlowByUid(flowUid = stepEntry.flowUid).bind()

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
        jobUid: String?
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
                    status = JobStatus.PENDING,
                    onFinishAction = OnFinishAction.STOP
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