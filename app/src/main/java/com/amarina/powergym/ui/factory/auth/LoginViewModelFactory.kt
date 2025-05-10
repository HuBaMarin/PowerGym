package com.amarina.powergym.ui.factory.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amarina.powergym.repository.UserRepository
import com.amarina.powergym.ui.viewmodel.auth.LoginViewModel
import com.amarina.powergym.utils.SessionManager
import com.amarina.powergym.utils.crypto.AdminAuthManager

class LoginViewModelFactory(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val adminAuthManager: AdminAuthManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(userRepository, sessionManager, adminAuthManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}