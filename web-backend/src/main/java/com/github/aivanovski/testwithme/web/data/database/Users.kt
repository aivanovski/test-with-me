package com.github.aivanovski.testwithme.web.data.database

import com.github.aivanovski.testwithme.web.entity.Uid
import com.github.aivanovski.testwithme.web.entity.User
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object UsersTable : LongIdTable() {
    val uid = varchar("uid", 64)
    val name = varchar("name", 128)
    val password = varchar("password", 64) // TODO: should be stored hashed and salted
}

class UserEntity(id: EntityID<Long>) : Entity<Long>(id) {
    var uid by UsersTable.uid
    var name by UsersTable.name
    var password by UsersTable.password

    companion object : EntityClass<Long, UserEntity>(UsersTable)
}

class UserDao {

    fun getAll(): List<User> = transaction {
        UserEntity.all().map { it.convertToUser() }
    }

    fun findByUid(uid: String): List<User> = transaction {
        UserEntity.find { (UsersTable.uid eq uid) }
            .map { it.convertToUser() }
    }

    fun findByName(name: String): List<User> = transaction {
        UserEntity.find { (UsersTable.name eq name) }
            .map { it.convertToUser() }
    }

    fun insert(user: User): User = transaction {
        UserEntity.new {
            uid = user.uid.toString()
            name = user.name
            password = user.password
        }.convertToUser()
    }

    private fun UserEntity.convertToUser(): User {
        return User(
            id = id.value,
            uid = Uid.fromString(uid),
            name = name,
            password = password
        )
    }
}