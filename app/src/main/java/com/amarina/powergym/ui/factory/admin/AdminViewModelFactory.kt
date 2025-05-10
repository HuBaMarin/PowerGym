package com.amarina.powergym.ui.factory.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amarina.powergym.database.dao.UsuarioDao
import com.amarina.powergym.ui.viewmodel.admin.AdminViewModel
import com.amarina.powergym.utils.SessionManager

class AdminViewModelFactory(
    private val usuarioDao: UsuarioDao,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            return AdminViewModel(usuarioDao, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}