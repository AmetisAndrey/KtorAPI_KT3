package com.example.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

object DatabaseFactory {
    private lateinit var dataSource: HikariDataSource

    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/ktor_users"
            username = System.getenv("DB_USER") ?: "postgres"
            password = System.getenv("DB_PASSWORD") ?: "postgres"
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = true
            transactionIsolation = "TRANSACTION_READ_COMMITTED"
        }
        dataSource = HikariDataSource(config)
        dataSource.connection.use { conn ->
            conn.createStatement().use { st ->
                st.execute("""
                    CREATE TABLE IF NOT EXISTS app_users (
                        id SERIAL PRIMARY KEY,
                        username VARCHAR(80) UNIQUE NOT NULL,
                        password_hash VARCHAR(100) NOT NULL,
                        role VARCHAR(16) NOT NULL DEFAULT 'USER'
                    )
                """.trimIndent())
            }
        }
    }

    fun <T> withConnection(block: (Connection) -> T): T =
        dataSource.connection.use(block)
}
