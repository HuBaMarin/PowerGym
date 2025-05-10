package com.amarina.powergym.ui.factory.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amarina.powergym.database.dao.EjercicioDao
import com.amarina.powergym.ui.viewmodel.main.MainViewModel

class MainViewModelFactory(
    private val ejercicioDao: EjercicioDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(ejercicioDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}