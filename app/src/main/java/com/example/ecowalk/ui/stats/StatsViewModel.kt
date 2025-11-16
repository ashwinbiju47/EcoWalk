package com.example.ecowalk.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowalk.repository.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class StatsViewModel(
    private val repo: StatsRepository
) : ViewModel() {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val today get() = LocalDate.now()
    private val weekStart get() = today.minusDays(6)

    private val _dailySteps = MutableStateFlow(0)
    val dailySteps: StateFlow<Int> = _dailySteps

    private val _weeklySteps = MutableStateFlow(0)
    val weeklySteps: StateFlow<Int> = _weeklySteps

    private val _carbonOffset = MutableStateFlow(0.0)
    val carbonOffset: StateFlow<Double> = _carbonOffset

    private val _stepHistory = MutableStateFlow<List<StepEntry>>(emptyList())
    val stepHistory: StateFlow<List<StepEntry>> = _stepHistory

    fun refreshStats() {
        viewModelScope.launch {
            val date = today.format(formatter)
            val start = weekStart.format(formatter)
            val end = today.format(formatter)

            _dailySteps.value = repo.getDailySteps(date)
            _weeklySteps.value = repo.getWeeklySteps(start, end)
            _carbonOffset.value = repo.getCarbonOffset(_weeklySteps.value)
            _stepHistory.value = repo.getHistory()
        }
    }

    fun addManualSteps(steps: Int) {
        viewModelScope.launch {
            val date = today.format(formatter)
            repo.addSteps(date, steps)
            refreshStats()
        }
    }

    data class StepEntry(
        val date: String,
        val steps: Int
    )
}
