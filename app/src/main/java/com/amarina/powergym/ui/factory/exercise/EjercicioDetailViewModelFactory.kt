package com.amarina.powergym.ui.factory.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amarina.powergym.database.dao.EjercicioDao
import com.amarina.powergym.database.dao.EstadisticaDao
import com.amarina.powergym.ui.viewmodel.exercise.EjercicioDetailViewModel
import com.amarina.powergym.utils.SessionManager

class EjercicioDetailViewModelFactory(
    private val ejercicioDao: EjercicioDao,
    private val estadisticaDao: EstadisticaDao,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EjercicioDetailViewModel::class.java)) {
            return EjercicioDetailViewModel(
                ejercicioDao,
                estadisticaDao,
                sessionManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}