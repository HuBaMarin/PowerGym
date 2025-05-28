package com.amarina.powergym.ui.viewmodel.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarina.powergym.R
import com.amarina.powergym.database.dao.EjercicioDao
import com.amarina.powergym.database.dao.EstadisticaDao
import com.amarina.powergym.database.entities.Ejercicio
import com.amarina.powergym.database.entities.Estadistica
import com.amarina.powergym.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class EjercicioDetailViewModel(
    private val ejercicioDao: EjercicioDao,
    private val estadisticaDao: EstadisticaDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private var context: android.content.Context? = null

    fun setContext(context: android.content.Context) {
        this.context = context
    }

    // Estado para el ejercicio
    private val _ejercicioState = MutableStateFlow<EjercicioState>(EjercicioState.Loading)
    val ejercicioState: StateFlow<EjercicioState> = _ejercicioState.asStateFlow()

    // Estado para las estadísticas del ejercicio
    private val _estadisticasState = MutableStateFlow<EstadisticasState>(EstadisticasState.Loading)
    val estadisticasState: StateFlow<EstadisticasState> = _estadisticasState.asStateFlow()

    // Estado para acciones como completar ejercicio
    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()

    fun loadEjercicio(ejercicioId: Int) {
        viewModelScope.launch {
            _ejercicioState.value = EjercicioState.Loading

            try {
                val ejercicio = ejercicioDao.obtenerEjercicioPorId(ejercicioId)
                if (ejercicio != null) {
                    _ejercicioState.value = EjercicioState.Success(ejercicio)
                    // Guardar el ID para usar en estadísticas
                    sessionManager.establecerIdEjercicio(ejercicioId)
                } else {
                    _ejercicioState.value = EjercicioState.Error("Ejercicio no encontrado")
                }
            } catch (e: Exception) {
                _ejercicioState.value = EjercicioState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Registra un ejercicio como completado
     */
    fun registrarEjercicioCompletado(
        ejercicioId: Int,
        series: Int,
        repeticiones: Int,
        tiempoMinutos: Int
    ) {
        val userId = sessionManager.obtenerIdUsuario()
        if (userId == -1) {
            _actionState.value = ActionState.Error("Usuario no autenticado")
            return
        }

        viewModelScope.launch {
            _actionState.value = ActionState.Loading

            try {
                // Obtener datos del ejercicio actual
                val ejercicio = ejercicioDao.obtenerEjercicioPorId(ejercicioId)

                if (ejercicio != null) {
                    val caloriasQuemadas = ejercicio.calorias * series
                    val tiempoTotal = tiempoMinutos * 60 * 1000L // Convertir a milisegundos

                    val estadistica = Estadistica(
                        userId = userId,
                        ejercicioId = ejercicioId,
                        fecha = System.currentTimeMillis(),
                        ejerciciosCompletados = 1,
                        caloriasQuemadas = caloriasQuemadas,
                        tiempoTotal = tiempoTotal,
                        repeticiones = repeticiones,
                        series = series,
                        nombreEjercicio = ejercicio.nombre,
                        grupoMuscular = ejercicio.grupoMuscular
                    )

                    try {
                        estadisticaDao.insertarEstadistica(estadistica)
                        val message = context?.getString(R.string.exercise_completed_success)
                            ?: "Exercise completed successfully"
                        _actionState.value = ActionState.Success(message)
                        // Recargar estadísticas
                        loadEstadisticas(ejercicioId)
                    } catch (e: Exception) {
                        _actionState.value = ActionState.Error(
                            e.message ?: "Error al guardar estadísticas"
                        )
                    }
                } else {
                    _actionState.value = ActionState.Error("Ejercicio no encontrado")
                }
            } catch (e: Exception) {
                _actionState.value = ActionState.Error("Error al obtener ejercicio: ${e.message}")
            }
        }
    }

    /**
     * Carga las estadísticas del ejercicio actual
     */
    fun loadEstadisticas(ejercicioId: Int) {
        val userId = sessionManager.obtenerIdUsuario()
        if (userId == -1) {
            _estadisticasState.value = EstadisticasState.Error("Usuario no autenticado")
            return
        }

        viewModelScope.launch {
            _estadisticasState.value = EstadisticasState.Loading

            try {
                // Obtener última semana por defecto
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val startDate = calendar.timeInMillis

                estadisticaDao.obtenerEstadisticasPorEjercicio(userId, ejercicioId)
                    .collect { estadisticas ->
                        val recientes = estadisticas.filter { it.fecha >= startDate }

                        // Calcular resumen
                        val resumen = calcularResumenEstadisticas(recientes)

                        _estadisticasState.value = EstadisticasState.Success(
                            estadisticas = recientes,
                            resumen = resumen
                        )
                    }
            } catch (e: Exception) {
                _estadisticasState.value = EstadisticasState.Error(
                    e.message ?: "Error al cargar estadísticas"
                )
            }
        }
    }

    /**
     * Calcula un resumen de las estadísticas
     */
    private fun calcularResumenEstadisticas(estadisticas: List<Estadistica>): ResumenEstadisticas {
        if (estadisticas.isEmpty()) {
            return ResumenEstadisticas()
        }

        val totalCalorias = estadisticas.sumOf { it.caloriasQuemadas }
        val totalTiempo = estadisticas.sumOf { it.tiempoTotal }
        val totalSeries = estadisticas.sumOf { it.series }
        val totalRepeticiones = estadisticas.sumOf { it.repeticiones }

        return ResumenEstadisticas(
            totalSesiones = estadisticas.size,
            totalCalorias = totalCalorias,
            totalTiempo = totalTiempo,
            totalSeries = totalSeries,
            totalRepeticiones = totalRepeticiones,
            mejorSesionCalorias = estadisticas.maxOf { it.caloriasQuemadas },
            mejorSesionTiempo = estadisticas.maxOf { it.tiempoTotal }
        )
    }

    fun resetActionState() {
        _actionState.value = ActionState.Idle
    }

    // Estados para el ejercicio
    sealed class EjercicioState {
        object Loading : EjercicioState()
        data class Success(val ejercicio: Ejercicio) : EjercicioState()
        data class Error(val message: String) : EjercicioState()
    }

    // Estados para las estadísticas
    sealed class EstadisticasState {
        object Loading : EstadisticasState()
        data class Success(
            val estadisticas: List<Estadistica>,
            val resumen: ResumenEstadisticas
        ) : EstadisticasState()
        data class Error(val message: String) : EstadisticasState()
    }

    // Estados para acciones
    sealed class ActionState {
        object Idle : ActionState()
        object Loading : ActionState()
        data class Success(val message: String) : ActionState()
        data class Error(val message: String) : ActionState()
    }

    // Clase para el resumen de estadísticas
    data class ResumenEstadisticas(
        val totalSesiones: Int = 0,
        val totalCalorias: Int = 0,
        val totalTiempo: Long = 0L,
        val totalSeries: Int = 0,
        val totalRepeticiones: Int = 0,
        val mejorSesionCalorias: Int = 0,
        val mejorSesionTiempo: Long = 0L
    )
}
