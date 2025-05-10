package com.amarina.powergym.ui.viewholder.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarina.powergym.database.entities.Usuario
import com.amarina.powergym.repository.UserRepository
import com.amarina.powergym.utils.SessionManager
import com.amarina.powergym.utils.Utils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = sessionManager.obtenerIdUsuario()
        if (userId == -1) {
            _profileState.value = ProfileState.Error("No hay usuario en sesión")
            return
        }

        viewModelScope.launch {
            _profileState.value = ProfileState.Loading

            userRepository.obtenerUsuarioPorId(userId)
                .onSuccess { user ->
                    _profileState.value = ProfileState.Success(user)
                }
                .onFailure { exception ->
                    _profileState.value =
                        ProfileState.Error(exception.message ?: "Error desconocido")
                }
        }
    }

    fun updateUserName(name: String) {
        val currentState = _profileState.value
        if (currentState is ProfileState.Success) {
            val updatedUser = currentState.user.copy(nombre = name)
            _profileState.value = ProfileState.Success(updatedUser)
        }
    }

    fun updateUserEmail(email: String): Boolean {
        if (!Utils.esEmailValido(email)) {
            return false
        }

        val currentState = _profileState.value
        if (currentState is ProfileState.Success) {
            val updatedUser = currentState.user.copy(email = email)
            _profileState.value = ProfileState.Success(updatedUser)
        }
        return true
    }

    fun updateUserPassword(currentPassword: String, newPassword: String): Boolean {
        if (!Utils.esContrasenaValida(newPassword)) {
            return false
        }

        val currentState = _profileState.value
        if (currentState is ProfileState.Success) {
            val user = currentState.user
            if (user.password == currentPassword) {
                val updatedUser = user.copy(password = newPassword)
                _profileState.value = ProfileState.Success(updatedUser)
                return true
            }
        }
        return false
    }

    fun saveUserProfile() {
        val currentState = _profileState.value
        if (currentState is ProfileState.Success) {
            viewModelScope.launch {
                _updateState.value = UpdateState.Updating

                userRepository.actualizarUsuario(currentState.user)
                    .onSuccess {
                        _updateState.value = UpdateState.Success
                        sessionManager.guardarSesionUsuario(currentState.user)
                        _navigationEvents.emit(NavigationEvent.Back)
                    }
                    .onFailure { exception ->
                        _updateState.value =
                            UpdateState.Error(exception.message ?: "Error desconocido")
                    }
            }
        }
    }

    fun deleteUserAccount() {
        val currentState = _profileState.value
        if (currentState is ProfileState.Success) {
            viewModelScope.launch {
                userRepository.eliminarUsuario(currentState.user)
                    .onSuccess {
                        sessionManager.cerrarSesion()
                        _navigationEvents.emit(NavigationEvent.ToLogin)
                    }
                    .onFailure { exception ->
                        _updateState.value =
                            UpdateState.Error(exception.message ?: "Error al eliminar cuenta")
                    }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.cerrarSesion()
            _navigationEvents.emit(NavigationEvent.ToLogin)
        }
    }

    fun navigateToSettings() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.ToSettings)
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.Back)
        }
    }

    sealed class ProfileState {
        object Loading : ProfileState()
        data class Success(val user: Usuario) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    sealed class UpdateState {
        object Idle : UpdateState()
        object Updating : UpdateState()
        object Success : UpdateState()
        data class Error(val message: String) : UpdateState()
    }

    sealed class NavigationEvent {
        object Back : NavigationEvent()
        object ToSettings : NavigationEvent()
        object ToLogin : NavigationEvent()
    }
}
