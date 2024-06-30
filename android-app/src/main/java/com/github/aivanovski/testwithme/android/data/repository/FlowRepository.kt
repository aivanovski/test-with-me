package com.github.aivanovski.testwithme.android.data.repository

import arrow.core.raise.either
import com.github.aivanovski.testwithme.android.data.api.ApiClient
import com.github.aivanovski.testwithme.android.data.db.dao.FlowEntryDao
import com.github.aivanovski.testwithme.android.data.db.dao.StepEntryDao
import com.github.aivanovski.testwithme.android.entity.db.FlowEntry
import com.github.aivanovski.testwithme.android.entity.db.StepEntry
import com.github.aivanovski.testwithme.android.domain.usecases.ParseFlowFileUseCase
import com.github.aivanovski.testwithme.android.entity.FlowWithSteps
import com.github.aivanovski.testwithme.android.entity.exception.AppException
import com.github.aivanovski.testwithme.android.entity.exception.FailedToFindEntityException
import com.github.aivanovski.testwithme.android.entity.FlowSourceType
import com.github.aivanovski.testwithme.android.entity.exception.FailedToFindFlowByUidException
import com.github.aivanovski.testwithme.android.domain.dataconverters.convertToStepEntries
import arrow.core.Either

class FlowRepository(
    private val stepDao: StepEntryDao,
    private val flowDao: FlowEntryDao,
    private val api: ApiClient,
    private val parseFlowUseCase: ParseFlowFileUseCase
) {

    fun getCachedFlowByUid(
        flowUid: String
    ): Either<AppException, FlowWithSteps> = either {
        flowDao.getByUidWithSteps(flowUid)
            ?: raise(FailedToFindFlowByUidException(flowUid))
    }

    suspend fun getFlowByUid(
        flowUid: String
    ): Either<AppException, FlowWithSteps> = either {
        val flow = flowDao.getByUidWithSteps(flowUid)
            ?: raise(FailedToFindFlowByUidException(flowUid))

        if (flow.entry.sourceType == FlowSourceType.REMOTE) {
            val response = api.getFlow(flowUid).bind()

            val yamlFlow = parseFlowUseCase.parseBase64File(
                base64content = response.flow.base64Content
            ).bind()

            val stepEntries = yamlFlow.steps.convertToStepEntries(
                flowUid = flowUid
            )

            stepDao.removeByFlowUid(flowUid)
            stepDao.insert(stepEntries)

            flowDao.getByUidWithSteps(flowUid)
                ?: raise(FailedToFindFlowByUidException(flowUid))
        } else {
            flow
        }
    }

    fun getStepByUid(
        stepUid: String
    ): Either<AppException, StepEntry> = either {
        val step = stepDao.getByUid(stepUid)
            ?: raise(newUnableToFindStepByUidError(stepUid))

        step
    }

    fun removeFlowData(
        flowUid: String
    ): Either<AppException, Unit> = either {
        // TODO: make a transaction
        flowDao.removeByUid(flowUid)
        stepDao.removeByFlowUid(flowUid)
    }

    fun save(
        flow: FlowWithSteps
    ): Either<AppException, Unit> = either {
        val flowUid = flow.entry.uid

        removeFlowData(flowUid).bind()

        flowDao.insert(flow.entry)
        stepDao.insert(flow.steps)
    }

    suspend fun getFlows(): Either<AppException, List<FlowEntry>> = either {
        val remoteFlows = api.getFlows().bind()

        val uidToLocalFlowMap = flowDao.getAll()
            .associateBy { flow -> flow.uid }

        for (remote in remoteFlows) {
            val local = uidToLocalFlowMap[remote.uid]
            if (local == null) {
                flowDao.insert(remote)
            }
        }

        return Either.Right(remoteFlows)
    }

    suspend fun getNextStep(
        stepUid: String?
    ): Either<AppException, StepEntry?> = either {
        val flowUid = stepUid?.let { getFlowUidByStepUid(stepUid) }
        val existingFlowEntry = flowUid?.let { flowDao.getByUid(flowUid) }

        val flow = if (existingFlowEntry == null) {
//            if (flowUid == null) {
//                raise(AppException("Flow uid is null"))
//            }
//
//            val response = api.getFlow(flowUid).bind()
//
//            val yamlFlow = parseFlowUseCase.parseBase64File(
//                base64content = response.flow.base64Content
//            ).bind()
//
//            stepDao.removeByFlowUid(flowUid)
//
//            flowDao.insert(yamlFlow.entry)
//            stepDao.insert(yamlFlow.steps)

            raise(AppException("Not implemented"))
        } else {
            flowDao.getByUidWithSteps(existingFlowEntry.uid)
                ?: raise(FailedToFindFlowByUidException(existingFlowEntry.uid))
        }

        val currentStepEntry = stepDao.getByUid(stepUid)
            ?: raise(newUnableToFindStepByUidError(stepUid))

        val nextStepUid = currentStepEntry.nextUid

        if (nextStepUid != null) {
            val nextEntry = stepDao.getByUid(nextStepUid)
                ?: raise(newUnableToFindStepByUidError(nextStepUid))

            nextEntry
        } else {
            null
        }
    }

    private fun getFlowUidByStepUid(stepUid: String): String? {
        return stepDao.getByUid(stepUid)?.flowUid
    }

    private fun newUnableToFindStepByUidError(uid: String): AppException {
        return FailedToFindEntityException(
            entityName = StepEntry::class.java.simpleName,
            entityField = "uid",
            fieldValue = uid
        )
    }
}