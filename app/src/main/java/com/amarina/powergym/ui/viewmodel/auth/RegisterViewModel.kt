package com.amarina.powergym.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarina.powergym.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents

    fun register(email: String, password: String, nombre: String = "") {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            userRepository.register(email, password, nombre)
                .onSuccess { _ ->
                    _registerState.value = RegisterState.Success
                    _navigationEvents.emit(NavigationEvent.ToLogin)
                }
                .onFailure { exception ->
                    _registerState.value =
                        RegisterState.Error(exception.message ?: "Error desconocido")
                }
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.Back)
        }
    }

    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        object Success : RegisterState()
        data class Error(val message: String) : RegisterState()
    }

    sealed class NavigationEvent {
        object ToLogin : NavigationEvent()
        object Back : NavigationEvent()
    }
}
