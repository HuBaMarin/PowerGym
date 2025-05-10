package com.amarina.powergym.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarina.powergym.utils.SessionManager
import com.amarina.powergym.utils.crypto.AdminAuthManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.amarina.powergym.repository.UserRepository

class LoginViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val adminAuthManager: AdminAuthManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents

    fun login(email: String, password: String) {
        viewModelScope.launch {
            if (sessionManager.estaCuentaBloqueada()) {
                _loginState.value = LoginState.Locked
                return@launch
            }

            _loginState.value = LoginState.Loading

            // Check if this is an admin login attempt
            if (email.endsWith("@powergym.com")) {
                // For admin users, redirect to admin verification screen to enter PIN
                _loginState.value = LoginState.AdminRedirect(email, password)
                _navigationEvents.emit(NavigationEvent.ToAdminVerification)
                return@launch
            }

            userRepository.iniciarSesion(email, password)
                .onSuccess { user ->
                    sessionManager.registrarIntentoSesion(
                        exitoso = true,
                    )
                    sessionManager.guardarSesionUsuario(user)

                    if (user.rol.equals("admin", ignoreCase = true)) {
                        _loginState.value =
                            LoginState.AdminSuccess("Login exitoso como administrador")
                        _navigationEvents.emit(NavigationEvent.ToMain)
                    } else {
                        _loginState.value = LoginState.Success("Login exitoso")
                        _navigationEvents.emit(NavigationEvent.ToMain)
                    }
                }
                .onFailure { exception ->
                    sessionManager.registrarIntentoSesion(exitoso = false)

                    // Check if account got locked due to this failed attempt
                    if (sessionManager.estaCuentaBloqueada()) {
                        _loginState.value = LoginState.Locked
                    } else {
                        _loginState.value =
                            LoginState.Error(exception.message ?: "Error desconocido")
                    }
                }
        }
    }

    fun navigateToRegister() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.ToRegister)
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val message: String) : LoginState()
        data class AdminSuccess(val message: String) : LoginState()
        object Locked : LoginState()
        data class Error(val message: String) : LoginState()
        data class AdminRedirect(val email: String, val password: String) : LoginState()
    }

    sealed class NavigationEvent {
        object ToMain : NavigationEvent()
        object ToRegister : NavigationEvent()

        // Kept for backward compatibility, no longer used
        object ToAdminVerification : NavigationEvent()
    }
}