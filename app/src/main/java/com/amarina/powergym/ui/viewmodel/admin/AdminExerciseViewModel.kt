package com.amarina.powergym.ui.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarina.powergym.database.dao.EjercicioDao
import com.amarina.powergym.database.entities.Ejercicio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminExerciseViewModel(
    val ejercicioDao: EjercicioDao
) : ViewModel() {

    private val _exercises = MutableStateFlow<List<Ejercicio>>(emptyList())
    val exercises: StateFlow<List<Ejercicio>> = _exercises

    private val _sections = MutableStateFlow<List<String>>(emptyList())
    val sections: StateFlow<List<String>> = _sections

    private val _muscleGroups = MutableStateFlow<List<String>>(emptyList())
    val muscleGroups: StateFlow<List<String>> = _muscleGroups

    private val _difficultyValues = MutableStateFlow<List<String>>(emptyList())
    val difficultyValues: StateFlow<List<String>> = _difficultyValues

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedExercise = MutableStateFlow<Ejercicio?>(null)
    val selectedExercise: StateFlow<Ejercicio?> = _selectedExercise

    private var currentSearchQuery = ""
    private var currentSectionFilter: String? = null

    init {
        loadExercises()
        loadSections()
        loadMuscleGroups()
        loadDifficultyValues()
    }

    fun loadExercises() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _exercises.value = ejercicioDao.obtenerTodosEjercicios()
            } catch (e: Exception) {
                _error.value = e.message ?: "Unexpected error"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadSections() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _sections.value = ejercicioDao.obtenerTodasSecciones()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading sections"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadMuscleGroups() {
        viewModelScope.launch {
            try {
                _muscleGroups.value = ejercicioDao.obtenerTodosGruposMusculares()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading muscle groups"
            }
        }
    }

    fun loadDifficultyValues() {
        viewModelScope.launch {
            try {
                _difficultyValues.value = ejercicioDao.obtenerValoresDificultad()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading difficulty values"
            }
        }
    }

    fun searchExercises(query: String) {
        currentSearchQuery = query
        applyFilters()
    }

    fun filterBySection(section: String?) {
        currentSectionFilter = section
        applyFilters()
    }

    private fun applyFilters() {
        viewModelScope.launch {
            _loading.value = true
            try {
                if (currentSearchQuery.isEmpty() && currentSectionFilter == null) {
                    loadExercises()
                    return@launch
                }

                val exerciseList = ejercicioDao.obtenerEjerciciosFiltrados(
                    dias = null,
                    dificultad = null,
                    grupoMuscular = null,
                    query = currentSearchQuery
                )

                val filteredList = if (currentSectionFilter != null) {
                    exerciseList.filter { it.seccion == currentSectionFilter }
                } else {
                    exerciseList
                }
                _exercises.value = filteredList
            } catch (e: Exception) {
                _error.value = e.message ?: "Error filtering exercises"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteExercise(exerciseId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val ejercicio = ejercicioDao.obtenerEjercicioPorId(exerciseId)
                if (ejercicio != null) {
                    ejercicioDao.eliminar(ejercicio)
                    loadExercises() // Refresh the list
                } else {
                    _error.value = "Exercise not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error deleting exercise"
            } finally {
                _loading.value = false
            }
        }
    }

    fun addExercise(
        name: String,
        description: String,
        muscleGroup: String,
        difficulty: String,
        trainingDays: String,
        imageUrl: String,
        videoUrl: String,
        section: String,
        calories: Int,
        frequency: Int,
        percentage: Float
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val newExercise = Ejercicio(
                    nombre = name,
                    descripcion = description,
                    grupoMuscular = muscleGroup,
                    dificultad = difficulty,
                    dias = trainingDays,
                    imagenEjercicio = imageUrl,
                    videoUrl = videoUrl,
                    seccion = section,
                    calorias = calories,
                    frecuencia = frequency,
                    porcentaje = percentage
                )

                ejercicioDao.insertar(newExercise)
                loadExercises() // Refrescar la lista
            } catch (e: Exception) {
                _error.value = e.message ?: "Error adding exercise"
            } finally {
                _loading.value = false
            }
        }
    }

    fun getExerciseById(exerciseId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val exercise = ejercicioDao.obtenerEjercicioPorId(exerciseId)
                _selectedExercise.value = exercise
                if (exercise == null) {
                    _error.value = "Exercise not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading exercise"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateExercise(
        exerciseId: Int,
        name: String,
        description: String,
        muscleGroup: String,
        difficulty: String,
        trainingDays: String,
        imageUrl: String,
        videoUrl: String,
        section: String,
        calories: Int,
        frequency: Int,
        percentage: Float
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                // Get the current exercise first to ensure it exists
                val currentExercise = ejercicioDao.obtenerEjercicioPorId(exerciseId)

                if (currentExercise != null) {
                    // Create the updated exercise with the same ID
                    val updatedExercise = Ejercicio(
                        id = exerciseId,
                        nombre = name,
                        descripcion = description,
                        grupoMuscular = muscleGroup,
                        dificultad = difficulty,
                        dias = trainingDays,
                        imagenEjercicio = imageUrl,
                        videoUrl = videoUrl,
                        seccion = section,
                        calorias = calories,
                        frecuencia = frequency,
                        porcentaje = percentage
                    )

                    // Update in the database
                    ejercicioDao.actualizar(updatedExercise)

                    // Clear selected exercise and refresh the list
                    _selectedExercise.value = null
                    loadExercises()
                } else {
                    _error.value = "Exercise not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error updating exercise"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSelectedExercise() {
        _selectedExercise.value = null
    }
}