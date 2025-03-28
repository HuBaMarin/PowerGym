package com.amarina.powergym.ui.settings.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amarina.powergym.repository.PreferenciaRepository
import com.amarina.powergym.ui.settings.SettingsViewModel
import com.amarina.powergym.utils.SessionManager

class SettingsViewModelFactory(
    private val preferenciaRepository: PreferenciaRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(preferenciaRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
