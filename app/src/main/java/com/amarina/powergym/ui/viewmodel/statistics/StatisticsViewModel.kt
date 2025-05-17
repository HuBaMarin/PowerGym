package com.amarina.powergym.ui.viewmodel.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarina.powergym.database.entities.Estadistica
import com.amarina.powergym.database.dao.EstadisticaDao
import com.amarina.powergym.database.dao.EjercicioDao
import com.amarina.powergym.ui.adapter.statistics.ExerciseWithStats
import com.amarina.powergym.utils.LocaleProvider
import com.amarina.powergym.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StatisticsViewModel(
    private val estadisticaDao: EstadisticaDao,
    private val ejercicioDao: EjercicioDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    enum class TimeRange { WEEK, MONTH, YEAR, ALL }

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Current time range for chart
    private val _currentTimeRange = MutableStateFlow(TimeRange.WEEK)
    val currentTimeRange: StateFlow<TimeRange> = _currentTimeRange

    // Comprehensive statistics list
    private val _comprehensiveStats = MutableStateFlow<List<ExerciseWithStats>>(emptyList())
    val comprehensiveStats: StateFlow<List<ExerciseWithStats>> = _comprehensiveStats

    // Training frequency data for current time range - Map of day to count
    private val _trainingFrequencyData = MutableStateFlow<Map<String, Int>>(emptyMap())
    val trainingFrequencyData: StateFlow<Map<String, Int>> = _trainingFrequencyData

    // Most frequent exercises
    private val _ejerciciosMasTrabajados = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val ejerciciosMasTrabajados: StateFlow<List<Pair<String, Int>>> = _ejerciciosMasTrabajados

    // Application context for locale access
    private lateinit var appContext: Context

    init {
        loadStatistics()
        loadFrequentExercises()
        loadTrainingFrequencyData()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val userId = sessionManager.obtenerIdUsuario()
                if (userId != -1) {
                    estadisticaDao.obtenerTodasEstadisticasDeUsuario(userId)
                        .collect { estadisticas ->
                            val exerciseMap = mutableMapOf<Int, Estadistica>()

                            // First pass: find the most recent stats for each exercise
                            for (stat in estadisticas) {
                                val existingStat = exerciseMap[stat.ejercicioId]
                                if (existingStat == null || stat.fecha > existingStat.fecha) {
                                    exerciseMap[stat.ejercicioId] = stat
                                }
                            }

                            val exerciseStats = mutableListOf<ExerciseWithStats>()

                            // Second pass: get exercise info for each stat
                            withContext(Dispatchers.IO) {
                                for (stat in exerciseMap.values) {
                                    val ejercicio =
                                        ejercicioDao.obtenerEjercicioPorId(stat.ejercicioId)
                                    ejercicio?.let {
                                        val exerciseWithStats = ExerciseWithStats(
                                            ejercicio = it,
                                            latestStats = stat
                                        )
                                        exerciseStats.add(exerciseWithStats)
                                    }
                                }
                            }

                            _comprehensiveStats.value =
                                exerciseStats.sortedByDescending { it.latestStats.fecha }
                        }
                }
            } catch (e: Exception) {
                _error.value = "Error loading statistics: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun loadFrequentExercises() {
        viewModelScope.launch {
            try {
                val userId = sessionManager.obtenerIdUsuario()
                if (userId != -1) {
                    estadisticaDao.obtenerEjerciciosMasFrecuentes(userId)
                        .collect { frecuentes ->
                            // The exercise names are already localized because they come from 
                            // strings.xml resources when the database is populated
                            _ejerciciosMasTrabajados.value = frecuentes.map {
                                Pair(it.nombre, it.frecuencia)
                            }
                        }
                }
            } catch (e: Exception) {
                _error.value = "Error loading frequent exercises: ${e.message}"
            }
        }
    }

    /**
     * Sets the time range for the training frequency chart and loads the data
     */
    fun setTimeRange(range: TimeRange) {
        if (_currentTimeRange.value != range) {
            _currentTimeRange.value = range
            loadTrainingFrequencyData()
        }
    }

    /**
     * Loads training frequency data for the current time range
     */
    private fun loadTrainingFrequencyData() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val userId = sessionManager.obtenerIdUsuario()
                android.util.Log.d(
                    "StatisticsViewModel",
                    "Loading training frequency data for user $userId, time range: ${_currentTimeRange.value}"
                )
                if (userId != -1) {
                    estadisticaDao.obtenerTodasEstadisticasDeUsuario(userId)
                        .collect { allStats ->
                            android.util.Log.d(
                                "StatisticsViewModel",
                                "Received ${allStats.size} statistics entries"
                            )
                            // Calculate start date based on current time range
                            val calendar = Calendar.getInstance()

                            // Filter statistics by time range
                            val filteredStats = when (_currentTimeRange.value) {
                                TimeRange.WEEK -> {
                                    // Set to 7 days ago
                                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                                    allStats.filter { it.fecha >= calendar.timeInMillis }
                                }

                                TimeRange.MONTH -> {
                                    // Set to 30 days ago
                                    calendar.add(Calendar.DAY_OF_YEAR, -30)
                                    allStats.filter { it.fecha >= calendar.timeInMillis }
                                }

                                TimeRange.YEAR -> {
                                    // Set to 365 days ago
                                    calendar.add(Calendar.DAY_OF_YEAR, -365)
                                    allStats.filter { it.fecha >= calendar.timeInMillis }
                                }

                                TimeRange.ALL -> allStats
                            }

                            // Process data based on time range
                            val frequencyMap = when (_currentTimeRange.value) {
                                TimeRange.WEEK -> {
                                    // Group by day of week 
                                    val dayFormat = getDateFormat("EEEE")
                                    processByDayOfWeek(filteredStats, dayFormat)
                                }

                                TimeRange.MONTH -> {
                                    // Group by day of month with translated date format
                                    processByMonth(filteredStats)
                                }

                                TimeRange.YEAR -> {
                                    // Group by month with translated month names
                                    val monthFormat = getDateFormat("MMM")
                                    processYearByMonths(filteredStats, monthFormat)
                                }

                                TimeRange.ALL -> {
                                    // Group by year with localized format
                                    val yearFormat = getDateFormat("yyyy")
                                    processByYear(filteredStats, yearFormat)
                                }
                            }
                            android.util.Log.d(
                                "StatisticsViewModel",
                                "Generated frequency map: $frequencyMap"
                            )
                            _trainingFrequencyData.value = frequencyMap
                        }
                }
            } catch (e: Exception) {
                _error.value = "Error loading training frequency data: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Get a date formatter with the correct app locale
     */
    private fun getDateFormat(pattern: String): SimpleDateFormat {
        return if (::appContext.isInitialized) {
            LocaleProvider.getLocalizedDateFormat(pattern, appContext)
        } else {
            // Fallback to system locale if context not available
            SimpleDateFormat(pattern, Locale.getDefault())
        }
    }

    /**
     * Process statistics by day of week
     */
    private fun processByDayOfWeek(
        stats: List<Estadistica>,
        dateFormat: SimpleDateFormat
    ): Map<String, Int> {
        // Get localized day names based on the current locale
        val calendar = Calendar.getInstance()
        // Use the provided dateFormat that already has the correct locale
        val dayFormat = dateFormat

        // Create a list to store all seven days of the week in the correct order
        val localizedDays = ArrayList<String>(7)

        // First, determine what day of the week is the first day (culturally appropriate)
        // In some countries it's Sunday, in others it's Monday
        val firstDay = calendar.firstDayOfWeek
        calendar.set(Calendar.DAY_OF_WEEK, firstDay)

        // Generate all seven days in order
        for (i in 0..6) {
            localizedDays.add(dayFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_WEEK, 1)
        }

        android.util.Log.d("StatisticsViewModel", "Localized days in order: $localizedDays")

        // Create an ordered map to ensure days appear in correct order
        val orderedMap = linkedMapOf<String, Int>()

        // Initialize all days with zero
        localizedDays.forEach { day ->
            orderedMap[day] = 0
        }

        // Count statistics for each day
        stats.forEach { stat ->
            val date = Date(stat.fecha)
            val dayName = dayFormat.format(date)

            android.util.Log.d(
                "StatisticsViewModel",
                "Stat date: $date, day of week: $dayName"
            )

            orderedMap[dayName] = (orderedMap[dayName] ?: 0) + 1
        }

        android.util.Log.d("StatisticsViewModel", "Day of week frequency map: $orderedMap")
        return orderedMap
    }

    private fun processByMonth(
        stats: List<Estadistica>
    ): Map<String, Int> {
        val lastExerciseDate = stats.maxByOrNull { it.fecha }?.fecha
            ?: return emptyMap()

        val endCalendar = Calendar.getInstance()
        if (lastExerciseDate > 0) {
            endCalendar.timeInMillis = lastExerciseDate
        }

        val startCalendar = Calendar.getInstance()
        startCalendar.timeInMillis = endCalendar.timeInMillis
        startCalendar.add(Calendar.DAY_OF_YEAR, -30)

        val dateFormat = getDateFormat("d MMM")
        val orderedMap = linkedMapOf<String, Int>()
        val currentCal = Calendar.getInstance()
        currentCal.timeInMillis = startCalendar.timeInMillis

        while (currentCal.timeInMillis <= endCalendar.timeInMillis) {
            val formattedDate = dateFormat.format(currentCal.time)
            orderedMap[formattedDate] = 0
            currentCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Group statistics by day and count
        stats.forEach { stat ->
            val date = Date(stat.fecha)
            val calendar = Calendar.getInstance()
            calendar.time = date

            // Only process if it's within the range
            if (calendar.timeInMillis >= startCalendar.timeInMillis &&
                calendar.timeInMillis <= endCalendar.timeInMillis
            ) {
                val formattedDate = dateFormat.format(calendar.time)

                android.util.Log.d(
                    "StatisticsViewModel",
                    "Stat date: $date, formatted date: $formattedDate"
                )

                orderedMap[formattedDate] = (orderedMap[formattedDate] ?: 0) + 1
            }
        }

        android.util.Log.d("StatisticsViewModel", "Month data map: $orderedMap")
        return orderedMap
    }

    /**
     * Process statistics for a year by grouping them into months
     */
    private fun processYearByMonths(
        stats: List<Estadistica>,
        monthFormat: SimpleDateFormat
    ): Map<String, Int> {
        // Get localized month names
        val calendar = Calendar.getInstance()
        val localizedMonths = mutableListOf<String>()

        // Get current year
        val currentYear = calendar.get(Calendar.YEAR)

        // Get all localized month names in order (January to December)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.YEAR, currentYear)

        for (month in 0..11) {
            calendar.set(Calendar.MONTH, month)
            localizedMonths.add(monthFormat.format(calendar.time))
        }

        android.util.Log.d("StatisticsViewModel", "Localized months: $localizedMonths")

        // Initialize all months with zero in proper order
        val orderedMap = linkedMapOf<String, Int>()
        localizedMonths.forEach { month ->
            orderedMap[month] = 0
        }

        // Only process stats from current year
        val filteredStats = stats.filter {
            val date = Date(it.fecha)
            val cal = Calendar.getInstance()
            cal.time = date
            cal.get(Calendar.YEAR) == currentYear
        }

        // Group statistics by month and count
        filteredStats.forEach { stat ->
            val date = Date(stat.fecha)
            val monthName = monthFormat.format(date)

            android.util.Log.d(
                "StatisticsViewModel",
                "Stat date: $date, month: $monthName"
            )

            orderedMap[monthName] = (orderedMap[monthName] ?: 0) + 1
        }

        android.util.Log.d("StatisticsViewModel", "Month frequency map: $orderedMap")
        return orderedMap
    }

    /**
     * Process statistics by year
     */
    private fun processByYear(
        stats: List<Estadistica>,
        dateFormat: SimpleDateFormat
    ): Map<String, Int> {
        // Find the min and max years in the data
        var minYear = Int.MAX_VALUE
        var maxYear = Int.MIN_VALUE

        stats.forEach { stat ->
            val date = Date(stat.fecha)
            val cal = Calendar.getInstance()
            cal.time = date
            val year = cal.get(Calendar.YEAR)

            if (year < minYear) minYear = year
            if (year > maxYear) maxYear = year
        }

        // If no data, use current year
        if (minYear == Int.MAX_VALUE) {
            minYear = Calendar.getInstance().get(Calendar.YEAR)
            maxYear = minYear
        }

        // Use the provided date format with correct locale
        val localizedFormat = dateFormat

        // Create ordered map with all years in range
        val orderedMap = linkedMapOf<String, Int>()
        for (year in minYear..maxYear) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            val yearStr = localizedFormat.format(cal.time)
            orderedMap[yearStr] = 0
        }

        // Populate with actual data
        stats.forEach { stat ->
            val date = Date(stat.fecha)
            val year = localizedFormat.format(date)
            orderedMap[year] = (orderedMap[year] ?: 0) + 1
        }

        return orderedMap
    }

    /**
     * Set the application context for localization
     */
    fun setContext(context: Context) {
        appContext = context.applicationContext
        refreshData() // Refresh data with new locale
    }

    // Call this to refresh all data
    fun refreshData() {
        loadStatistics()
        loadFrequentExercises()
        loadTrainingFrequencyData()
    }

    fun clearError() {
        _error.value = null
    }
}