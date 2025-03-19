package com.amarina.powergym.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.amarina.powergym.PowerGymApplication
import com.amarina.powergym.R
import com.amarina.powergym.databinding.ActivityPrincipalBinding
import com.amarina.powergym.ui.activity.EjercicioDetailActivity
import com.amarina.powergym.ui.adapter.EjercicioAdapter
import com.amarina.powergym.ui.activity.ProfileActivity
import com.amarina.powergym.ui.activity.StatisticsActivity
import com.amarina.powergym.ui.settings.SettingsActivity
import com.amarina.powergym.ui.viewmodel.main.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: EjercicioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            (application as PowerGymApplication).mainViewModelFactory
        )[MainViewModel::class.java]

        setupAdapter()
        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupNavigation()
        observeViewModel()
    }

    private fun setupAdapter() {
        adapter = EjercicioAdapter { ejercicio ->
            viewModel.navigateToEjercicioDetail(ejercicio.id)
        }
    }

    private fun setupRecyclerView() {
        binding.rvEjercicios.apply {
            layoutManager = GridLayoutManager(this@PrincipalActivity, 2).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (adapter?.getItemViewType(position)) {
                            0 -> 2 // Header ocupa 2 celdas
                            else -> 1 // Items normales ocupan 1 celda
                        }
                    }
                }
            }
            adapter = this@PrincipalActivity.adapter
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setConsulta(newText ?: "")
                return true
            }
        })
    }

    private fun setupFilters() {
        binding.chipDias.setOnClickListener {
            showDiasDialog()
        }

        binding.chipDificultad.setOnClickListener {
            showDificultadDialog()
        }

        binding.chipGrupoMuscular.setOnClickListener {
            showGrupoMuscularDialog()
        }
    }

    private fun showDiasDialog() {
        val dias = resources.getStringArray(R.array.dias_semana)
        val seleccionados = viewModel.diasSeleccionados.value.toTypedArray()
        val checkedItems = dias.map { it in seleccionados }.toBooleanArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("Selecciona los días")
            .setMultiChoiceItems(dias, checkedItems) { _, which, isChecked ->
                val selectedDias = viewModel.diasSeleccionados.value.toMutableSet()
                if (isChecked) {
                    selectedDias.add(dias[which])
                } else {
                    selectedDias.remove(dias[which])
                }
                viewModel.setDiasSeleccionados(selectedDias)
            }
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun showDificultadDialog() {
        val dificultades = resources.getStringArray(R.array.dificultades)
        val selectedIndex = viewModel.dificultadesSeleccionadas.value
            .singleOrNull()
            ?.let { dificultades.indexOf(it) }
            ?: -1

        MaterialAlertDialogBuilder(this)
            .setTitle("Selecciona la dificultad")
            .setSingleChoiceItems(dificultades, selectedIndex) { dialog, which ->
                val selectedDificultades = setOf(dificultades[which])
                viewModel.setDificultadesSeleccionadas(selectedDificultades)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showGrupoMuscularDialog() {
        lifecycleScope.launch {
            val gruposMuscular = viewModel.musculosState.value.toTypedArray()
            val selectedIndex = viewModel.grupoMuscularSeleccionado.value
                ?.let { gruposMuscular.indexOf(it) }
                ?: -1

            MaterialAlertDialogBuilder(this@PrincipalActivity)
                .setTitle("Selecciona el grupo muscular")
                .setSingleChoiceItems(gruposMuscular, selectedIndex) { dialog, which ->
                    viewModel.setGrupoMuscularSeleccionado(gruposMuscular[which])
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
               R.id.navigation_search -> {
                    viewModel.search()
                    false
                }

                R.id.navigation_statistics -> {
                    viewModel.navigateToStatistics()
                    false
                }
                else -> false
            }
        }

        binding.cardProfile.setOnClickListener {
            viewModel.navigateToProfile()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.ejerciciosState.collectLatest { state ->
                when (state) {
                    is MainViewModel.EjerciciosState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.rvEjercicios.visibility = View.GONE
                        binding.tvEmpty.visibility = View.GONE
                    }
                    is MainViewModel.EjerciciosState.Success -> {
                        binding.progressBar.visibility = View.GONE

                        if (state.ejerciciosBySection.isEmpty()) {
                            binding.rvEjercicios.visibility = View.GONE
                            binding.tvEmpty.visibility = View.VISIBLE
                        } else {
                            binding.rvEjercicios.visibility = View.VISIBLE
                            binding.tvEmpty.visibility = View.GONE

                            val sections = state.ejerciciosBySection.toList()
                            adapter.updateSections(sections)
                        }
                    }
                    is MainViewModel.EjerciciosState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rvEjercicios.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.tvEmpty.text = state.message
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.navigationEvents.collectLatest { event ->
                when (event) {
                    is MainViewModel.NavigationEvent.ToEjercicioDetail -> {
                        val intent = Intent(this@PrincipalActivity, EjercicioDetailActivity::class.java)
                        intent.putExtra(EjercicioDetailActivity.EXTRA_EJERCICIO_ID, event.ejercicioId)
                        startActivity(intent)
                    }
                    is MainViewModel.NavigationEvent.ToSettings -> {
                        startActivity(Intent(this@PrincipalActivity, SettingsActivity::class.java))
                    }
                    is MainViewModel.NavigationEvent.ToProfile -> {
                        startActivity(Intent(this@PrincipalActivity, ProfileActivity::class.java))
                    }
                    is MainViewModel.NavigationEvent.ToStatistics -> {
                        startActivity(Intent(this@PrincipalActivity, StatisticsActivity::class.java))
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.diasSeleccionados.collectLatest { dias ->
                updateChipDiasText(dias)
            }
        }

        lifecycleScope.launch {
            viewModel.dificultadesSeleccionadas.collectLatest { dificultades ->
                updateChipDificultadText(dificultades)
            }
        }

        lifecycleScope.launch {
            viewModel.grupoMuscularSeleccionado.collectLatest { grupo ->
                updateChipGrupoMuscularText(grupo)
            }
        }
    }

    private fun updateChipDiasText(dias: Set<String>) {
        binding.chipDias.text = when {
            dias.isEmpty() -> "Todos los días"
            dias.size == 1 -> dias.first()
            else -> "${dias.size} días"
        }
    }

    private fun updateChipDificultadText(dificultades: Set<String>) {
        binding.chipDificultad.text = when {
            dificultades.isEmpty() -> "Cualquier dificultad"
            else -> dificultades.first()
        }
    }

    private fun updateChipGrupoMuscularText(grupo: String?) {
        binding.chipGrupoMuscular.text = grupo ?: "Todos los grupos"
    }
}
