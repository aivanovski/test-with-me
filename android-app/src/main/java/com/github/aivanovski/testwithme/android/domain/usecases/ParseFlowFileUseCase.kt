package com.github.aivanovski.testwithme.android.domain.usecases

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.android.entity.exception.ParsingException
import com.github.aivanovski.testwithme.android.utils.Base64Utils
import com.github.aivanovski.testwithme.entity.Flow
import com.github.aivanovski.testwithme.flow.yaml.YamlParser

class ParseFlowFileUseCase {

    fun parseBase64File(
        base64content: String
    ): Either<ParsingException, Flow> = either {
        val decodedContent = Base64Utils.decode(base64content)
            ?: raise(ParsingException("Invalid bas64 string"))

        parse(decodedContent).bind()
    }

    private fun parse(
        content: String,
    ): Either<ParsingException, Flow> = either {
        val flow = YamlParser().parse(content)
            .mapLeft { exception -> ParsingException(cause = exception) }
            .bind()

        flow

        // TODO: remove comments
//        val steps = mutableListOf<StepEntry>()
//        for (stepIdx in convertedSteps.indices) {
//            val step = convertedSteps[stepIdx]
//            val nextStep = convertedSteps.getOrNull(stepIdx + 1)
//            val stepUid = "$flowName:$stepIdx"
//
//            val nextUid = if (nextStep != null) {
//                "$flowName:${stepIdx + 1}"
//            } else {
//                null
//            }
//
//            steps.add(
//                StepEntry(
//                    id = null,
//                    uid = stepUid,
//                    index = stepIdx,
//                    flowUid = flowUid,
//                    nextUid = nextUid,
//                    command = step,
//                    stepVerificationType = StepVerificationType.LOCAL
//                )
//            )
//        }
//
//        return Either.Right(
//            FlowWithSjwteps(
//                entry = FlowEntry(
//                    id = null,
//                    uid = flowUid,
//                    projectUid = "", // TODO: should not be empty
//                    name = flowUid,
//                    sourceType = FlowSourceType.REMOTE
//                ),
//                steps = steps
//            )
//        )
    }
}