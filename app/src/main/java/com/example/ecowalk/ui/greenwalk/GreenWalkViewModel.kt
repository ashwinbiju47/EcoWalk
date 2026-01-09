package com.example.ecowalk.ui.greenwalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowalk.data.local.GreenWalkEntry
import com.example.ecowalk.data.repository.GreenWalkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import android.location.Location
import com.example.ecowalk.utils.LocationClient
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import java.time.LocalDate

/**
 * ViewModel for Green Walk feature
 */
class GreenWalkViewModel(
    private val repository: GreenWalkRepository,
    private val locationClient: LocationClient
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<GreenWalkUiState>(GreenWalkUiState.Input)
    val uiState: StateFlow<GreenWalkUiState> = _uiState

    // Walk history
    private val _walkHistory = MutableStateFlow<List<GreenWalkEntry>>(emptyList())
    val walkHistory: StateFlow<List<GreenWalkEntry>> = _walkHistory

    // Current walk result
    private val _currentWalk = MutableStateFlow<GreenWalkEntry?>(null)
    val currentWalk: StateFlow<GreenWalkEntry?> = _currentWalk

    private var trackingJob: Job? = null

    init {
        loadWalkHistory()
    }

    /**
     * Start tracking location updates
     */
    fun startTracking() {
        trackingJob?.cancel()
        _uiState.value = GreenWalkUiState.Tracking()

        trackingJob = viewModelScope.launch {
            locationClient.getLocationUpdates(2000L)
                .catch { e ->
                    _uiState.value = GreenWalkUiState.Error("Location error: ${e.message}")
                }
                .collect { location ->
                    val currentState = _uiState.value
                    if (currentState is GreenWalkUiState.Tracking) {
                        val newLocations = currentState.currentLocations + location
                        val newDist = calculateDistance(newLocations)
                        _uiState.value = currentState.copy(
                            currentLocations = newLocations,
                            distanceKm = newDist
                        )
                    }
                }
        }
    }

    /**
     * Stop tracking and show results
     */
    fun stopTracking() {
        trackingJob?.cancel()
        val trackingState = _uiState.value as? GreenWalkUiState.Tracking ?: return

        val locations = trackingState.currentLocations
        if (locations.size < 2) {
            _uiState.value = GreenWalkUiState.Error("Not enough data to save walk")
            return
        }

        val start = locations.first()
        val end = locations.last()
        val distance = trackingState.distanceKm

        // Encode polyline
        val latLngs = locations.map { LatLng(it.latitude, it.longitude) }
        val polyline = PolyUtil.encode(latLngs)

        val entry = GreenWalkEntry(
            date = LocalDate.now().toString(),
            startLocationName = "Tracked Start",
            endLocationName = "Tracked End",
            startLat = start.latitude,
            startLng = start.longitude,
            endLat = end.latitude,
            endLng = end.longitude,
            userSteps = 0,
            totalDistanceKm = distance,
            greenExposurePercentage = 85.0, // Mock for now
            routePolyline = polyline
        )

        _currentWalk.value = entry
        _uiState.value = GreenWalkUiState.Results(entry)
    }

    private fun calculateDistance(locations: List<Location>): Double {
        if (locations.size < 2) return 0.0
        var dist = 0.0
        for (i in 0 until locations.size - 1) {
            dist += locations[i].distanceTo(locations[i + 1])
        }
        return dist / 1000.0 // Convert to km
    }

    /**
     * Analyze a walk between two location names
     * Geocoding happens automatically in the repository
     */
    fun analyzeWalk(
        startLocationName: String,
        endLocationName: String
    ) {
        viewModelScope.launch {
            _uiState.value = GreenWalkUiState.Analyzing

            val result = repository.analyzeWalkByName(
                startLocationName = startLocationName,
                endLocationName = endLocationName
            )

            result.fold(
                onSuccess = { walk ->
                    _currentWalk.value = walk
                    _uiState.value = GreenWalkUiState.Results(walk)
                },
                onFailure = { error ->
                    _uiState.value = GreenWalkUiState.Error(
                        error.message ?: "Failed to analyze walk"
                    )
                }
            )
        }
    }

    /**
     * Save the current walk to database
     */
    fun saveCurrentWalk(userSteps: Int = 0) {
        viewModelScope.launch {
            _currentWalk.value?.let { walk ->
                val updatedWalk = walk.copy(userSteps = userSteps)
                repository.saveWalk(updatedWalk)
                loadWalkHistory()
                resetToInput()
            }
        }
    }

    /**
     * Load walk history from database
     */
    private fun loadWalkHistory() {
        viewModelScope.launch {
            _walkHistory.value = repository.getAllWalks()
        }
    }

    /**
     * Reset to input state
     */
    fun resetToInput() {
        trackingJob?.cancel()
        _uiState.value = GreenWalkUiState.Input
        _currentWalk.value = null
    }
}

/**
 * UI State for Green Walk screen
 */
sealed class GreenWalkUiState {
    object Input : GreenWalkUiState()
    object Analyzing : GreenWalkUiState()
    data class Results(val walk: GreenWalkEntry) : GreenWalkUiState()
    data class Error(val message: String) : GreenWalkUiState()
    data class Tracking(
        val distanceKm: Double = 0.0,
        val startTime: Long = System.currentTimeMillis(),
        val currentLocations: List<Location> = emptyList()
    ) : GreenWalkUiState()
}
