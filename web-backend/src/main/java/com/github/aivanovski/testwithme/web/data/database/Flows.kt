package com.github.aivanovski.testwithme.web.data.database

import com.github.aivanovski.testwithme.web.entity.Flow
import com.github.aivanovski.testwithme.web.entity.Uid
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object FlowsTable : LongIdTable() {
    val uid = varchar("uid", 64)
    val projectUid = varchar("project_uid", 64)
    val name = varchar("name", 128)
    val path = varchar("path", 128)
}

class FlowEntity(id: EntityID<Long>) : Entity<Long>(id) {
    var uid by FlowsTable.uid
    var projectUid by FlowsTable.projectUid
    var name by FlowsTable.name
    var path by FlowsTable.path

    companion object : EntityClass<Long, FlowEntity>(FlowsTable)
}

class FlowDao {

    fun getAll(): List<Flow> = transaction {
        FlowEntity.all().map { it.convertToFlow() }
    }

    fun insert(flow: Flow): Flow = transaction {
        FlowEntity.new {
            uid = flow.uid.toString()
            projectUid = flow.projectUid.toString()
            name = flow.name
            path = flow.path
        }.convertToFlow()
    }

    private fun FlowEntity.convertToFlow(): Flow {
        return Flow(
            id = id.value,
            uid = Uid.fromString(uid),
            projectUid = Uid.fromString(projectUid),
            name = name,
            path = path
        )
    }
}