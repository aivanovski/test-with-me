package com.github.aivanovski.testwithme.web.data.database

import com.github.aivanovski.testwithme.web.di.GlobalInjector.get
import com.github.aivanovski.testwithme.web.domain.usecases.InitializeDefaultDataUseCase
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun configureDatabase() {
    Database.connect(createConnection())

    transaction {
        SchemaUtils.create(ProjectsTable, FlowsTable, UsersTable)
    }

    val initDataUseCase: InitializeDefaultDataUseCase = get()

    initDataUseCase.initializeDefaultDataIfNeed()
        .onLeft {
            throw IllegalStateException()
        }
}

private fun createConnection(): HikariDataSource {
    val config = HikariConfig()
        .apply {
            driverClassName = "org.h2.Driver"
            jdbcUrl = "jdbc:h2:file:./app-db/test-with-me"
            maximumPoolSize = 3
            isAutoCommit = true
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
    return HikariDataSource(config)
}
