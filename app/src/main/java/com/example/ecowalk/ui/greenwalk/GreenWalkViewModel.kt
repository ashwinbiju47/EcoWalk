package com.example.ecowalk.ui.greenwalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowalk.data.local.GreenWalkEntry
import com.example.ecowalk.data.repository.GreenWalkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Green Walk feature
 */
class GreenWalkViewModel(
    private val repository: GreenWalkRepository
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

    init {
        loadWalkHistory()
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
}
