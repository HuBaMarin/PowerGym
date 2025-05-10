package com.amarina.powergym.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarina.powergym.PowerGymApplication
import com.amarina.powergym.R
import com.amarina.powergym.databinding.ActivityStatisticsBinding
import com.amarina.powergym.ui.adapter.exercise.FrequencyItemAdapter
import com.amarina.powergym.ui.adapter.statistics.ComprehensiveStatsAdapter
import com.amarina.powergym.ui.viewmodel.statistics.StatisticsViewModel
import com.amarina.powergym.utils.LanguageHelper
import com.amarina.powergym.utils.SessionManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var viewModel: StatisticsViewModel
    private lateinit var sessionManager: SessionManager

    private lateinit var ejerciciosMasTrabajadosAdapter: FrequencyItemAdapter
    private lateinit var comprehensiveStatsAdapter: ComprehensiveStatsAdapter
    private lateinit var trainingChart: BarChart

    companion object {
        const val EXTRA_EJERCICIO_ID = "extra_ejercicio_id"
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.establecerIdioma(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Initialize the ViewModel
        viewModel = ViewModelProvider(
            this,
            (application as PowerGymApplication).statisticsViewModelFactory
        )[StatisticsViewModel::class.java]

        (application as PowerGymApplication).statisticsViewModel = viewModel

        // Set the context in ViewModel for proper localization
        viewModel.setContext(this)

        configureSelectedExercise()


        setupBottomNavigation()
        setupAdapters()
        setupChart()
        observeViewModel()
    }

    private fun configureSelectedExercise() {
        val ejercicioId = intent.getIntExtra(EXTRA_EJERCICIO_ID, -1)
        if (ejercicioId != -1) {
            sessionManager.establecerIdEjercicio(ejercicioId)
            sessionManager.establecerEjerciciosSeleccionados(listOf(ejercicioId))
        } else {
            val storedEjercicioId = sessionManager.obtenerIdEjercicio()
            if (storedEjercicioId != -1) {
                sessionManager.establecerEjerciciosSeleccionados(listOf(storedEjercicioId))
            } else {
                binding.tvEmpty.text = getString(R.string.no_exercise_selected)
                binding.tvEmpty.visibility = View.VISIBLE
            }
        }
    }

    private fun setupAdapters() {
        ejerciciosMasTrabajadosAdapter = FrequencyItemAdapter()
        binding.rvEjerciciosMasTrabajados.adapter = ejerciciosMasTrabajadosAdapter
        binding.rvEjerciciosMasTrabajados.layoutManager = LinearLayoutManager(this)

        comprehensiveStatsAdapter = ComprehensiveStatsAdapter()
        binding.rvComprehensiveStats.adapter = comprehensiveStatsAdapter
        binding.rvComprehensiveStats.layoutManager = LinearLayoutManager(this)
    }

    private fun setupChart() {
        trainingChart = binding.trainingChart.trainingFrequencyChart

        android.util.Log.d("StatisticsActivity", "Chart initialized")

        // Configure chart appearance
        trainingChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            legend.textColor = resources.getColor(R.color.primary, theme)
            setDrawBorders(false)
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setScaleEnabled(false)
            setTouchEnabled(true)
            setPinchZoom(false)

            // Set theme colors
            axisLeft.apply {
                setDrawGridLines(false)
                textColor = resources.getColor(R.color.primary, theme)
                axisMinimum = 0f
                granularity = 1f
            }

            axisRight.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = resources.getColor(R.color.primary, theme)
            }
        }

        // Set time range toggle listeners
        binding.trainingChart.timeRangeToggle.apply {
            // Default to week view
            check(R.id.btnWeek)

            // Add listeners
            addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    when (checkedId) {
                        R.id.btnWeek -> viewModel.setTimeRange(StatisticsViewModel.TimeRange.WEEK)
                        R.id.btnMonth -> viewModel.setTimeRange(StatisticsViewModel.TimeRange.MONTH)
                        R.id.btnYear -> viewModel.setTimeRange(StatisticsViewModel.TimeRange.YEAR)
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        // Observe most frequent exercises
        lifecycleScope.launch {
            viewModel.ejerciciosMasTrabajados.collectLatest { exercises ->
                ejerciciosMasTrabajadosAdapter.submitList(exercises)
                binding.rvEjerciciosMasTrabajados.visibility =
                    if (exercises.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }

        // Observe comprehensive stats
        lifecycleScope.launch {
            viewModel.comprehensiveStats.collectLatest { stats ->
                comprehensiveStatsAdapter.submitList(stats)

                if (stats.isEmpty()) {
                    binding.tvNoStats.visibility = View.VISIBLE
                    binding.rvComprehensiveStats.visibility = View.GONE
                } else {
                    binding.tvNoStats.visibility = View.GONE
                    binding.rvComprehensiveStats.visibility = View.VISIBLE
                }
            }
        }

        // Observe training frequency data for chart
        lifecycleScope.launch {
            viewModel.trainingFrequencyData.collectLatest { frequencyMap ->
                android.util.Log.d(
                    "StatisticsActivity",
                    "Observer received frequency map, size: ${frequencyMap.size}"
                )
                updateTrainingChart(frequencyMap)
            }
        }

        // Observe loading state for chart
        lifecycleScope.launch {
            viewModel.loading.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.trainingChart.chartProgressBar.visibility =
                    if (isLoading) View.VISIBLE else View.GONE

                // Show empty state if no data
                val noData = !isLoading &&
                        viewModel.comprehensiveStats.value.isEmpty() &&
                        viewModel.ejerciciosMasTrabajados.value.isEmpty()

                if (noData) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.tvEmpty.text = getString(R.string.no_statistics)
                } else {
                    binding.tvEmpty.visibility = View.GONE
                }
            }
        }

        // Observe errors
        lifecycleScope.launch {
            viewModel.error.collectLatest { errorMsg ->
                errorMsg?.let {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.tvEmpty.text = it
                    viewModel.clearError()
                }
            }
        }
    }

    private fun updateTrainingChart(frequencyMap: Map<String, Int>) {
        // Debug: Log the frequency map
        android.util.Log.d("StatisticsActivity", "Training frequency data: $frequencyMap")

        if (frequencyMap.isEmpty()) {
            binding.trainingChart.tvNoChartData.visibility = View.VISIBLE
            trainingChart.visibility = View.GONE
            return
        }

        binding.trainingChart.tvNoChartData.visibility = View.GONE
        trainingChart.visibility = View.VISIBLE

        // Get the time range to know how to format the chart
        val currentTimeRange = viewModel.currentTimeRange.value

        // Prepare data for chart
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        // Use different logic based on time range
        when (currentTimeRange) {
            StatisticsViewModel.TimeRange.WEEK -> {
                // Use the keys from frequency map, which are already localized days
                val daysOfWeek = frequencyMap.keys.toList()

                daysOfWeek.forEachIndexed { index, day ->
                    entries.add(BarEntry(index.toFloat(), frequencyMap[day]?.toFloat() ?: 0f))
                    // Use first 3 letters or appropriate abbreviation
                    labels.add(if (day.length > 3) day.substring(0, 3) else day)
                }
            }
            StatisticsViewModel.TimeRange.MONTH -> {
                // For month view, use the localized date keys from the frequency map
                val dates = frequencyMap.keys.toList()

                dates.forEachIndexed { index, dateStr ->
                    entries.add(BarEntry(index.toFloat(), frequencyMap[dateStr]?.toFloat() ?: 0f))
                    labels.add(dateStr)
                }

                // Limit the number of visible labels to avoid crowding
                if (labels.size > 10) {
                    val step = labels.size / 5
                    val filteredLabels = mutableListOf<String>()
                    for (i in labels.indices) {
                        if (i % step == 0 || i == labels.lastIndex) {
                            filteredLabels.add(labels[i])
                        } else {
                            filteredLabels.add("")
                        }
                    }
                    labels.clear()
                    labels.addAll(filteredLabels)
                }
            }
            StatisticsViewModel.TimeRange.YEAR -> {
                // Use the keys from frequency map, which are already localized month names
                val months = frequencyMap.keys.toList()

                android.util.Log.d("StatisticsActivity", "Rendering year view with months: $months")
                android.util.Log.d("StatisticsActivity", "Year frequency map: $frequencyMap")

                months.forEachIndexed { index, month ->
                    // Make sure we're getting the right data from frequency map
                    val value = frequencyMap[month]?.toFloat() ?: 0f
                    android.util.Log.d("StatisticsActivity", "Month: $month, Value: $value")
                    entries.add(BarEntry(index.toFloat(), value))
                    labels.add(month)
                }

                // Turn on value labels for months
                val dataSet = BarDataSet(entries, getString(R.string.training_sessions))
                dataSet.apply {
                    color = resources.getColor(R.color.primary, theme)
                    valueTextColor = resources.getColor(R.color.primary_variant, theme)
                    valueTextSize = 12f
                    setDrawValues(true)
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return if (value > 0) value.toInt().toString() else ""
                        }
                    }
                }

                // Set data to chart
                trainingChart.data = BarData(dataSet)
                trainingChart.axisLeft.axisMaximum = (entries.maxOfOrNull { it.y } ?: 0f) + 1f

                // Ensure enough space for labels
                trainingChart.xAxis.labelCount = labels.size

                // Set X axis labels
                trainingChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

                // Make sure labels are visible for year view
                trainingChart.xAxis.labelRotationAngle = 30f

                // Animate and refresh
                trainingChart.animateY(500)
                trainingChart.invalidate()
            }
            StatisticsViewModel.TimeRange.ALL -> {
                // For all time, show years (sorted)
                val years = frequencyMap.keys.toList()
                years.forEachIndexed { index, year ->
                    entries.add(BarEntry(index.toFloat(), frequencyMap[year]?.toFloat() ?: 0f))
                    labels.add(year)
                }

                // Turn on value labels for years
                val dataSet = BarDataSet(entries, getString(R.string.training_sessions))
                dataSet.apply {
                    color = resources.getColor(R.color.primary, theme)
                    valueTextColor = resources.getColor(R.color.primary_variant, theme)
                    valueTextSize = 12f
                    setDrawValues(true)
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return if (value > 0) value.toInt().toString() else ""
                        }
                    }
                }

                // Set data to chart
                trainingChart.data = BarData(dataSet)
                trainingChart.axisLeft.axisMaximum = (entries.maxOfOrNull { it.y } ?: 0f) + 1f

                // Ensure enough space for labels
                trainingChart.xAxis.labelCount = labels.size

                // Set X axis labels
                trainingChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

                // Make sure labels are visible for years view
                trainingChart.xAxis.labelRotationAngle = 0f

                // Animate and refresh
                trainingChart.animateY(500)
                trainingChart.invalidate()
                return
            }
        }

        // Create bar data set with styling
        val dataSet = BarDataSet(entries, getString(R.string.training_sessions))
        dataSet.apply {
            color = resources.getColor(R.color.secondary, theme)
            valueTextColor = resources.getColor(R.color.primary, theme)
            valueTextSize = 12f
            setDrawValues(true)
        }

        // Set data to chart
        trainingChart.data = BarData(dataSet)
        trainingChart.axisLeft.axisMaximum = (entries.maxOfOrNull { it.y } ?: 0f) + 1f

        // Ensure enough space for labels
        trainingChart.xAxis.labelCount = labels.size

        // Set X axis labels
        trainingChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        // Make sure labels are visible for year view
        if (currentTimeRange == StatisticsViewModel.TimeRange.YEAR) {
            trainingChart.xAxis.labelRotationAngle = 30f
        } else {
            trainingChart.xAxis.labelRotationAngle = 0f
        }

        // Animate and refresh
        trainingChart.animateY(500)
        trainingChart.invalidate()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, PrincipalActivity::class.java))
                    finish()
                    true
                }

                R.id.navigation_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    finish()
                    true
                }

                R.id.navigation_statistics -> true
                else -> false
            }
        }

        // Marcar la navegación de estadísticas como seleccionada
        binding.bottomNavigation.selectedItemId = R.id.navigation_statistics
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle home button click
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear the reference to avoid memory leaks
        (application as PowerGymApplication).statisticsViewModel = null
    }
}