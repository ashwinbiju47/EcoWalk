package com.example.ecowalk.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// OSRM Response Models
data class OSRMResponse(
    val code: String,
    val routes: List<OSRMRoute>
)

data class OSRMRoute(
    val geometry: String,  // Encoded polyline
    val distance: Double,  // Distance in meters
    val duration: Double   // Duration in seconds
)

// OSRM API Service
interface OSRMApiService {
    
    /**
     * Get walking route between two coordinates
     * @param coordinates in format: "lng1,lat1;lng2,lat2"
     * @param overview "full" to get complete route geometry
     * @param geometries "polyline" or "geojson"
     */
    @GET("route/v1/foot/{coordinates}")
    suspend fun getRoute(
        @Path("coordinates", encoded = true) coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "polyline"
    ): OSRMResponse
}
