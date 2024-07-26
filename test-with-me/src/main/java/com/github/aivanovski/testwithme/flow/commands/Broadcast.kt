package com.github.aivanovski.testwithme.flow.commands

import arrow.core.Either
import com.github.aivanovski.testwithme.entity.exception.FlowExecutionException
import com.github.aivanovski.testwithme.flow.runner.ExecutionContext
import java.lang.StringBuilder

class Broadcast(
    private val packageName: String,
    private val action: String,
    private val data: Map<String, String>
) : ExecutableStepCommand<Unit> {

    override fun describe(): String {
        return StringBuilder()
            .apply {
                append("Broadcast: $packageName/$action")
                if (data.isNotEmpty()) {
                    append(" [")

                    for ((key, value) in data.entries) {
                        if (!endsWith("[")) {
                            append(", ")
                        }
                        append("$key=$value")
                    }

                    append("]")
                }
            }
            .toString()
    }

    override suspend fun <NodeType> execute(
        context: ExecutionContext<NodeType>
    ): Either<FlowExecutionException, Unit> {
        return context.driver.sendBroadcast(
            packageName = packageName,
            action = action,
            data = data
        )
    }
}