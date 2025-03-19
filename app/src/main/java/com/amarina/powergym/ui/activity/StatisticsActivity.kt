package com.amarina.powergym.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amarina.powergym.PowerGymApplication
import com.amarina.powergym.R
import com.amarina.powergym.databinding.ActivityStatisticsBinding
import com.amarina.powergym.ui.adapter.StatisticsAdapter
import com.amarina.powergym.ui.statistics.StatisticsViewModel
import com.amarina.powergym.utils.showToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var viewModel: StatisticsViewModel
    private lateinit var adapter: StatisticsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            (application as PowerGymApplication).statisticsViewModelFactory
        )[StatisticsViewModel::class.java]

        setupToolbar()
        setupAdapter()
        setupChips()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupAdapter() {
        adapter = StatisticsAdapter()
        binding.rvEstadisticas.adapter = adapter
    }

    private fun setupChips() {
        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipWeek -> viewModel.loadStatisticsByDateRange(StatisticsViewModel.DateRange.WEEK)
                R.id.chipMonth -> viewModel.loadStatisticsByDateRange(StatisticsViewModel.DateRange.MONTH)
                R.id.chipYear -> viewModel.loadStatisticsByDateRange(StatisticsViewModel.DateRange.YEAR)
                R.id.chipAll -> viewModel.loadStatisticsByDateRange(StatisticsViewModel.DateRange.ALL)
            }
        }

        // Seleccionar la semana por defecto
        binding.chipWeek.isChecked = true
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.statisticsState.collectLatest { state ->
                when (state) {
                    is StatisticsViewModel.StatisticsState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.rvEstadisticas.visibility = View.GONE
                        binding.tvEmpty.visibility = View.GONE
                    }
                    is StatisticsViewModel.StatisticsState.Empty -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rvEstadisticas.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                    }
                    is StatisticsViewModel.StatisticsState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rvEstadisticas.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE

                        adapter.submitList(state.estadisticas)
                    }
                    is StatisticsViewModel.StatisticsState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rvEstadisticas.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.tvEmpty.text = state.message
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.totalState.collectLatest { state ->
                when (state) {
                    is StatisticsViewModel.TotalState.Loading -> {
                        binding.cardTotals.visibility = View.INVISIBLE
                    }
                    is StatisticsViewModel.TotalState.Success -> {
                        binding.cardTotals.visibility = View.VISIBLE

                        val totals = state.totals
                        binding.tvTotalEjercicios.text = totals.ejercicios.toString()
                        binding.tvTotalCalorias.text = "${totals.calorias} cal"
                        binding.tvTotalTiempo.text = Utils.formatTime(totals.tiempo)
                    }
                    is StatisticsViewModel.TotalState.Error -> {
                        binding.cardTotals.visibility = View.INVISIBLE
                        showToast(state.message)
                    }
                }
            }
        }
    }
}
