package com.example.repository
import com.example.models.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class UserRepository {
    private val sequence = AtomicInteger(0)
    private val users = ConcurrentHashMap<Int, User>()
    fun findAll(): List<User> = users.values.sortedBy { it.id }
    fun findById(id: Int): User? = users[id]
    fun create(request: CreateUserRequest): User {
        val id = sequence.incrementAndGet()
        return User(id, request.name.trim(), request.email.trim()).also { users[id] = it }
    }
    fun delete(id: Int): Boolean = users.remove(id) != null
}
