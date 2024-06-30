package com.github.aivanovski.testwithme.android.test

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import arrow.core.raise.either
import com.github.aivanovski.testwithme.android.di.GlobalInjector
import com.github.aivanovski.testwithme.android.domain.flow.FlowRunnerInteractor
import com.github.aivanovski.testwithme.android.entity.exception.AppException
import com.github.aivanovski.testwithme.android.utils.Base64Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import arrow.core.Either
import com.github.aivanovski.testwithme.extensions.unwrapError
import com.github.aivanovski.testwithme.utils.StringUtils

class TestFlowBroadcastReceiver : BroadcastReceiver() {

    private val testInteractor: FlowRunnerInteractor by GlobalInjector.inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        val content = intent?.extras?.getString(EXTRA_TEST_FLOW)

        Timber.d("onReceive: data.size=%s", content?.length)
        if (content != null) {
            printFlow(content)

            scope.launch {
                val parseResult = parseAndRunFlow(content)

                Timber.d(
                    "Process result: isSuccess=%s, jobUid=%s",
                    parseResult.isRight(),
                    parseResult.getOrNull()
                )

                if (parseResult.isLeft()) {
                    Timber.e(parseResult.unwrapError())
                }
            }
        }
    }

    private suspend fun parseAndRunFlow(
        testContent: String
    ): Either<AppException, String> = either {
        val jobUid = testInteractor.parseAndAddToJobQueue(testContent).bind()

        testInteractor.removeAllJobs(exclude = setOf(jobUid)).bind()

        jobUid
    }

    private fun printFlow(data: String) {
        val decodedData = Base64Utils.decode(data) ?: StringUtils.EMPTY

        val lines = decodedData.split("\n")
        Timber.d("Test flow: %s lines", lines.size)
        for (line in lines) {
            Timber.d(line)
        }
    }

    companion object {
        private const val EXTRA_TEST_FLOW = "testFlow"
    }
}