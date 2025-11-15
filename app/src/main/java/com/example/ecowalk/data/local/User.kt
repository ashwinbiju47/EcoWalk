package com.example.ecowalk.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class User(
    @PrimaryKey val firebaseUid: String,
    val email: String
)
