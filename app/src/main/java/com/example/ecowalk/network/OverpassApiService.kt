package com.example.ecowalk.network

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// Overpass API Request/Response Models
data class OverpassRequest(
    val data: String  // Overpass QL query as string
)

data class OverpassResponse(
    val version: Double,
    val elements: List<OverpassElement>
)

data class OverpassElement(
    val type: String,  // "node", "way", "relation"
    val id: Long,
    val lat: Double? = null,
    val lon: Double? = null,
    val tags: Map<String, String>? = null,
    val nodes: List<Long>? = null,  // For ways
    val geometry: List<OverpassNode>? = null  // When geometry is included
)

data class OverpassNode(
    val lat: Double,
    val lon: Double
)

// Overpass API Service
interface OverpassApiService {
    
    /**
     * Query Overpass API for green spaces within a bounding box
     * @param query Overpass QL query string
     */
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("interpreter")
    suspend fun query(@Body query: String): OverpassResponse
}

/**
 * Helper to build Overpass QL queries for green spaces
 */
object OverpassQueryBuilder {
    
    /**
     * Build query for green spaces in a bounding box
     * @param south minimum latitude
     * @param west minimum longitude
     * @param north maximum latitude
     * @param east maximum longitude
     * @return Overpass QL query string
     */
    fun buildGreenSpaceQuery(south: Double, west: Double, north: Double, east: Double): String {
        return """
            [out:json][timeout:25];
            (
              way["leisure"="park"]($south,$west,$north,$east);
              way["natural"="wood"]($south,$west,$north,$east);
              way["landuse"="forest"]($south,$west,$north,$east);
              way["landuse"="grass"]($south,$west,$north,$east);
              way["landuse"="meadow"]($south,$west,$north,$east);
              way["leisure"="garden"]($south,$west,$north,$east);
              way["natural"="scrub"]($south,$west,$north,$east);
            );
            out geom;
        """.trimIndent()
    }
}
