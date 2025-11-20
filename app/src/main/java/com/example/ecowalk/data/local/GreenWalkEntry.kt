package com.example.ecowalk.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "green_walk_entries")
data class GreenWalkEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String,  // ISO format: "2025-11-20"
    val startLocationName: String,
    val endLocationName: String,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val userSteps: Int = 0,  // Optional, manually entered
    val totalDistanceKm: Double,
    val greenExposurePercentage: Double,
    val routePolyline: String  // Encoded polyline from OSRM
)
