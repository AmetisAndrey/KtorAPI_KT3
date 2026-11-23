package com.example.repository

import com.example.database.DatabaseFactory
import com.example.models.UserDto
import java.sql.SQLException

data class StoredUser(val id: Int, val username: String, val passwordHash: String, val role: String) {
    fun toDto() = UserDto(id, username, role)
}

class UserRepository {
    fun findByUsername(username: String): StoredUser? = DatabaseFactory.withConnection { c ->
        c.prepareStatement("SELECT id, username, password_hash, role FROM app_users WHERE username = ?").use {
            it.setString(1, username)
            it.executeQuery().use { rs ->
                if (rs.next()) StoredUser(rs.getInt("id"), rs.getString("username"), rs.getString("password_hash"), rs.getString("role")) else null
            }
        }
    }

    fun findById(id: Int): StoredUser? = DatabaseFactory.withConnection { c ->
        c.prepareStatement("SELECT id, username, password_hash, role FROM app_users WHERE id = ?").use {
            it.setInt(1, id)
            it.executeQuery().use { rs ->
                if (rs.next()) StoredUser(rs.getInt("id"), rs.getString("username"), rs.getString("password_hash"), rs.getString("role")) else null
            }
        }
    }

    fun list(): List<UserDto> = DatabaseFactory.withConnection { c ->
        c.prepareStatement("SELECT id, username, role FROM app_users ORDER BY id").use { ps ->
            ps.executeQuery().use { rs ->
                buildList { while (rs.next()) add(UserDto(rs.getInt("id"), rs.getString("username"), rs.getString("role"))) }
            }
        }
    }

    fun create(username: String, passwordHash: String, role: String = "USER"): StoredUser =
        DatabaseFactory.withConnection { c ->
            c.prepareStatement(
                "INSERT INTO app_users(username, password_hash, role) VALUES (?, ?, ?) RETURNING id"
            ).use {
                it.setString(1, username); it.setString(2, passwordHash); it.setString(3, role)
                it.executeQuery().use { rs ->
                    rs.next()
                    StoredUser(rs.getInt(1), username, passwordHash, role)
                }
            }
        }

    fun updateUsername(id: Int, username: String): StoredUser? = DatabaseFactory.withConnection { c ->
        c.prepareStatement("UPDATE app_users SET username = ? WHERE id = ?").use {
            it.setString(1, username); it.setInt(2, id)
            if (it.executeUpdate() == 0) null else findById(id)
        }
    }

    fun delete(id: Int): Boolean = DatabaseFactory.withConnection { c ->
        c.prepareStatement("DELETE FROM app_users WHERE id = ?").use {
            it.setInt(1, id); it.executeUpdate() > 0
        }
    }
}
