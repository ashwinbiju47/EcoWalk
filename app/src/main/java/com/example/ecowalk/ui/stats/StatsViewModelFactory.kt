package com.example.ecowalk.ui.stats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ecowalk.data.local.UserDatabase
import com.example.ecowalk.repository.StatsRepository

class StatsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = UserDatabase.getDatabase(context)
        val repo = StatsRepository(db.stepDao())
        return StatsViewModel(repo) as T
    }
}
