package com.github.aivanovski.testwithme.web.data.database

import com.github.aivanovski.testwithme.web.entity.Group
import com.github.aivanovski.testwithme.web.entity.Uid
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object GroupsTable : LongIdTable() {
    val uid = varchar("uid", 64)
    val parentUid = varchar("parent_uid", 64).nullable()
    val projectUid = varchar("project_uid", 64)
    val name = varchar("name", 128)
}

class GroupEntity(id: EntityID<Long>) : Entity<Long>(id) {
    var uid by GroupsTable.uid
    var parentUid by GroupsTable.parentUid
    var projectUid by GroupsTable.projectUid
    var name by GroupsTable.name

    companion object : EntityClass<Long, GroupEntity>(GroupsTable)
}

class GroupDao {

    fun getAll(): List<Group> = transaction {
        GroupEntity.all().map { it.readRow() }
    }

    fun getByProjectUid(projectUid: Uid): List<Group> = transaction {
        val uidText = projectUid.toString()
        GroupEntity.find { (GroupsTable.projectUid eq uidText) }
            .map { it.readRow() }
    }

    fun insert(group: Group): Group = transaction {
        GroupEntity.new {
            uid = group.uid.toString()
            parentUid = group.parentUid?.toString()
            projectUid = group.projectUid.toString()
            name = group.name
        }.readRow()
    }

    private fun GroupEntity.readRow(): Group {
        return Group(
            id = id.value,
            uid = Uid.fromString(uid),
            parentUid = parentUid?.let { Uid.fromString(it) },
            projectUid = Uid.fromString(projectUid),
            name = name
        )
    }
}