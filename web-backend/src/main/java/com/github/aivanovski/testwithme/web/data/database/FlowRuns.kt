package com.github.aivanovski.testwithme.web.data.database

import com.github.aivanovski.testwithme.web.entity.Timestamp
import com.github.aivanovski.testwithme.web.entity.Uid
import com.github.aivanovski.testwithme.web.entity.FlowRun
import com.github.aivanovski.testwithme.web.extensions.asBoolean
import com.github.aivanovski.testwithme.web.extensions.asInt
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object FlowRunsTable : LongIdTable() {
    val flowUid = varchar("flow_uid", 64)
    val userUid = varchar("user_uid", 64)
    val timestamp = varchar("timestamp", 32)
    val duration = long("duration")
    val isSuccess = integer("is_success")
    val result = varchar("result", 1024)
}

class FlowRunEntity(id: EntityID<Long>) : Entity<Long>(id) {
    var flowUid by FlowRunsTable.flowUid
    var userUid by FlowRunsTable.userUid
    var timestamp by FlowRunsTable.timestamp
    var duration by FlowRunsTable.duration
    var isSuccess by FlowRunsTable.isSuccess
    var result by FlowRunsTable.result

    companion object : EntityClass<Long, FlowRunEntity>(FlowRunsTable)
}

class FlowRunDao {

    fun getAll(): List<FlowRun> = transaction {
        FlowRunEntity.all().map { it.readRow() }
    }

    fun insert(flowRun: FlowRun): FlowRun = transaction {
        FlowRunEntity.new {
            flowUid = flowRun.flowUid.toString()
            userUid = flowRun.userUid.toString()
            timestamp = flowRun.timestamp.toString()
            duration = flowRun.durationInMillis
            isSuccess = flowRun.isSuccess.asInt()
            result = flowRun.result
        }.readRow()
    }

    private fun FlowRunEntity.readRow(): FlowRun {
        return FlowRun(
            id = id.value,
            flowUid = Uid.fromString(flowUid),
            userUid = Uid.fromString(userUid),
            timestamp = Timestamp.fromString(timestamp),
            durationInMillis = duration,
            isSuccess = isSuccess.asBoolean(),
            result = result
        )
    }
}