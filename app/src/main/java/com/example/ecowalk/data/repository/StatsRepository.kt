package com.example.ecowalk.repository

import com.example.ecowalk.data.local.StepDao
import com.example.ecowalk.data.local.StepEntry
import com.example.ecowalk.ui.stats.StatsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatsRepository(
    private val dao: StepDao
) {

    suspend fun addSteps(date: String, steps: Int) {
        withContext(Dispatchers.IO) {
            dao.insert(StepEntry(date = date, steps = steps))
        }
    }

    suspend fun getDailySteps(date: String): Int {
        return withContext(Dispatchers.IO) {
            dao.getDailyTotal(date) ?: 0
        }
    }

    suspend fun getWeeklySteps(start: String, end: String): Int {
        return withContext(Dispatchers.IO) {
            dao.getWeeklyTotal(start, end) ?: 0
        }
    }

    suspend fun getCarbonOffset(steps: Int): Double {
        return steps * 0.00004
    }

    suspend fun getHistory(): List<StatsViewModel.StepEntry> {
        return withContext(Dispatchers.IO) {
            dao.getAllEntries()  // You implement this in Dao
                .sortedByDescending { it.date }   // newest first
                .map { entry ->
                    StatsViewModel.StepEntry(
                        date = entry.date,
                        steps = entry.steps
                    )
                }
        }
    }
}
