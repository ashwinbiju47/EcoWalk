package com.example.ecowalk.data.repository

import com.example.ecowalk.data.local.GreenWalkDao
import com.example.ecowalk.data.local.GreenWalkEntry
import com.example.ecowalk.network.NominatimApiService
import com.example.ecowalk.network.OSRMApiService
import com.example.ecowalk.network.OverpassApiService
import com.example.ecowalk.network.OverpassQueryBuilder
import com.example.ecowalk.utils.BoundingBox
import com.example.ecowalk.utils.LatLng
import com.example.ecowalk.utils.RouteAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class GreenWalkRepository(
    private val dao: GreenWalkDao,
    private val osrmApi: OSRMApiService,
    private val overpassApi: OverpassApiService,
    private val nominatimApi: NominatimApiService
) {

    /**
     * Search for locations by name
     * Returns list of matching locations with coordinates
     */
    suspend fun searchLocation(query: String): Result<List<LocationResult>> = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank()) {
                return@withContext Result.success(emptyList())
            }
            
            val results = nominatimApi.search(query, limit = 5)
            val locations = results.map { response ->
                LocationResult(
                    name = response.display_name,
                    lat = response.lat.toDouble(),
                    lng = response.lon.toDouble()
                )
            }
            Result.success(locations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Analyze a walk between start and end location names
     * This method geocodes the location names first
     */
    suspend fun analyzeWalkByName(
        startLocationName: String,
        endLocationName: String
    ): Result<GreenWalkEntry> = withContext(Dispatchers.IO) {
        try {
            // Geocode start location
            val startResults = nominatimApi.search(startLocationName, limit = 1)
            if (startResults.isEmpty()) {
                return@withContext Result.failure(Exception("Could not find start location: $startLocationName"))
            }
            val startLocation = startResults.first()
            
            // Geocode end location
            val endResults = nominatimApi.search(endLocationName, limit = 1)
            if (endResults.isEmpty()) {
                return@withContext Result.failure(Exception("Could not find end location: $endLocationName"))
            }
            val endLocation = endResults.first()
            
            // Now analyze the walk with coordinates
            analyzeWalk(
                startLocationName = startLocation.display_name,
                endLocationName = endLocation.display_name,
                startLat = startLocation.lat.toDouble(),
                startLng = startLocation.lon.toDouble(),
                endLat = endLocation.lat.toDouble(),
                endLng = endLocation.lon.toDouble()
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Analyze a walk between start and end locations
     * Returns the walk entry with calculated green exposure percentage
     */
    suspend fun analyzeWalk(
        startLocationName: String,
        endLocationName: String,
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        userSteps: Int = 0
    ): Result<GreenWalkEntry> = withContext(Dispatchers.IO) {
        try {
            // 1. Get route from OSRM
            val coordinates = "$startLng,$startLat;$endLng,$endLat"
            val routeResponse = osrmApi.getRoute(coordinates)
            
            if (routeResponse.code != "Ok" || routeResponse.routes.isEmpty()) {
                return@withContext Result.failure(Exception("Failed to get route"))
            }
            
            val route = routeResponse.routes.first()
            val distanceKm = route.distance / 1000.0  // Convert meters to km
            
            // Get and validate the polyline geometry
            val polyline = route.geometry
            
            // Validate polyline before processing
            if (polyline.isBlank()) {
                return@withContext Result.failure(Exception("Empty polyline received from OSRM"))
            }
            
            // 2. Decode polyline to get route points
            val routePoints = RouteAnalyzer.decodePolyline(polyline)
            
            if (routePoints.isEmpty()) {
                return@withContext Result.failure(Exception("Failed to decode route"))
            }
            
            // 3. Get bounding box for the route
            val bbox = RouteAnalyzer.getBoundingBox(routePoints)
            
            // Validate bounding box coordinates
            if (!isValidBoundingBox(bbox)) {
                return@withContext Result.failure(Exception("Invalid bounding box calculated from route"))
            }
            
            // 4. Query Overpass API for green spaces in the bounding box
            val query = OverpassQueryBuilder.buildGreenSpaceQuery(
                bbox.south, bbox.west, bbox.north, bbox.east
            )
            
            // Query Overpass API - don't add "data=" prefix here, just pass the query
            val greenSpaces = try {
                overpassApi.query("data=$query")
            } catch (e: Exception) {
                // If Overpass query fails, return empty results instead of failing the whole analysis
                // This allows the route analysis to continue even if green space data is unavailable
                com.example.ecowalk.network.OverpassResponse(version = 0.0, elements = emptyList())
            }
            
            // 5. Calculate green exposure percentage
            val greenPercentage = calculateGreenExposure(routePoints, greenSpaces.elements)
            
            // 6. Create walk entry
            val walk = GreenWalkEntry(
                date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                startLocationName = startLocationName,
                endLocationName = endLocationName,
                startLat = startLat,
                startLng = startLng,
                endLat = endLat,
                endLng = endLng,
                userSteps = userSteps,
                totalDistanceKm = distanceKm,
                greenExposurePercentage = greenPercentage,
                routePolyline = polyline
            )
            
            Result.success(walk)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save a walk entry to the database
     */
    suspend fun saveWalk(walk: GreenWalkEntry): Long {
        return withContext(Dispatchers.IO) {
            dao.insert(walk)
        }
    }
    
    /**
     * Get all saved walks
     */
    suspend fun getAllWalks(): List<GreenWalkEntry> {
        return withContext(Dispatchers.IO) {
            dao.getAllWalks()
        }
    }
    
    /**
     * Calculate green exposure percentage for a route
     * Algorithm: For each route segment, check if it passes through green spaces
     */
    private fun calculateGreenExposure(
        routePoints: List<LatLng>,
        greenSpaces: List<com.example.ecowalk.network.OverpassElement>
    ): Double {
        if (routePoints.size < 2) return 0.0
        
        // Convert green space polygons
        val greenPolygons = greenSpaces.mapNotNull { element ->
            element.geometry?.map { node -> LatLng(node.lat, node.lon) }
        }.filter { it.size >= 3 }  // Valid polygons need at least 3 points
        
        if (greenPolygons.isEmpty()) return 0.0
        
        // Calculate total distance and green distance
        var totalDistance = 0.0
        var greenDistance = 0.0
        
        for (i in 0 until routePoints.size - 1) {
            val start = routePoints[i]
            val end = routePoints[i + 1]
            
            val segmentDistance = RouteAnalyzer.calculateDistance(
                start.lat, start.lng, end.lat, end.lng
            )
            
            totalDistance += segmentDistance
            
            // Check if this segment passes through any green space
            var isGreen = false
            for (polygon in greenPolygons) {
                if (RouteAnalyzer.doesSegmentIntersectPolygon(start, end, polygon)) {
                    isGreen = true
                    break
                }
            }
            
            if (isGreen) {
                greenDistance += segmentDistance
            }
        }
        
        return if (totalDistance > 0) {
            (greenDistance / totalDistance * 100.0).coerceIn(0.0, 100.0)
        } else {
            0.0
        }
    }
    
    /**
     * Validate that bounding box coordinates are within valid ranges
     */
    private fun isValidBoundingBox(bbox: BoundingBox): Boolean {
        return bbox.south in -90.0..90.0 &&
               bbox.north in -90.0..90.0 &&
               bbox.west in -180.0..180.0 &&
               bbox.east in -180.0..180.0 &&
               bbox.south < bbox.north &&
               bbox.west < bbox.east
    }
}

/**
 * Result from location search
 */
data class LocationResult(
    val name: String,
    val lat: Double,
    val lng: Double
)
