package com.amarina.powergym.ui.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarina.powergym.database.dao.UsuarioDao
import com.amarina.powergym.database.entities.Usuario
import com.amarina.powergym.utils.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val usuarioDao: UsuarioDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _users = MutableStateFlow<List<Usuario>>(emptyList())
    val users: StateFlow<List<Usuario>> = _users

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _users.value = usuarioDao.obtenerTodos()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error cargando usuarios"
            } finally {
                _loading.value = false
            }
        }
    }

    fun onEditUserClicked(userId: Int) {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.NavigateToUserEdit(userId))
        }
    }


    fun clearError() {
        _error.value = null
    }

    sealed class NavigationEvent {
        data class NavigateToUserEdit(val userId: Int) : NavigationEvent()
    }
}