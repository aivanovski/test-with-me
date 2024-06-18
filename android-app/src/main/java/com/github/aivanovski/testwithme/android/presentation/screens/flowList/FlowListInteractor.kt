package com.github.aivanovski.testwithme.android.presentation.screens.flowList

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.android.data.repository.FlowRepository
import com.github.aivanovski.testwithme.android.entity.db.FlowEntry
import com.github.aivanovski.testwithme.android.entity.exception.AppException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FlowListInteractor(
    private val flowRepository: FlowRepository
) {

    suspend fun getFlows(): Either<AppException, List<FlowEntry>> = withContext(Dispatchers.IO) {
        either {
            val flows = flowRepository.getFlows().bind()

            flows
        }
    }
}