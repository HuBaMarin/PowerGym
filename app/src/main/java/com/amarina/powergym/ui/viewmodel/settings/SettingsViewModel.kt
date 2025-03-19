package com.amarina.powergym.ui.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarina.powergym.database.entities.Preferencia
import com.amarina.powergym.repository.PreferenciaRepository
import com.amarina.powergym.utils.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferenciaRepository: PreferenciaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _preferenciaState = MutableStateFlow<PreferenciaState>(PreferenciaState.Loading)
    val preferenciaState: StateFlow<PreferenciaState> = _preferenciaState

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents

    init {
        loadPreferencias()
    }

    private fun loadPreferencias() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            _preferenciaState.value = PreferenciaState.Error("No hay usuario en sesión")
            return
        }

        viewModelScope.launch {
            _preferenciaState.value = PreferenciaState.Loading

            preferenciaRepository.getPreferenciasSync(userId)
                .onSuccess { preferencia ->
                    if (preferencia != null) {
                        _preferenciaState.value = PreferenciaState.Success(preferencia)
                    } else {
                        // Crear preferencias por defecto si no existen
                        val defaultPreferencia = Preferencia(usuarioId = userId)
                        _preferenciaState.value = PreferenciaState.Success(defaultPreferencia)
                    }
                }
                .onFailure { exception ->
                    _preferenciaState.value =
                        PreferenciaState.Error(exception.message ?: "Error desconocido")
                }
        }
    }

    fun updateNotificaciones(enabled: Boolean) {
        val currentState = _preferenciaState.value
        if (currentState is PreferenciaState.Success) {
            val updatedPreferencia = currentState.preferencia.copy(notificacionesHabilitadas = enabled)
            _preferenciaState.value = PreferenciaState.Success(updatedPreferencia)
        }
    }

    fun updateTema(darkMode: Boolean) {
        val currentState = _preferenciaState.value
        if (currentState is PreferenciaState.Success) {
            val updatedPreferencia = currentState.preferencia.copy(temaDark = darkMode)
            _preferenciaState.value = PreferenciaState.Success(updatedPreferencia)
        }
    }

    fun updateRecordatorios(enabled: Boolean) {
        val currentState = _preferenciaState.value
        if (currentState is PreferenciaState.Success) {
            val updatedPreferencia = currentState.preferencia.copy(recordatorios = enabled)
            _preferenciaState.value = PreferenciaState.Success(updatedPreferencia)
        }
    }

    fun updateFrecuencia(frecuencia: String) {
        val currentState = _preferenciaState.value
        if (currentState is PreferenciaState.Success) {
            val updatedPreferencia = currentState.preferencia.copy(frecuencia = frecuencia)
            _preferenciaState.value = PreferenciaState.Success(updatedPreferencia)
        }
    }

    fun saveSettings() {
        val currentState = _preferenciaState.value
        if (currentState is PreferenciaState.Success) {
            viewModelScope.launch {
                _saveState.value = SaveState.Saving

                val preferencia = currentState.preferencia
                val result = if (preferencia.usuarioId > 0) {
                    preferenciaRepository.savePreferencias(preferencia)
                } else {
                    preferenciaRepository.updatePreferencias(preferencia)
                        .map { 0L }
                }

                result
                    .onSuccess {
                        _saveState.value = SaveState.Success
                        _navigationEvents.emit(NavigationEvent.Back)
                    }
                    .onFailure { exception ->
                        _saveState.value = SaveState.Error(exception.message ?: "Error desconocido")
                    }
            }
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.Back)
        }
    }

    sealed class PreferenciaState {
        object Loading : PreferenciaState()
        data class Success(val preferencia: Preferencia) : PreferenciaState()
        data class Error(val message: String) : PreferenciaState()
    }

    sealed class SaveState {
        object Idle : SaveState()
        object Saving : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }

    sealed class NavigationEvent {
        object Back : NavigationEvent()
    }
}
