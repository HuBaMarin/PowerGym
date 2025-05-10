package com.amarina.powergym.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarina.powergym.PowerGymApplication
import com.amarina.powergym.R
import com.amarina.powergym.databinding.ActivityAdminExerciseBinding
import com.amarina.powergym.ui.adapter.admin.ExerciseAdapter
import com.amarina.powergym.ui.viewmodel.admin.AdminExerciseViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminExerciseBinding
    private lateinit var viewModel: AdminExerciseViewModel
    private lateinit var exerciseAdapter: ExerciseAdapter
    
    private var sections = listOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the session manager from application
        val sessionManager = (application as PowerGymApplication).sessionManager

        // Bypass the admin check for debugging purposes
        /*
        // Verify admin access
        if (!sessionManager.esAdmin()) {
            showAccessDeniedDialog()
            return
        }
        */

        setupViewModel()
        setupRecyclerView()
        setupListeners()
        loadSections()
        observeViewModel()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            (application as PowerGymApplication).adminExerciseViewModelFactory
        )[AdminExerciseViewModel::class.java]
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseAdapter(
            onEditClick = { exerciseId ->
                showEditExerciseDialog(exerciseId)
            },
            onDeleteClick = { exerciseId ->
                showDeleteConfirmationDialog(exerciseId)
            }
        )

        binding.rvExercises.apply {
            layoutManager = LinearLayoutManager(this@AdminExerciseActivity)
            adapter = exerciseAdapter
        }
    }

    private fun loadSections() {
        lifecycleScope.launch {
            viewModel.loadSections()
        }
    }

    private fun setupListeners() {
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchExercises(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchExercises(it) }
                return true
            }
        })

        binding.btnAddExercise.setOnClickListener {
            showAddExerciseDialog()
        }

        binding.btnRefresh.setOnClickListener {
            onRefreshClicked()
        }

        binding.btnBack.setOnClickListener {
            logoutAndRedirect()
        }

        binding.btnLogOut.setOnClickListener {
            logoutAndRedirect()
        }

        binding.spinnerFilter.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedSection = if (position > 0) sections[position - 1] else null
                    viewModel.filterBySection(selectedSection)
                }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                viewModel.filterBySection(null)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.exercises.collectLatest { exercises ->
                exerciseAdapter.submitList(exercises)
                binding.tvNoExercises.visibility =
                    if (exercises.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.loading.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.sections.collectLatest { sectionsList ->
                sections = sectionsList
                setupSectionSpinner(sectionsList)
            }
        }

        lifecycleScope.launch {
            viewModel.muscleGroups.collectLatest { muscleGroups ->
                val adapter = ArrayAdapter(
                    this@AdminExerciseActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    muscleGroups
                )
                // This is used to update any open dialogs with muscle groups
                val dialog =
                    (this@AdminExerciseActivity as? android.app.Activity)?.window?.decorView?.rootView
                dialog?.findViewById<AutoCompleteTextView>(R.id.actvMuscleGroup)
                    ?.setAdapter(adapter)
            }
        }

        lifecycleScope.launch {
            viewModel.difficultyValues.collectLatest { difficulties ->
                val adapter = ArrayAdapter(
                    this@AdminExerciseActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    difficulties
                )
                // This is used to update any open dialogs with difficulty values
                val dialog =
                    (this@AdminExerciseActivity as? android.app.Activity)?.window?.decorView?.rootView
                dialog?.findViewById<AutoCompleteTextView>(R.id.actvDifficulty)?.setAdapter(adapter)
            }
        }

        lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                error?.let {
                    AlertDialog.Builder(this@AdminExerciseActivity)
                        .setTitle("Error")
                        .setMessage(it)
                        .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                        .show()
                    viewModel.clearError()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.selectedExercise.collectLatest { exercise ->
                if (exercise != null) {
                    showEditExerciseDialog(exercise)
                }
            }
        }
    }

    private fun setupSectionSpinner(sections: List<String>) {
        val items = mutableListOf("All Sections")
        items.addAll(sections)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = adapter
    }

    private fun showAccessDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.access_denied)
            .setMessage(R.string.admin_access_required)
            .setPositiveButton(R.string.accept) { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun showAddExerciseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_exercise, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        val etName = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)
        val actvMuscleGroup = dialogView.findViewById<AutoCompleteTextView>(R.id.actvMuscleGroup)
        val actvDifficulty = dialogView.findViewById<AutoCompleteTextView>(R.id.actvDifficulty)
        val etTrainingDays = dialogView.findViewById<TextInputEditText>(R.id.etTrainingDays)
        val etImageUrl = dialogView.findViewById<TextInputEditText>(R.id.etImageUrl)
        val etVideoUrl = dialogView.findViewById<TextInputEditText>(R.id.etVideoUrl)
        val actvSection = dialogView.findViewById<AutoCompleteTextView>(R.id.actvSection)
        val etCalories = dialogView.findViewById<TextInputEditText>(R.id.etCalories)
        val etFrequency = dialogView.findViewById<TextInputEditText>(R.id.etFrequency)
        val etPercentage = dialogView.findViewById<TextInputEditText>(R.id.etPercentage)

        setupMuscleGroupDropdown(actvMuscleGroup)
        setupDifficultyDropdown(actvDifficulty)
        setupSectionDropdown(actvSection)

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                // Validate and gather all inputs
                if (validateInputs(
                        etName, etDescription, actvMuscleGroup, actvDifficulty,
                        etTrainingDays, etImageUrl, actvSection, etCalories,
                        etFrequency, etPercentage
                    )
                ) {
                    try {
                        // Submit the exercise to the viewModel
                        viewModel.addExercise(
                            name = etName.text.toString().trim(),
                            description = etDescription.text.toString().trim(),
                            muscleGroup = actvMuscleGroup.text.toString().trim(),
                            difficulty = actvDifficulty.text.toString().trim(),
                            trainingDays = etTrainingDays.text.toString().trim(),
                            imageUrl = etImageUrl.text.toString().trim(),
                            videoUrl = etVideoUrl.text.toString().trim(),
                            section = actvSection.text.toString().trim(),
                            calories = etCalories.text.toString().toIntOrNull() ?: 0,
                            frequency = etFrequency.text.toString().toIntOrNull() ?: 0,
                            percentage = etPercentage.text.toString().toFloatOrNull() ?: 0f
                        )
                        dialog.dismiss()
                    } catch (e: Exception) {
                        showSnackbarError(getString(R.string.error_adding_exercise))
                    }
                }
            }
        }

        dialog.show()
    }

    private fun setupMuscleGroupDropdown(autoCompleteTextView: AutoCompleteTextView) {
        lifecycleScope.launch {
            viewModel.muscleGroups.collectLatest { muscleGroups ->
                val adapter = ArrayAdapter(
                    this@AdminExerciseActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    muscleGroups
                )
                autoCompleteTextView.setAdapter(adapter)
            }
        }
    }

    private fun setupDifficultyDropdown(autoCompleteTextView: AutoCompleteTextView) {
        lifecycleScope.launch {
            viewModel.difficultyValues.collectLatest { difficulties ->
                val adapter = ArrayAdapter(
                    this@AdminExerciseActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    difficulties
                )
                autoCompleteTextView.setAdapter(adapter)
            }
        }
    }

    private fun setupSectionDropdown(autoCompleteTextView: AutoCompleteTextView) {
        lifecycleScope.launch {
            viewModel.sections.collectLatest { sections ->
                val adapter = ArrayAdapter(
                    this@AdminExerciseActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    sections
                )
                autoCompleteTextView.setAdapter(adapter)
            }
        }
    }

    private fun validateInputs(
        etName: TextInputEditText,
        etDescription: TextInputEditText,
        actvMuscleGroup: AutoCompleteTextView,
        actvDifficulty: AutoCompleteTextView,
        etTrainingDays: TextInputEditText,
        etImageUrl: TextInputEditText,
        actvSection: AutoCompleteTextView,
        etCalories: TextInputEditText,
        etFrequency: TextInputEditText,
        etPercentage: TextInputEditText
    ): Boolean {
        var isValid = true

        // Required fields
        if (etName.text.isNullOrBlank()) {
            (etName.parent.parent as? TextInputLayout)?.error = getString(R.string.required_field)
            isValid = false
        } else {
            (etName.parent.parent as? TextInputLayout)?.error = null
        }

        if (actvMuscleGroup.text.isNullOrBlank()) {
            (actvMuscleGroup.parent.parent as? TextInputLayout)?.error =
                getString(R.string.required_field)
            isValid = false
        } else {
            (actvMuscleGroup.parent.parent as? TextInputLayout)?.error = null
        }

        if (actvDifficulty.text.isNullOrBlank()) {
            (actvDifficulty.parent.parent as? TextInputLayout)?.error =
                getString(R.string.required_field)
            isValid = false
        } else {
            (actvDifficulty.parent.parent as? TextInputLayout)?.error = null
        }

        if (etTrainingDays.text.isNullOrBlank()) {
            (etTrainingDays.parent.parent as? TextInputLayout)?.error =
                getString(R.string.required_field)
            isValid = false
        } else {
            (etTrainingDays.parent.parent as? TextInputLayout)?.error = null
        }

        if (etImageUrl.text.isNullOrBlank()) {
            (etImageUrl.parent.parent as? TextInputLayout)?.error =
                getString(R.string.required_field)
            isValid = false
        } else {
            (etImageUrl.parent.parent as? TextInputLayout)?.error = null
        }

        if (actvSection.text.isNullOrBlank()) {
            (actvSection.parent.parent as? TextInputLayout)?.error =
                getString(R.string.required_field)
            isValid = false
        } else {
            (actvSection.parent.parent as? TextInputLayout)?.error = null
        }

        if (etCalories.text.isNullOrBlank()) {
            (etCalories.parent.parent as? TextInputLayout)?.error =
                getString(R.string.required_field)
            isValid = false
        } else {
            (etCalories.parent.parent as? TextInputLayout)?.error = null
        }

        if (etFrequency.text.isNullOrBlank()) {
            (etFrequency.parent.parent as? TextInputLayout)?.error =
                getString(R.string.required_field)
            isValid = false
        } else {
            (etFrequency.parent.parent as? TextInputLayout)?.error = null
        }

        if (etPercentage.text.isNullOrBlank()) {
            (etPercentage.parent.parent as? TextInputLayout)?.error =
                getString(R.string.required_field)
            isValid = false
        } else {
            (etPercentage.parent.parent as? TextInputLayout)?.error = null
        }

        return isValid
    }

    private fun showSnackbarError(message: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showEditExerciseDialog(exerciseId: Int) {
        // Load the exercise from the database first
        viewModel.getExerciseById(exerciseId)
        // The actual dialog is shown in the observer for selectedExercise
    }

    private fun showEditExerciseDialog(exercise: com.amarina.powergym.database.entities.Ejercicio) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_exercise, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.edit_exercise))
            .setView(dialogView)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        // Fill the dialog with existing exercise data
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etName).apply {
            setText(exercise.nombre)
        }
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription).apply {
            setText(exercise.descripcion)
        }
        val actvMuscleGroup =
            dialogView.findViewById<AutoCompleteTextView>(R.id.actvMuscleGroup).apply {
                setText(exercise.grupoMuscular)
            }
        val actvDifficulty =
            dialogView.findViewById<AutoCompleteTextView>(R.id.actvDifficulty).apply {
                setText(exercise.dificultad)
            }
        val etTrainingDays = dialogView.findViewById<TextInputEditText>(R.id.etTrainingDays).apply {
            setText(exercise.dias)
        }
        val etImageUrl = dialogView.findViewById<TextInputEditText>(R.id.etImageUrl).apply {
            setText(exercise.imagenEjercicio)
        }
        val etVideoUrl = dialogView.findViewById<TextInputEditText>(R.id.etVideoUrl).apply {
            setText(exercise.videoUrl)
        }
        val actvSection = dialogView.findViewById<AutoCompleteTextView>(R.id.actvSection).apply {
            setText(exercise.seccion)
        }
        val etCalories = dialogView.findViewById<TextInputEditText>(R.id.etCalories).apply {
            setText(exercise.calorias.toString())
        }
        val etFrequency = dialogView.findViewById<TextInputEditText>(R.id.etFrequency).apply {
            setText(exercise.frecuencia.toString())
        }
        val etPercentage = dialogView.findViewById<TextInputEditText>(R.id.etPercentage).apply {
            setText(exercise.porcentaje.toString())
        }

        setupMuscleGroupDropdown(actvMuscleGroup)
        setupDifficultyDropdown(actvDifficulty)
        setupSectionDropdown(actvSection)

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                // Validate and gather all inputs
                if (validateInputs(
                        etName, etDescription, actvMuscleGroup, actvDifficulty,
                        etTrainingDays, etImageUrl, actvSection, etCalories,
                        etFrequency, etPercentage
                    )
                ) {
                    try {
                        // Update the exercise using the viewModel
                        viewModel.updateExercise(
                            exerciseId = exercise.id,
                            name = etName.text.toString().trim(),
                            description = etDescription.text.toString().trim(),
                            muscleGroup = actvMuscleGroup.text.toString().trim(),
                            difficulty = actvDifficulty.text.toString().trim(),
                            trainingDays = etTrainingDays.text.toString().trim(),
                            imageUrl = etImageUrl.text.toString().trim(),
                            videoUrl = etVideoUrl.text.toString().trim(),
                            section = actvSection.text.toString().trim(),
                            calories = etCalories.text.toString().toIntOrNull() ?: 0,
                            frequency = etFrequency.text.toString().toIntOrNull() ?: 0,
                            percentage = etPercentage.text.toString().toFloatOrNull() ?: 0f
                        )
                        dialog.dismiss()
                    } catch (e: Exception) {
                        showSnackbarError(getString(R.string.error_updating_exercise))
                    }
                }
            }
        }

        dialog.setOnDismissListener {
            // Clear the selected exercise when dialog is closed
            viewModel.clearSelectedExercise()
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(exerciseId: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_deletion)
            .setMessage(R.string.confirm_delete_exercise)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteExercise(exerciseId)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun onRefreshClicked() {
        viewModel.loadExercises()
    }

    private fun logoutAndRedirect() {
        // Get session manager and logout
        val sessionManager = (application as PowerGymApplication).sessionManager
        sessionManager.cerrarSesion()

        // Redirect to login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}