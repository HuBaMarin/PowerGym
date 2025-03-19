package com.amarina.powergym.ui.ejercicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarina.powergym.database.entities.Ejercicio
import com.amarina.powergym.repository.EjercicioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EjercicioDetailViewModel(
    private val ejercicioRepository: EjercicioRepository
) : ViewModel() {

    private val _ejercicioState = MutableStateFlow<EjercicioState>(EjercicioState.Loading)
    val ejercicioState: StateFlow<EjercicioState> = _ejercicioState

    fun loadEjercicio(ejercicioId: Int) {
        viewModelScope.launch {
            _ejercicioState.value = EjercicioState.Loading

            ejercicioRepository.getEjercicioById(ejercicioId)
                .onSuccess { ejercicio ->
                    _ejercicioState.value = EjercicioState.Success(ejercicio)
                }
                .onFailure { exception ->
                    _ejercicioState.value = EjercicioState.Error(exception.message ?: "Error desconocido")
                }
        }
    }

    sealed class EjercicioState {
        object Loading : EjercicioState()
        data class Success(val ejercicio: Ejercicio) : EjercicioState()
        data class Error(val message: String) : EjercicioState()
    }
}
