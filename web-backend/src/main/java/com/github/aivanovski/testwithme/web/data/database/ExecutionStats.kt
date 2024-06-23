package com.github.aivanovski.testwithme.web.data.database

import com.github.aivanovski.testwithme.web.entity.Timestamp
import com.github.aivanovski.testwithme.web.entity.Uid
import com.github.aivanovski.testwithme.web.entity.ExecutionStat
import com.github.aivanovski.testwithme.web.extensions.asBoolean
import com.github.aivanovski.testwithme.web.extensions.asInt
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object ExecutionStatsTable : LongIdTable() {
    val flowUid = varchar("flow_uid", 64)
    val userUid = varchar("user_uid", 64)
    val executionTime = varchar("execution_time", 32)
    val isSuccess = integer("is_success")
    val result = varchar("result", 1024)
}

class ExecutionStatEntity(id: EntityID<Long>) : Entity<Long>(id) {
    var flowUid by ExecutionStatsTable.flowUid
    var userUid by ExecutionStatsTable.userUid
    var executionTime by ExecutionStatsTable.executionTime
    var isSuccess by ExecutionStatsTable.isSuccess
    var result by ExecutionStatsTable.result

    companion object : EntityClass<Long, ExecutionStatEntity>(ExecutionStatsTable)
}

class ExecutionStatDao {

    fun getAll(): List<ExecutionStat> = transaction {
        ExecutionStatEntity.all().map { it.readRow() }
    }

    fun insert(stat: ExecutionStat): ExecutionStat = transaction {
        ExecutionStatEntity.new {
            flowUid = stat.flowUid.toString()
            userUid = stat.userUid.toString()
            executionTime = stat.executionTime.toString()
            isSuccess = stat.isSuccess.asInt()
            result = stat.result
        }.readRow()
    }

    private fun ExecutionStatEntity.readRow(): ExecutionStat {
        return ExecutionStat(
            id = id.value,
            flowUid = Uid.fromString(flowUid),
            userUid = Uid.fromString(userUid),
            executionTime = Timestamp.fromString(executionTime),
            isSuccess = isSuccess.asBoolean(),
            result = result
        )
    }
}