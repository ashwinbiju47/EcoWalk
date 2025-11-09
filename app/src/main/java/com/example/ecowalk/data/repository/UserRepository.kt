package com.example.ecowalk.data.repository

import com.example.ecowalk.data.local.User
import com.example.ecowalk.data.local.UserDao

class UserRepository(private val userDao: UserDao) {
    suspend fun registerUser(email: String, password: String): Boolean {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) return false
        userDao.insertUser(User(email = email, password = password))
        return true
    }

    suspend fun loginUser(email: String, password: String): User? {
        return userDao.loginUser(email, password)
    }
}
