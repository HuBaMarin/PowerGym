package com.amarina.powergym.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarina.powergym.utils.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.amarina.powergym.repository.UserRepository

class LoginViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents

    fun login(email: String, password: String) {

        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            userRepository.login(email, password)
                .onSuccess { user ->
                    sessionManager.saveUserSession(user)
                    _loginState.value = LoginState.Success
                    _navigationEvents.emit(NavigationEvent.ToMain)
                }
                .onFailure { exception ->
                    _loginState.value = LoginState.Error(exception.message ?: "Error desconocido")
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
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }

    sealed class NavigationEvent {
        object ToMain : NavigationEvent()
        object ToRegister : NavigationEvent()
    }
}
