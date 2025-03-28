package com.amarina.powergym.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarina.powergym.database.entities.Ejercicio
import com.amarina.powergym.repository.EjercicioRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val ejercicioRepository: EjercicioRepository
) : ViewModel() {

    private val _filteredCount = MutableStateFlow(0)
    val filteredCount: StateFlow<Int> = _filteredCount

  private val _ejerciciosState = MutableStateFlow<EjerciciosState>(EjerciciosState.Loading)
  val ejercicios: StateFlow<EjerciciosState> = _ejerciciosState
  //private val ejerciciosState: StateFlow<EjerciciosState> = _ejerciciosState

    private val _musculosState = MutableStateFlow<List<String>>(emptyList())
    val musculosState: StateFlow<List<String>> = _musculosState

    private val _seccionesState = MutableStateFlow<List<String>>(emptyList())


    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents

    // Filtros aplicados actualmente
    private val _diasSeleccionados = MutableStateFlow<Set<String>>(emptySet())
    val diasSeleccionados: StateFlow<Set<String>> = _diasSeleccionados

    private val _dificultadesSeleccionadas = MutableStateFlow<Set<String>>(emptySet())
    val dificultadesSeleccionadas: StateFlow<Set<String>> = _dificultadesSeleccionadas

    private val _grupoMuscularSeleccionado = MutableStateFlow<String?>(null)
    val grupoMuscularSeleccionado: StateFlow<String?> = _grupoMuscularSeleccionado

    private val _consulta = MutableStateFlow("")
    val consulta: StateFlow<String> = _consulta

    init {
        loadEjercicios()
        loadMusculos()
        loadSecciones()
    }

    fun loadEjercicios() {
        viewModelScope.launch {
            _ejerciciosState.value = EjerciciosState.Loading

            ejercicioRepository.getAllEjercicios()
                .onSuccess { ejercicios ->
                    val grouped = ejercicios.groupBy { it.seccion }
                    _ejerciciosState.value = EjerciciosState.Success(grouped)
                }
                .onFailure { exception ->
                    _ejerciciosState.value =
                        EjerciciosState.Error(exception.message ?: "Error desconocido")
                }
        }
    }

    private fun loadMusculos() {
        viewModelScope.launch {
            ejercicioRepository.getAllMuscleGroups()
                .onSuccess { musculos ->
                    _musculosState.value = musculos
                }
                .onFailure { /* Ignorar errores aquí */ }
        }
    }

    private fun loadSecciones() {
        viewModelScope.launch {
            ejercicioRepository.getAllSections()
                .onSuccess { secciones ->
                    _seccionesState.value = secciones
                }
                .onFailure { /* Ignorar errores aquí */ }
        }
    }

    fun setDiasSeleccionados(dias: Set<String>) {
        _diasSeleccionados.value = dias
        aplicarFiltros()
    }

    fun setDificultadesSeleccionadas(dificultades: Set<String>) {
        _dificultadesSeleccionadas.value = dificultades
        aplicarFiltros()
    }

    fun setGrupoMuscularSeleccionado(grupo: String?) {
        _grupoMuscularSeleccionado.value = grupo
        aplicarFiltros()
    }

   fun setDiaFilter(dias: String) {
       _diasSeleccionados.value = setOf(dias)
       aplicarFiltros()
   }
    fun setDificultadFilter(dificultad: String?) {
        _dificultadesSeleccionadas.value = if (dificultad != null) setOf(dificultad) else emptySet()
        aplicarFiltros()
    }

   suspend fun getAllMuscleGroups(): Result<List<String>> {
       return ejercicioRepository.getAllMuscleGroups()
   }

    fun setGrupoMuscularFilter(grupo: String?) {
        _grupoMuscularSeleccionado.value = grupo
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        viewModelScope.launch {
            _ejerciciosState.value = EjerciciosState.Loading

            val dias = diasSeleccionados.value.joinToString(",").takeIf { it.isNotEmpty() }
            val dificultad = dificultadesSeleccionadas.value.singleOrNull()
            val grupoMuscular = grupoMuscularSeleccionado.value
            val query = consulta.value

            ejercicioRepository.getFilteredEjercicios(dias, dificultad, grupoMuscular, query)
                .onSuccess { ejercicios ->
                    val grouped = ejercicios.groupBy { it.seccion }
                    _ejerciciosState.value = EjerciciosState.Success(grouped)
                }
                .onFailure { exception ->
                    _ejerciciosState.value =
                        EjerciciosState.Error(exception.message ?: "Error desconocido")
                }
        }
    }

    fun navigateToEjercicioDetail(ejercicioId: Int) {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.ToEjercicioDetail(ejercicioId))
        }
    }


    fun navigateToSearch() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.ToSearch)
        }
    }

    fun navigateToProfile() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.ToProfile)
        }
    }

    fun navigateToStatistics() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.ToStatistics)
        }
    }

    sealed class EjerciciosState {
        abstract val ejercicios: List<Ejercicio>

        object Loading : EjerciciosState() {
            override val ejercicios: List<Ejercicio> = emptyList()
        }

        data class Success(val ejerciciosBySection: Map<String, List<Ejercicio>>) : EjerciciosState() {
            override val ejercicios: List<Ejercicio> = ejerciciosBySection.values.flatten()
        }

        data class Error(val message: String) : EjerciciosState() {
            override val ejercicios: List<Ejercicio> = emptyList()
        }
    }

    sealed class NavigationEvent {
        data class ToEjercicioDetail(val ejercicioId: Int) : NavigationEvent()
        object ToSettings : NavigationEvent()
        object ToProfile : NavigationEvent()
        object ToStatistics : NavigationEvent()
        object ToSearch : NavigationEvent()
        object ToMain : NavigationEvent()
    }
}
