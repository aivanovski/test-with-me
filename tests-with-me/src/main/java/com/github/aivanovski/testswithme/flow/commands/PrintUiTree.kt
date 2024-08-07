package com.github.aivanovski.testswithme.flow.commands

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testswithme.entity.exception.FlowExecutionException
import com.github.aivanovski.testswithme.extensions.dumpToString
import com.github.aivanovski.testswithme.flow.runner.ExecutionContext

class PrintUiTree : ExecutableStepCommand<Unit> {

    override fun describe(): String {
        return "Print UI tree"
    }

    override suspend fun <NodeType> execute(
        context: ExecutionContext<NodeType>
    ): Either<FlowExecutionException, Unit> =
        either {
            val uiRoot = context.driver.getUiTree().bind()

            context.logger.debug(uiRoot.dumpToString())
        }
}