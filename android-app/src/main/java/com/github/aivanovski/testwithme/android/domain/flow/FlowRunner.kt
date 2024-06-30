package com.github.aivanovski.testwithme.android.domain.flow

import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityNodeInfo
import arrow.core.raise.either
import com.github.aivanovski.testwithme.android.data.Settings
import com.github.aivanovski.testwithme.android.entity.OnStepFinishedAction
import com.github.aivanovski.testwithme.flow.driver.Driver
import com.github.aivanovski.testwithme.android.entity.OnFinishAction
import com.github.aivanovski.testwithme.android.entity.JobStatus
import com.github.aivanovski.testwithme.android.entity.db.FlowEntry
import com.github.aivanovski.testwithme.android.entity.db.JobEntry
import com.github.aivanovski.testwithme.android.entity.exception.AppException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import arrow.core.Either
import com.github.aivanovski.testwithme.android.presentation.MainActivity
import com.github.aivanovski.testwithme.android.presentation.StartArgs
import com.github.aivanovski.testwithme.extensions.unwrap
import com.github.aivanovski.testwithme.extensions.unwrapError

class FlowRunner(
    private val context: Context,
    private val settings: Settings,
    private val interactor: FlowRunnerInteractor,
    driver: Driver<AccessibilityNodeInfo>
) {

    private val stateRef = AtomicReference(RunnerState.IDLE)
    private val stepIndex = AtomicInteger(0)
    private val jobUidRef = AtomicReference<String?>(null)
    private val listeners = mutableListOf<FlowLifecycleListener>()
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main)
    private val commandExecutor = CommandExecutor(interactor, driver)
    private val commandFactory = StepCommandFactory(interactor)

    init {
        listeners.add(TimberFlowReporter())
    }

    fun isRunning(): Boolean = (stateRef.get() == RunnerState.RUNNING)

    fun isIdle(): Boolean = (stateRef.get() == RunnerState.IDLE)

    fun runNextFlow() {
        val jobUid = settings.startJobUid

        scope.launch {
            val findNextJobResult = findNextJobToRun(jobUid)
            if (findNextJobResult.isLeft()) {
                onErrorOccurred(exception = findNextJobResult.unwrapError())
                return@launch
            }

            val nextJobUid = findNextJobResult.getOrNull()
            Timber.d("runNextFlow: jobUid=%s", nextJobUid)

            if (nextJobUid != null) {
                val runResult = runFlow(nextJobUid)
                if (runResult.isLeft()) {
                    onErrorOccurred(exception = runResult.unwrapError())
                    return@launch
                }
            }
        }
    }

    private suspend fun findNextJobToRun(
        targetJobUid: String?
    ): Either<AppException, String?> = either {
        val jobs = interactor.getJobs().bind()

        val running = jobs.filterByStatus(JobStatus.RUNNING)
        val pending = jobs.filterByStatus(JobStatus.PENDING)

        val cancelledUids = jobs
            .filterByStatus(JobStatus.CANCELLED)
            .map { job -> job.uid }

        val finishedUids = jobs
            .filterByStatus(JobStatus.FINISHED)
            .map { job -> job.uid }

        if (targetJobUid in cancelledUids || targetJobUid in finishedUids) {
            settings.startJobUid = null
        }

        Timber.d(
            "jobs: size=%s, running=%s, pending=%s",
            jobs.size,
            running.size,
            pending.size
        )
        for (job in jobs) {
            Timber.d("    %s", job)
        }

        if (running.isNotEmpty() && isIdle()) {
            for (job in running) {
                val cancelledJob = job.copy(
                    status = JobStatus.CANCELLED
                )

                interactor.updateJob(cancelledJob).bind()
            }
        }

        val jobUid = if (pending.isNotEmpty() && isIdle()) {
            val jobToStart = targetJobUid?.let {
                pending.firstOrNull { entry ->
                    entry.uid == targetJobUid
                }
            }

            jobToStart?.uid ?: pending.first().uid
        } else {
            null
        }

        jobUid
    }

    private suspend fun runFlow(
        jobUid: String
    ): Either<AppException, Unit> = either {
        val jobs = interactor.getJobs().bind()

        val job = jobs.firstOrNull { job -> job.uid == jobUid }
            ?: raise(AppException("Failed to find job by uid: $jobUid"))

        val flow = interactor.getCachedFlowByUid(job.flowUid).bind()

        jobUidRef.set(jobUid)
        stateRef.set(RunnerState.RUNNING)
        stepIndex.set(0)

        if (settings.startJobUid == jobUid) {
            settings.startJobUid = null
        }

        val updatedJob = job.copy(status = JobStatus.RUNNING)
        interactor.updateJob(updatedJob).bind()

        notifyOnFlowStarted(flow.entry)

        runCurrentStep(
            initialDelay = 1500L
        ).bind()
    }

    fun stop() {
        stateRef.set(RunnerState.IDLE)
        job.cancel()
    }

    private suspend fun onErrorOccurred(
        jobUid: String? = null,
        exception: AppException
    ) {
        Timber.e("onErrorOccurred: jobUid=%s, error=%s", jobUid, exception)
        Timber.e(exception)
        stateRef.set(RunnerState.IDLE)

        if (jobUid != null) {
            val job = interactor.getJobByUid(jobUid).getOrNull()
            if (job != null) {
                interactor.updateJob(job.copy(status = JobStatus.CANCELLED))
            }
        }
    }

    private suspend fun runCurrentStep(
        initialDelay: Long = DELAY_BETWEEN_STEPS
    ): Either<AppException, Unit> = either {
        delay(initialDelay)

        if (!isRunning()) {
            raise(AppException("Flow was cancelled"))
        }

        val jobData = interactor.getCurrentJobData().bind()

        val (job, flow, stepEntry, executionData) = jobData

        val command = commandFactory.createCommand(stepEntry.command).bind()

        val nextActionResult = commandExecutor.execute(
            job = job,
            flow = flow.entry,
            stepEntry = stepEntry,
            command = command,
            stepIndex = stepIndex.get(),
            attemptIndex = executionData.attemptCount,
            lifecycleListener = listeners.first()
        )
        if (nextActionResult.isLeft()) {
            finishFlowExecution(
                jobUid = job.uid,
                isRunNextAllowed = false
            ).bind()

            return@either
        }

        val nextAction = nextActionResult.unwrap()
        when (nextAction) {
            is OnStepFinishedAction.Next -> {
                stepIndex.incrementAndGet()
                runCurrentStep().bind()
            }

            OnStepFinishedAction.Complete -> {
                finishFlowExecution(
                    jobUid = job.uid,
                    isRunNextAllowed = true
                ).bind()
            }

            OnStepFinishedAction.Retry -> {
                runCurrentStep().bind()
            }

            OnStepFinishedAction.Stop -> {
                finishFlowExecution(
                    jobUid = job.uid,
                    isRunNextAllowed = false
                ).bind()

                raise(AppException("Flow was stopped"))
            }
        }
    }

    private suspend fun finishFlowExecution(
        jobUid: String,
        isRunNextAllowed: Boolean
    ): Either<AppException, Unit> = either {
        stateRef.set(RunnerState.IDLE)

        val job = interactor.getJobByUid(jobUid).bind()

        interactor.updateJob(
            job.copy(
                status = JobStatus.FINISHED
            )
        ).bind()

        val uploadResult = interactor.uploadJobResult(job.uid)
        val isUploadedSuccessfully = uploadResult.isRight()
        val isRunNext = (job.onFinishAction == OnFinishAction.RUN_NEXT)
        if (isRunNext && isRunNextAllowed && isUploadedSuccessfully) {
            scope.launch {
                runNextFlow()
            }
        } else {
            val intent = MainActivity.createStartIntent(
                context = context,
                args = StartArgs(
                    flowUid = job.flowUid
                )
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private fun notifyOnFlowStarted(
        flow: FlowEntry
    ) {
        for (listener in listeners) {
            listener.onFlowStarted(flow)
        }
    }

    private fun List<JobEntry>.filterByStatus(
        status: JobStatus
    ): List<JobEntry> {
        return filter { job -> job.status == status }
    }

    enum class RunnerState {
        IDLE,
        RUNNING
    }

    companion object {
        private const val DELAY_IF_PENDING_START = 5000L // in milliseconds
        const val DELAY_BETWEEN_STEPS = 1000L // in milliseconds
    }
}