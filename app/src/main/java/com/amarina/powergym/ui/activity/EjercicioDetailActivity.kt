package com.amarina.powergym.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amarina.powergym.PowerGymApplication
import com.amarina.powergym.R
import com.amarina.powergym.databinding.ActivityEjercicioDetailBinding
import com.amarina.powergym.ui.viewmodel.exercise.EjercicioDetailViewModel
import com.amarina.powergym.utils.SessionManager
import com.amarina.powergym.utils.mostrarToast
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

class EjercicioDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEjercicioDetailBinding
    private lateinit var viewModel: EjercicioDetailViewModel
    private lateinit var sessionManager: SessionManager
    private var ejercicioId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEjercicioDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Obtener el ID del ejercicio de los extras del intent
        ejercicioId = intent.getIntExtra("ejercicio_id", -1)

        // Configurar la toolbar con back arrow
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationIcon(R.drawable.nav_back)

        // Inicializar el ViewModel
        val app = application as PowerGymApplication
        viewModel = ViewModelProvider(this, app.ejercicioDetailViewModelFactory)
            .get(EjercicioDetailViewModel::class.java)

        if (ejercicioId != -1) {
            // Cargar el ejercicio usando el ViewModel
            viewModel.loadEjercicio(ejercicioId)
            // También cargar estadísticas relacionadas
            viewModel.loadEstadisticas(ejercicioId)
        } else {
            mostrarToast("No se pudo cargar el ejercicio")
            finish()
        }

        setupClickListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.ejercicioState.collectLatest { state ->
                when (state) {
                    is EjercicioDetailViewModel.EjercicioState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.contentLayout.visibility = View.GONE
                    }

                    is EjercicioDetailViewModel.EjercicioState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.contentLayout.visibility = View.VISIBLE

                        // Actualizar UI con los datos del ejercicio
                        val ejercicio = state.ejercicio
                        try {
                            binding.apply {
                                tvNombreEjercicio.text = ejercicio.nombre
                                tvDescripcion.text = ejercicio.descripcion
                                tvGrupoMuscular.text = translateMuscleGroup(ejercicio.grupoMuscular)
                                tvDificultad.text = translateDifficulty(ejercicio.dificultad)
                                tvDias.text = translateTrainingDays(ejercicio.dias)
                                tvCalorias.text =
                                    getString(R.string.calories_per_session, ejercicio.calorias)

                                // Cargar imagen con Picasso
                                if (ejercicio.imagenEjercicio.isNotEmpty()) {
                                    Picasso.get()
                                        .load(ejercicio.imagenEjercicio)
                                        .placeholder(R.drawable.workout_placeholder_image)
                                        .error(R.drawable.baseline_error_24)
                                        .into(ivEjercicio)
                                }

                                // Configurar botón para ver video
                                btnVerVideo.setOnClickListener {
                                    if (ejercicio.videoUrl.isNotEmpty()) {
                                        openVideo(ejercicio.videoUrl)
                                    } else {
                                        mostrarToast("No hay video disponible")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            mostrarToast("Error displaying exercise details: ${e.message}")
                            finish()
                        }
                    }

                    is EjercicioDetailViewModel.EjercicioState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        mostrarToast("Error: ${state.message}")
                        finish()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.actionState.collectLatest { state ->
                when (state) {
                    is EjercicioDetailViewModel.ActionState.Loading -> {
                        // Podríamos mostrar un indicador de carga si es necesario
                    }

                    is EjercicioDetailViewModel.ActionState.Success -> {
                        Toast.makeText(
                            this@EjercicioDetailActivity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        val appInstance = application as PowerGymApplication
                        appInstance.statisticsViewModel?.refreshData()
                        viewModel.resetActionState()
                    }

                    is EjercicioDetailViewModel.ActionState.Error -> {
                        Toast.makeText(
                            this@EjercicioDetailActivity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetActionState()
                    }

                    else -> {} // No hacer nada en estado Idle
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnMostrarEstadisticas.setOnClickListener {
            sessionManager.establecerIdEjercicio(ejercicioId)

            // Iniciar la actividad de estadísticas
            val intent = Intent(this, StatisticsActivity::class.java).apply {
                putExtra(StatisticsActivity.EXTRA_EJERCICIO_ID, ejercicioId)
            }
            startActivity(intent)
        }

        binding.btnCompletarEjercicio.setOnClickListener {
            showCompletionDialog()
        }
    }

    private fun showCompletionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_complete_exercise, null)

        val dialog = AlertDialog.Builder(this@EjercicioDetailActivity)
            .setTitle(R.string.complete_exercise)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val seriesEditText = dialogView.findViewById<TextInputEditText>(R.id.etSeries)
                val repeticionesEditText =
                    dialogView.findViewById<TextInputEditText>(R.id.etRepeticiones)
                val tiempoEditText = dialogView.findViewById<TextInputEditText>(R.id.etTiempo)

                val series = seriesEditText.text.toString().toIntOrNull() ?: 0
                val repeticiones = repeticionesEditText.text.toString().toIntOrNull() ?: 0
                val tiempo = tiempoEditText.text.toString().toIntOrNull() ?: 0

                // Registrar el ejercicio completado a través del ViewModel
                viewModel.registrarEjercicioCompletado(
                    ejercicioId,
                    series,
                    repeticiones,
                    tiempo
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.show()
    }

    private fun openVideo(videoUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, videoUrl.toUri())
            startActivity(intent)
        } catch (e: Exception) {
            mostrarToast("No se pudo abrir el video: ${e.message}")
        }
    }

    private fun translateTrainingDays(days: String): String {
        // Split the days string by commas
        val daysList = days.split(",").map { it.trim() }

        return daysList.map { day ->
            when (day) {
                "Monday" -> getString(R.string.day_monday)
                "Tuesday" -> getString(R.string.day_tuesday)
                "Wednesday" -> getString(R.string.day_wednesday)
                "Thursday" -> getString(R.string.day_thursday)
                "Friday" -> getString(R.string.day_friday)
                "Saturday" -> getString(R.string.day_saturday)
                "Sunday" -> getString(R.string.day_sunday)
                "Mon" -> getString(R.string.day_mon)
                "Tue" -> getString(R.string.day_tue)
                "Wed" -> getString(R.string.day_wed)
                "Thu" -> getString(R.string.day_thu)
                "Fri" -> getString(R.string.day_fri)
                "Sat" -> getString(R.string.day_sat)
                "Sun" -> getString(R.string.day_sun)
                else -> day
            }
        }.joinToString(", ")
    }

    private fun translateDifficulty(difficulty: String): String {
        return when (difficulty.lowercase()) {
            "beginner", "basico", "básico" -> getString(R.string.difficulty_basic)
            "intermediate", "intermedio" -> getString(R.string.difficulty_intermediate)
            "advanced", "avanzado" -> getString(R.string.difficulty_advanced)
            "adaptable" -> getString(R.string.difficulty_adaptable)
            else -> difficulty
        }
    }

    private fun translateMuscleGroup(muscleGroup: String): String {
        return when (muscleGroup.lowercase()) {
            "legs", "piernas" -> getString(R.string.muscle_group_legs)
            "arms", "brazos" -> getString(R.string.muscle_group_arms)
            "core" -> getString(R.string.muscle_group_core)
            "multiple", "múltiple", "multiples", "múltiples" -> getString(R.string.muscle_group_multiple)
            "shoulders", "hombros" -> getString(R.string.muscle_group_shoulders)
            "respiratory", "respiratorio" -> getString(R.string.muscle_group_respiratory)
            "forearms", "antebrazos" -> getString(R.string.muscle_group_forearms)
            "core and legs", "core y piernas" -> getString(R.string.muscle_group_core_legs)
            "chest", "pecho" -> getString(R.string.muscle_group_chest)
            "back", "espalda" -> getString(R.string.muscle_group_back)
            "glutes", "glúteos" -> getString(R.string.muscle_group_glutes)
            "full body", "cuerpo completo" -> getString(R.string.muscle_group_full_body)
            else -> muscleGroup
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}