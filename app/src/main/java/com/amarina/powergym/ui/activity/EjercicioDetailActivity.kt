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

        // Configurar la toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

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
                        binding.apply {
                            tvNombreEjercicio.text = ejercicio.nombre
                            tvDescripcion.text = ejercicio.descripcion
                            tvGrupoMuscular.text = ejercicio.grupoMuscular
                            tvDificultad.text = ejercicio.dificultad
                            tvDias.text = ejercicio.dias
                            tvCalorias.text = "${ejercicio.calorias} calorías por sesión"

                            // Cargar imagen con Picasso
                            if (ejercicio.imagenEjercicio.isNotEmpty()) {
                                Picasso.get()
                                    .load(ejercicio.imagenEjercicio)
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
            .setTitle("Completar ejercicio")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
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
            .setNegativeButton("Cancelar", null)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}