package com.amarina.powergym.ui.viewmodel.ejercicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amarina.powergym.repository.EjercicioRepository
import com.amarina.powergym.ui.ejercicio.EjercicioDetailViewModel

class EjercicioDetailViewModelFactory(
    private val ejercicioRepository: EjercicioRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EjercicioDetailViewModel::class.java)) {
            return EjercicioDetailViewModel(ejercicioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
