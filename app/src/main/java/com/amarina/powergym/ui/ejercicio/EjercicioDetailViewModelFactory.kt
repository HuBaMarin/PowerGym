package com.amarina.powergym.ui.ejercicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amarina.powergym.repository.EjercicioRepository
import com.amarina.powergym.repository.EstadisticaRepository
import com.amarina.powergym.utils.SessionManager

class EjercicioDetailViewModelFactory(
    private val ejercicioRepository: EjercicioRepository,
    private val estadisticaRepository: EstadisticaRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EjercicioDetailViewModel::class.java)) {
            return EjercicioDetailViewModel(
                ejercicioRepository,
                estadisticaRepository,
                sessionManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

