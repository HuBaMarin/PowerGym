package com.amarina.powergym.ui.viewmodel.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amarina.powergym.repository.EstadisticaRepository
import com.amarina.powergym.ui.statistics.StatisticsViewModel
import com.amarina.powergym.utils.SessionManager

class StatisticsViewModelFactory(
    private val estadisticaRepository: EstadisticaRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            return StatisticsViewModel(estadisticaRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
