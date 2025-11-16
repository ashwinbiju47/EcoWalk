package com.example.ecowalk.data.local


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StepDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: StepEntry)

    @Query("SELECT SUM(steps) FROM steps WHERE date = :date")
    suspend fun getDailyTotal(date: String): Int?

    @Query("""
        SELECT SUM(steps)
        FROM steps
        WHERE date BETWEEN :start AND :end
    """)
    suspend fun getWeeklyTotal(start: String, end: String): Int?

    @Query("SELECT * FROM steps ORDER BY date DESC")
    suspend fun getAllEntries(): List<StepEntry>
}
