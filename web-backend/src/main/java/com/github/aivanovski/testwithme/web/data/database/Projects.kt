package com.github.aivanovski.testwithme.web.data.database

import com.github.aivanovski.testwithme.web.entity.Project
import com.github.aivanovski.testwithme.web.entity.Uid
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object ProjectsTable : LongIdTable() {
    val uid = varchar("uid", 64)
    val userUid = varchar("user_uid", 64)
    val name = varchar("name", 128)
}

class ProjectEntity(id: EntityID<Long>) : Entity<Long>(id) {
    var uid by ProjectsTable.uid
    var userUid by ProjectsTable.userUid
    var name by ProjectsTable.name

    companion object : EntityClass<Long, ProjectEntity>(ProjectsTable)
}

class ProjectDao {

    fun getAll(): List<Project> = transaction {
        ProjectEntity.all().map { it.convertToProject() }
    }

    fun getByUserUid(userUid: Uid): List<Project> = transaction {
        val uidText = userUid.toString()

        ProjectEntity.find { (ProjectsTable.userUid eq uidText) }
            .map { it.convertToProject() }
    }

    fun getByUid(uid: Uid): Project? = transaction {
        val uidText = uid.toString()
        ProjectEntity.find { (ProjectsTable.uid eq uidText) }
            .map { it.convertToProject() }
            .firstOrNull()
    }

    fun insert(project: Project): Project = transaction {
        ProjectEntity.new {
            uid = project.uid.toString()
            userUid = project.userUid.toString()
            name = project.name
        }.convertToProject()
    }

    private fun ProjectEntity.convertToProject(): Project {
        return Project(
            id = id.value,
            uid = Uid.fromString(uid),
            userUid = Uid.fromString(userUid),
            name = name
        )
    }
}