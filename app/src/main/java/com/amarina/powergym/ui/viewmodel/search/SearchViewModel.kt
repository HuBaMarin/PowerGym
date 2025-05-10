package com.amarina.powergym.ui.viewmodel.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarina.powergym.database.dao.EjercicioDao
import com.amarina.powergym.database.entities.Ejercicio
import com.amarina.powergym.ui.search.SearchHistoryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SearchViewModel(
    private val ejercicioDao: EjercicioDao,
    private val historyManager: SearchHistoryManager
) : ViewModel() {

    // Flow para la consulta de búsqueda
    private val _queryFlow = MutableStateFlow("")
    val queryFlow: StateFlow<String> = _queryFlow.asStateFlow()

    // Flow para el filtro de dificultad
    private val _difficultyFilter = MutableStateFlow<String?>(null)
    val difficultyFilter: StateFlow<String?> = _difficultyFilter.asStateFlow()

    // Flow para el estado de búsqueda
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    // Flow para el historial de búsqueda
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    // Lista de valores de dificultad disponibles
    private val _difficultyValues = MutableStateFlow<List<String>>(emptyList())
    val difficultyValues: StateFlow<List<String>> = _difficultyValues.asStateFlow()

    init {
        loadSearchHistory()
        loadDifficultyValues()
        loadInitialResults()

        // Combinar consulta y filtro para realizar la búsqueda
        viewModelScope.launch {
            combine(_queryFlow, _difficultyFilter) { query, difficulty ->
                Pair(query, difficulty)
            }.collect { (query, difficulty) ->
                // Always perform search, even for empty query and null filter
                performSearch(query, difficulty)
            }
        }
    }

    // Cargar valores de dificultad desde la base de datos
    private fun loadDifficultyValues() {
        viewModelScope.launch {
            try {
                val values = ejercicioDao.obtenerValoresDificultad()
                Log.d("SearchViewModel", "Valores de dificultad cargados: $values")
                _difficultyValues.value = values
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error al cargar valores de dificultad", e)
            }
        }
    }

    // Actualizar el filtro de dificultad
    fun setDifficultyFilter(difficulty: String?) {
        Log.d("SearchViewModel", "Filtro de dificultad cambiado a: $difficulty")

        // Special handling for null filter (All filter)
        // This forces a reload of all exercises when All filter is selected
        if (difficulty == null && _difficultyFilter.value != null) {
            Log.d("SearchViewModel", "Cambiando a filtro ALL, cargando todos los ejercicios")
            _difficultyFilter.value = difficulty
            viewModelScope.launch {
                performSearch("", null)
            }
        } else {
            _difficultyFilter.value = difficulty
        }
    }

    // Establecer la consulta de búsqueda
    fun setSearchQuery(query: String, saveToHistory: Boolean = false) {
        val trimmedQuery = query.trim()
        _queryFlow.value = trimmedQuery

        if (saveToHistory && trimmedQuery.isNotBlank()) {
            historyManager.addSearchQuery(trimmedQuery)
            loadSearchHistory()
        }
    }

    // Realizar la búsqueda
    private suspend fun performSearch(query: String, difficulty: String?) {
        Log.d("SearchViewModel", "Realizando búsqueda - Query: '$query', Dificultad: '$difficulty'")

        _searchState.value = SearchState.Loading

        try {
            // Utilizamos DAO con los parámetros adecuados
            val ejercicios = if (query.isBlank() && difficulty == null) {
                // If both query and filter are empty, get all exercises
                Log.d("SearchViewModel", "Cargando todos los ejercicios")
                ejercicioDao.obtenerTodosEjercicios()
            } else {
                ejercicioDao.obtenerEjerciciosFiltrados(
                    dias = null,
                    dificultad = difficulty,
                    grupoMuscular = null,
                    query = query
                )
            }

            Log.d("SearchViewModel", "Búsqueda completada - ${ejercicios.size} resultados")

            // Always return Success state, even with empty list
            // Empty state handling will be done in the UI
            _searchState.value = SearchState.Success(ejercicios)

            // Save successful non-empty queries to history
            if (ejercicios.isNotEmpty() && query.isNotBlank()) {
                historyManager.addSearchQuery(query)
                loadSearchHistory()
            }
        } catch (error: Exception) {
            Log.e("SearchViewModel", "Error en búsqueda", error)
            _searchState.value = SearchState.Error(error.message ?: "Error desconocido")
        }
    }

    // Cargar el historial de búsqueda
    private fun loadSearchHistory() {
        _searchHistory.value = historyManager.getSearchHistory()
    }

    // Limpiar el historial de búsqueda
    fun clearSearchHistory() {
        historyManager.clearSearchHistory()
        loadSearchHistory()
    }

    // Load all exercises for initial display
    fun loadInitialResults() {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            try {
                val ejercicios = ejercicioDao.obtenerTodosEjercicios()
                _searchState.value = SearchState.Success(ejercicios)
            } catch (error: Exception) {
                Log.e("SearchViewModel", "Error cargando resultados iniciales", error)
                _searchState.value = SearchState.Error(error.message ?: "Error desconocido")
            }
        }
    }

    // Estados posibles de la búsqueda
    sealed class SearchState {
        object Idle : SearchState()
        object Loading : SearchState()
        data class Success(val ejercicios: List<Ejercicio>) : SearchState()
        data class Error(val message: String) : SearchState()
    }
}
