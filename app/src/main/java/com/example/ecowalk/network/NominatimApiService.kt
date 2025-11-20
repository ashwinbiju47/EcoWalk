package com.example.ecowalk.network

import retrofit2.http.GET
import retrofit2.http.Query

// Nominatim Geocoding Response Models
data class NominatimResponse(
    val place_id: Long,
    val lat: String,
    val lon: String,
    val display_name: String,
    val type: String,
    val importance: Double
)

// Nominatim API Service for Geocoding
interface NominatimApiService {
    
    /**
     * Search for a location by name
     * @param query Location name to search for
     * @param format Response format (default: json)
     * @param limit Maximum number of results
     */
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5,
        @Query("addressdetails") addressDetails: Int = 1
    ): List<NominatimResponse>
}
