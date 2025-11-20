package com.example.ecowalk.utils

import com.google.maps.android.PolyUtil
import kotlin.math.*

/**
 * Utility class for route analysis and geometric calculations
 */
object RouteAnalyzer {
    
    /**
     * Decode a polyline string into a list of lat/lng points
     * Uses Google's PolyUtil for polyline decoding
     */
    fun decodePolyline(encoded: String): List<LatLng> {
        return PolyUtil.decode(encoded).map { LatLng(it.latitude, it.longitude) }
    }
    
    /**
     * Calculate distance between two points in kilometers using Haversine formula
     */
    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371.0 // km
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * Check if a point is inside a polygon using ray casting algorithm
     */
    fun isPointInPolygon(point: LatLng, polygon: List<LatLng>): Boolean {
        var inside = false
        var j = polygon.size - 1
        
        for (i in polygon.indices) {
            val xi = polygon[i].lng
            val yi = polygon[i].lat
            val xj = polygon[j].lng
            val yj = polygon[j].lat
            
            val intersect = ((yi > point.lat) != (yj > point.lat)) &&
                    (point.lng < (xj - xi) * (point.lat - yi) / (yj - yi) + xi)
            
            if (intersect) inside = !inside
            j = i
        }
        
        return inside
    }
    
    /**
     * Calculate the bounding box for a list of coordinates
     * Returns [south, west, north, east]
     */
    fun getBoundingBox(points: List<LatLng>, padding: Double = 0.01): BoundingBox {
        val lats = points.map { it.lat }
        val lngs = points.map { it.lng }
        
        return BoundingBox(
            south = lats.minOrNull()!! - padding,
            west = lngs.minOrNull()!! - padding,
            north = lats.maxOrNull()!! + padding,
            east = lngs.maxOrNull()!! + padding
        )
    }
    
    /**
     * Check if a line segment intersects with a polygon
     * Simplified check: checks if either endpoint is inside the polygon
     * or if the midpoint is inside
     */
    fun doesSegmentIntersectPolygon(
        start: LatLng,
        end: LatLng,
        polygon: List<LatLng>
    ): Boolean {
        // Check endpoints
        if (isPointInPolygon(start, polygon) || isPointInPolygon(end, polygon)) {
            return true
        }
        
        // Check midpoint
        val midpoint = LatLng((start.lat + end.lat) / 2, (start.lng + end.lng) / 2)
        return isPointInPolygon(midpoint, polygon)
    }
}

/**
 * Simple LatLng data class
 */
data class LatLng(
    val lat: Double,
    val lng: Double
)

/**
 * Bounding box data class
 */
data class BoundingBox(
    val south: Double,
    val west: Double,
    val north: Double,
    val east: Double
)
