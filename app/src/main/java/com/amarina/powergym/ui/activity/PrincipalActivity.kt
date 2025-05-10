package com.amarina.powergym.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.amarina.powergym.PowerGymApplication
import com.amarina.powergym.R
import com.amarina.powergym.databinding.ActivityPrincipalBinding
import com.amarina.powergym.ui.adapter.exercise.EjercicioAdapter
import com.amarina.powergym.ui.adapter.exercise.EjercicioAdapterItem
import com.amarina.powergym.ui.viewmodel.main.MainViewModel
import com.amarina.powergym.utils.LanguageHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: EjercicioAdapter
    private var currentDifficulty: String? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.establecerIdioma(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is admin and redirect to AdminExerciseActivity
        val sessionManager = (application as PowerGymApplication).sessionManager
        if (sessionManager.esAdmin()) {
            startActivity(Intent(this, AdminExerciseActivity::class.java))
            finish()
            return
        }

        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            (application as PowerGymApplication).mainViewModelFactory
        )[MainViewModel::class.java]

        setupAdapter()
        setupRecyclerView()
        setupFilters()
        setupNavigation()
        observeViewModel()
    }

    private fun setupAdapter() {
        adapter = EjercicioAdapter { ejercicio ->
            // Navegar a la pantalla de detalles del ejercicio
            val intent = Intent(this, EjercicioDetailActivity::class.java)
            intent.putExtra("ejercicio_id", ejercicio)
            startActivity(intent)
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


    private fun setupFilters() {
        binding.chipDificultad.setOnClickListener {
            showDificultadDialog()
        }

        binding.chipDificultad.setOnCloseIconClickListener {
            currentDifficulty = null
            binding.chipDificultad.text = getString(R.string.difficulty)
            binding.chipDificultad.isCloseIconVisible = false
            viewModel.clearDificultadFilter()
            binding.chipDificultad.isChecked = false
        }
    }


    private fun showDificultadDialog() {
        // Opciones visibles para el usuario
        val dificultadesDisplay = arrayOf(
            getString(R.string.basic),
            getString(R.string.medium),
            getString(R.string.advanced)
        )

        // Valores reales en la base de datos
        val dificultadesDB = arrayOf(
            "basico",
            "intermedio",
            "avanzado"
        )

        // Determinar el índice de selección actual
        val currentDisplayValue = when(currentDifficulty) {
            "basico" -> getString(R.string.basic)
            "intermedio" -> getString(R.string.medium)
            "avanzado" -> getString(R.string.advanced)
            else -> null
        }

        val currentIndex = if (currentDisplayValue != null) {
            dificultadesDisplay.indexOf(currentDisplayValue)
        } else {
            -1
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_difficulty))
            .setSingleChoiceItems(dificultadesDisplay, currentIndex) { dialog, which ->
                // Usar el valor de la BD para filtrar
                val dbValue = dificultadesDB[which]
                val displayValue = dificultadesDisplay[which]

                // Guardar la selección y aplicar filtro
                currentDifficulty = dbValue
                applyDifficultyFilter(dbValue, displayValue)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun applyDifficultyFilter(dbValue: String, displayValue: String) {
        // Actualizar UI del chip
        binding.chipDificultad.text = "${getString(R.string.difficulty)}: $displayValue"
        binding.chipDificultad.isCloseIconVisible = true
        binding.chipDificultad.isChecked = true

        // Aplicar filtro usando el valor de la BD
        viewModel.setDificultadFilter(dbValue)
    }







    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    false
                }
                R.id.navigation_statistics -> {
                    startActivity(Intent(this, StatisticsActivity::class.java))
                    false
                }
                else -> false
            }
        }

        binding.cvPerfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.ejercicios.collectLatest { state ->
                when (state) {
                    is MainViewModel.EjerciciosState.Loading -> {
                        binding.progressBar.visibility = android.view.View.VISIBLE
                        binding.rvEjercicios.visibility = android.view.View.GONE
                        binding.tvEmpty.visibility = android.view.View.GONE
                    }
                    is MainViewModel.EjerciciosState.Success -> {
                        binding.progressBar.visibility = android.view.View.GONE

                        if (state.ejercicios.isEmpty()) {
                            binding.rvEjercicios.visibility = android.view.View.GONE
                            binding.tvEmpty.visibility = android.view.View.VISIBLE
                            binding.tvEmpty.text = getString(R.string.no_results)
                        } else {
                            binding.rvEjercicios.visibility = android.view.View.VISIBLE
                            binding.tvEmpty.visibility = android.view.View.GONE

                            // SOLO actualiza las secciones con los ejercicios
                            adapter.updateSections(state.ejercicios)

                            // ELIMINA esta línea que sobreescribe con lista vacía
                            // adapter.submitList(adapterItems)
                        }
                    }
                    is MainViewModel.EjerciciosState.Error -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        binding.rvEjercicios.visibility = android.view.View.GONE
                        binding.tvEmpty.visibility = android.view.View.VISIBLE
                        binding.tvEmpty.text = getString(R.string.no_results)
                    }
                }
            }
        }
    }

}
