package com.amarina.powergym.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarina.powergym.database.entities.Estadistica
import com.amarina.powergym.repository.EstadisticaRepository
import com.amarina.powergym.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class StatisticsViewModel(
    private val estadisticaRepository: EstadisticaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _statisticsState = MutableStateFlow<StatisticsState>(StatisticsState.Loading)
    val statisticsState: StateFlow<StatisticsState> = _statisticsState

    private val _totalState = MutableStateFlow<TotalState>(TotalState.Loading)
    val totalState: StateFlow<TotalState> = _totalState

    private val userId = sessionManager.getUserId()

    init {
        if (userId != -1) {
            loadStatistics()
            loadTotals()
        } else {
            _statisticsState.value = StatisticsState.Error("No hay usuario en sesión")
            _totalState.value = TotalState.Error("No hay usuario en sesión")
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            estadisticaRepository.getEstadisticasByUsuario(userId)
                .collectLatest { estadisticas ->
                    if (estadisticas.isEmpty()) {
                        _statisticsState.value = StatisticsState.Empty
                    } else {
                        _statisticsState.value = StatisticsState.Success(estadisticas)
                    }
                }
        }
    }

    private fun loadTotals() {
        viewModelScope.launch {
            _totalState.value = TotalState.Loading

            val ejerciciosResult = estadisticaRepository.getTotalEjerciciosCompletados(userId)
            val caloriasResult = estadisticaRepository.getTotalCaloriasQuemadas(userId)
            val tiempoResult = estadisticaRepository.getTotalTiempoEntrenamiento(userId)

            if (ejerciciosResult.isSuccess && caloriasResult.isSuccess && tiempoResult.isSuccess) {
                val totals = TotalStats(
                    ejercicios = ejerciciosResult.getOrDefault(0),
                    calorias = caloriasResult.getOrDefault(0),
                    tiempo = tiempoResult.getOrDefault(0L)
                )
                _totalState.value = TotalState.Success(totals)
            } else {
                _totalState.value = TotalState.Error("Error al cargar los totales")
            }
        }
    }

    fun loadStatisticsByDateRange(range: DateRange) {
        viewModelScope.launch {
            _statisticsState.value = StatisticsState.Loading

            val calendar = Calendar.getInstance()
            val endDate = calendar.timeInMillis

            calendar.apply {
                when (range) {
                    DateRange.WEEK -> add(Calendar.DAY_OF_YEAR, -7)
                    DateRange.MONTH -> add(Calendar.MONTH, -1)
                    DateRange.YEAR -> add(Calendar.YEAR, -1)
                    DateRange.ALL -> set(1970, 0, 1)
                }
            }
            val startDate = calendar.timeInMillis

            estadisticaRepository.getEstadisticasByDateRange(userId, startDate, endDate)
                .onSuccess { estadisticas ->
                    if (estadisticas.isEmpty()) {
                        _statisticsState.value = StatisticsState.Empty
                    } else {
                        _statisticsState.value = StatisticsState.Success(estadisticas)
                    }
                }
                .onFailure { exception ->
                    _statisticsState.value = StatisticsState.Error(exception.message ?: "Error desconocido")
                }
        }
    }

    data class TotalStats(
        val ejercicios: Int,
        val calorias: Int,
        val tiempo: Long
    )

    enum class DateRange {
        WEEK, MONTH, YEAR, ALL
    }

    sealed class StatisticsState {
        object Loading : StatisticsState()
        object Empty : StatisticsState()
        data class Success(val estadisticas: List<Estadistica>) : StatisticsState()
        data class Error(val message: String) : StatisticsState()
    }

    sealed class TotalState {
        object Loading : TotalState()
        data class Success(val totals: TotalStats) : TotalState()
        data class Error(val message: String) : TotalState()
    }
}
