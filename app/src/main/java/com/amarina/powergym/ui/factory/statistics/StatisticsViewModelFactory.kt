package com.amarina.powergym.ui.factory.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amarina.powergym.database.dao.EjercicioDao
import com.amarina.powergym.database.dao.EstadisticaDao
import com.amarina.powergym.ui.viewmodel.statistics.StatisticsViewModel
import com.amarina.powergym.utils.SessionManager

class StatisticsViewModelFactory(
    private val estadisticaDao: EstadisticaDao,
    private val ejercicioDao: EjercicioDao,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticsViewModel(
                estadisticaDao,
                ejercicioDao,
                sessionManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}