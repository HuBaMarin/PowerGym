package com.amarina.powergym.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amarina.powergym.repository.EjercicioRepository
import com.amarina.powergym.repository.EstadisticaRepository
import com.amarina.powergym.utils.SessionManager
class StatisticsViewModelFactory(
    private val estadisticaRepository: EstadisticaRepository,
    private val ejercicioRepository: EjercicioRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticsViewModel(
                estadisticaRepository,
                ejercicioRepository,
                sessionManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
