package com.amarina.powergym.ui.viewmodel.main.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amarina.powergym.repository.EjercicioRepository
import com.amarina.powergym.ui.viewmodel.main.MainViewModel

class MainViewModelFactory(
    private val ejercicioRepository: EjercicioRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(ejercicioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
