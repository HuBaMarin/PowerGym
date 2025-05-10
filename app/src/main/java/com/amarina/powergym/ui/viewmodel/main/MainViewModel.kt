package com.amarina.powergym.ui.viewmodel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarina.powergym.database.dao.EjercicioDao
import com.amarina.powergym.database.entities.Ejercicio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val ejercicioDao: EjercicioDao
) : ViewModel() {

    private val _ejerciciosState = MutableStateFlow<EjerciciosState>(EjerciciosState.Loading)
    val ejercicios: StateFlow<EjerciciosState> = _ejerciciosState

    private val _musculosState = MutableStateFlow<List<String>>(emptyList())


    private val _seccionesState = MutableStateFlow<List<String>>(emptyList())

    private val _filterDifficulty = MutableStateFlow<String?>(null)


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

            try {
                val ejercicios = ejercicioDao.obtenerTodosEjercicios()
                val grouped = ejercicios.groupBy { it.seccion }
                _ejerciciosState.value = EjerciciosState.Success(grouped)
            } catch (exception: Exception) {
                _ejerciciosState.value =
                    EjerciciosState.Error(exception.message ?: "Error desconocido")
            }
        }
    }

    private fun loadMusculos() {
        viewModelScope.launch {
            try {
                val musculos = ejercicioDao.obtenerTodosGruposMusculares()
                _musculosState.value = musculos
            } catch (exception: Exception) {
                // Silenciar errores, solo log
            }
        }
    }

    private fun loadSecciones() {
        viewModelScope.launch {
            try {
                val secciones = ejercicioDao.obtenerTodasSecciones()
                _seccionesState.value = secciones
            } catch (exception: Exception) {
                // Silenciar errores, solo log
            }
        }
    }

    fun setDificultadFilter(dificultad: String?) {
        _filterDifficulty.value = dificultad
        refreshEjercicios()
    }


    fun clearDificultadFilter() {
        _filterDifficulty.value = null
        refreshEjercicios()
    }

    private fun refreshEjercicios() {
        viewModelScope.launch {
            _ejerciciosState.value = EjerciciosState.Loading
            try {
                val allEjercicios = ejercicioDao.obtenerTodosEjercicios()

                // Aplicar filtros usando los valores correctos de la BD
                val filteredEjercicios = if (_filterDifficulty.value != null) {
                    allEjercicios.filter { ejercicio ->
                        ejercicio.dificultad == _filterDifficulty.value
                    }
                } else {
                    allEjercicios
                }

                // Agrupar por sección después de filtrar
                val groupedEjercicios = filteredEjercicios.groupBy { it.seccion }
                _ejerciciosState.value = EjerciciosState.Success(groupedEjercicios)

            } catch (e: Exception) {
                _ejerciciosState.value = EjerciciosState.Error(e.message ?: "Error desconocido")
            }
        }
    }


    sealed class EjerciciosState {
        abstract val ejercicios: List<Ejercicio>

        object Loading : EjerciciosState() {
            override val ejercicios: List<Ejercicio> = emptyList()
        }

        data class Success(val ejerciciosBySection: Map<String, List<Ejercicio>>) :
            EjerciciosState() {
            override val ejercicios: List<Ejercicio> = ejerciciosBySection.values.flatten()
        }

        data class Error(val message: String) : EjerciciosState() {
            override val ejercicios: List<Ejercicio> = emptyList()
        }
    }

}
