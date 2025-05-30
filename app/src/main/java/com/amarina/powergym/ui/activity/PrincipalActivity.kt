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
import com.amarina.powergym.ui.viewmodel.main.MainViewModel
import com.amarina.powergym.utils.LanguageHelper
import com.amarina.powergym.utils.TranslationHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: EjercicioAdapter
    private var currentDifficulty: String? = null
    private var currentSection: String? = null
    private var currentMuscleGroup: String? = null

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

        // Check if we should open settings after app restart (language change)
        if (intent.getBooleanExtra("open_settings", false)) {
            startActivity(Intent(this, SettingsActivity::class.java))
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
        adapter = EjercicioAdapter { ejercicioId ->
            // Navegar a la pantalla de detalles del ejercicio
            val intent = Intent(this, EjercicioDetailActivity::class.java)
            intent.putExtra("ejercicio_id", ejercicioId)
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

        binding.chipGroupSection.setOnClickListener {
            showSectionDialog()
        }

        binding.chipGroupSection.setOnCloseIconClickListener {
            currentSection = null
            binding.chipGroupSection.text = getString(R.string.group_section)
            binding.chipGroupSection.isCloseIconVisible = false
            viewModel.clearSectionFilter()
            binding.chipGroupSection.isChecked = false
        }

        binding.chipMuscleGroup.setOnClickListener {
            showMuscleGroupDialog()
        }

        binding.chipMuscleGroup.setOnCloseIconClickListener {
            currentMuscleGroup = null
            binding.chipMuscleGroup.text = getString(R.string.muscle_group)
            binding.chipMuscleGroup.isCloseIconVisible = false
            viewModel.clearMuscleGroupFilter()
            binding.chipMuscleGroup.isChecked = false
        }
    }

    private fun showDificultadDialog() {
        // Opciones visibles para el usuario
        val dificultadesDisplay = arrayOf(
            getString(R.string.difficulty_basic),
            getString(R.string.difficulty_intermediate),
            getString(R.string.difficulty_advanced)
        )

        // Valores reales en la base de datos (usar los mismos string resources)
        val dificultadesDB = arrayOf(
            getString(R.string.difficulty_basic),
            getString(R.string.difficulty_intermediate),
            getString(R.string.difficulty_advanced)
        )

        // Determinar el índice de selección actual
        val currentDisplayValue = when(currentDifficulty) {
            getString(R.string.difficulty_basic) -> getString(R.string.difficulty_basic)
            getString(R.string.difficulty_intermediate) -> getString(R.string.difficulty_intermediate)
            getString(R.string.difficulty_advanced) -> getString(R.string.difficulty_advanced)
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

    private fun showSectionDialog() {
        lifecycleScope.launch {
            try {
                // Get sections from database directly (already translated)
                val sectionsFromDB = viewModel.secciones.value

                if (sectionsFromDB.isEmpty()) {
                    return@launch
                }

                // Translate sections for display using TranslationHelper
                val sectionsDisplay = sectionsFromDB.map { section ->
                    translateSectionForDisplay(section)
                }.toTypedArray()

                // Determine current selection index
                val currentIndex = if (currentSection != null) {
                    sectionsFromDB.indexOf(currentSection)
                } else {
                    -1
                }

                AlertDialog.Builder(this@PrincipalActivity)
                    .setTitle(getString(R.string.filter_by_group))
                    .setSingleChoiceItems(sectionsDisplay, currentIndex) { dialog, which ->
                        // Use the selected section value directly from database
                        val selectedSection = sectionsFromDB[which]
                        val displayValue = sectionsDisplay[which]

                        // Save selection and apply filter
                        currentSection = selectedSection
                        applySectionFilter(selectedSection, displayValue)
                        dialog.dismiss()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            } catch (e: Exception) {
                // Handle any errors
            }
        }
    }

    private fun applySectionFilter(dbValue: String, displayValue: String) {
        // Update chip UI
        binding.chipGroupSection.text = "${getString(R.string.group_section)}: $displayValue"
        binding.chipGroupSection.isCloseIconVisible = true
        binding.chipGroupSection.isChecked = true

        // Apply filter using DB value
        viewModel.setSectionFilter(dbValue)
    }

    private fun showMuscleGroupDialog() {
        lifecycleScope.launch {
            try {
                // Get muscle groups from viewModel
                val muscleGroups = viewModel.musculos.value

                if (muscleGroups.isEmpty()) {
                    return@launch
                }

                // Translate muscle groups for display
                val muscleGroupsDisplay = muscleGroups.map { muscleGroup ->
                    translateMuscleGroupForDisplay(muscleGroup)
                }.toTypedArray()

                // Determine current selection index
                val currentIndex = if (currentMuscleGroup != null) {
                    muscleGroups.indexOf(currentMuscleGroup)
                } else {
                    -1
                }

                AlertDialog.Builder(this@PrincipalActivity)
                    .setTitle(getString(R.string.filter_by_group))
                    .setSingleChoiceItems(
                        muscleGroupsDisplay,
                        currentIndex
                    ) { dialog, which ->
                        // Use the original muscle group value for filtering (database value)
                        val selectedMuscleGroup = muscleGroups[which]
                        val displayValue = muscleGroupsDisplay[which]

                        // Save selection and apply filter
                        currentMuscleGroup = selectedMuscleGroup
                        applyMuscleGroupFilter(selectedMuscleGroup, displayValue)
                        dialog.dismiss()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            } catch (e: Exception) {
                // Handle any errors
            }
        }
    }

    private fun applyMuscleGroupFilter(muscleGroup: String, displayValue: String) {
        // Update chip UI with translated display value
        binding.chipMuscleGroup.text = "${getString(R.string.muscle_group)}: $displayValue"
        binding.chipMuscleGroup.isCloseIconVisible = true
        binding.chipMuscleGroup.isChecked = true

        // Apply filter using original muscle group value from database
        viewModel.setMuscleGroupFilter(muscleGroup)
    }

    /**
     * Translate muscle group names for display in filter dialog
     */
    private fun translateMuscleGroupForDisplay(muscleGroup: String): String {
        return TranslationHelper.translateMuscleGroup(muscleGroup, this)
    }

    /**
     * Translate section names for display in filter dialog
     */
    private fun translateSectionForDisplay(section: String): String {
        return TranslationHelper.translateSection(section, this)
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
