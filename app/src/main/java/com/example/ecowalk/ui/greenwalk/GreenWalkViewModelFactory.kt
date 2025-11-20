package com.example.ecowalk.ui.greenwalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ecowalk.data.repository.GreenWalkRepository

class GreenWalkViewModelFactory(
    private val repository: GreenWalkRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GreenWalkViewModel::class.java)) {
            return GreenWalkViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
