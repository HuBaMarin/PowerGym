package com.amarina.powergym.ui.factory.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amarina.powergym.database.dao.EjercicioDao
import com.amarina.powergym.ui.viewmodel.admin.AdminExerciseViewModel

class AdminExerciseViewModelFactory(
    private val ejercicioDao: EjercicioDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminExerciseViewModel::class.java)) {
            return AdminExerciseViewModel(ejercicioDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}