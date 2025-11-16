package com.example.ecowalk.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "steps")
data class StepEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val steps: Int
)

