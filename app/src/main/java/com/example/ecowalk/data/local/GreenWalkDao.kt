package com.example.ecowalk.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GreenWalkDao {
    
    @Insert
    suspend fun insert(walk: GreenWalkEntry): Long
    
    @Query("SELECT * FROM green_walk_entries ORDER BY date DESC, id DESC")
    suspend fun getAllWalks(): List<GreenWalkEntry>
    
    @Query("SELECT * FROM green_walk_entries WHERE id = :id")
    suspend fun getWalkById(id: Int): GreenWalkEntry?
    
    @Query("DELETE FROM green_walk_entries WHERE id = :id")
    suspend fun deleteWalkById(id: Int)
}
