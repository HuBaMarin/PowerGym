package com.amarina.powergym.ui.activity

import android.content.Context
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
import com.amarina.powergym.utils.LanguageHelper
import com.amarina.powergym.utils.SessionManager
import com.amarina.powergym.utils.mostrarToast
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EjercicioDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEjercicioDetailBinding
    private lateinit var viewModel: EjercicioDetailViewModel
    private lateinit var sessionManager: SessionManager
    private var ejercicioId: Int = -1

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.establecerIdioma(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEjercicioDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        ejercicioId = intent.getIntExtra("ejercicio_id", -1)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.title = getString(R.string.exercise_details)
        binding.toolbar.setNavigationIcon(R.drawable.nav_back)

        val app = application as PowerGymApplication
        viewModel = ViewModelProvider(this, app.ejercicioDetailViewModelFactory)
            .get(EjercicioDetailViewModel::class.java)

        // Set context for proper string resource access
        viewModel.setContext(this)

        if (ejercicioId != -1) {
            viewModel.loadEjercicio(ejercicioId)
        } else {
            mostrarToast(getString(R.string.loading_difficulty_error))
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
                        val ejercicio = state.ejercicio
                        binding.apply {
                            tvNombreEjercicio.text = ejercicio.nombre
                            tvDescripcion.text = ejercicio.descripcion

                            tvGrupoMuscular.text = ejercicio.grupoMuscular
                            tvDificultad.text = ejercicio.dificultad

                            val translatedDays = ejercicio.dias.split(",").map { day ->
                                day.trim()
                            }.joinToString(", ")
                            tvDias.text = translatedDays

                            tvCalorias.text =
                                getString(R.string.calories_per_session, ejercicio.calorias)

                            if (ejercicio.imagenEjercicio.isNotEmpty()) {
                                Picasso.get()
                                    .load(ejercicio.imagenEjercicio)
                                    .placeholder(R.drawable.workout_placeholder_image)
                                    .error(R.drawable.baseline_error_24)
                                    .into(ivEjercicio)
                            }

                            btnVerVideo.setOnClickListener {
                                if (ejercicio.videoUrl.isNotEmpty()) {
                                    openVideo(ejercicio.videoUrl)
                                } else {
                                    mostrarToast(getString(R.string.general_error))
                                }
                            }
                        }
                    }

                    is EjercicioDetailViewModel.EjercicioState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        mostrarToast("${getString(R.string.general_error)}: ${state.message}")
                        finish()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.actionState.collectLatest { state ->
                when (state) {
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

                    else -> Unit
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnMostrarEstadisticas.setOnClickListener {
            sessionManager.establecerIdEjercicio(ejercicioId)
            val intent = Intent(this, StatisticsActivity::class.java)
            intent.putExtra("ejercicio_id", ejercicioId)
            startActivity(intent)
        }

        binding.btnCompletarEjercicio.setOnClickListener {
            showCompletionDialog()
        }
    }

    private fun showCompletionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_complete_exercise, null)
        AlertDialog.Builder(this)
            .setTitle(R.string.complete_exercise)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val series =
                    dialogView.findViewById<TextInputEditText>(R.id.etSeries).text.toString()
                        .toIntOrNull() ?: 0
                val repeticiones =
                    dialogView.findViewById<TextInputEditText>(R.id.etRepeticiones).text.toString()
                        .toIntOrNull() ?: 0
                val tiempo =
                    dialogView.findViewById<TextInputEditText>(R.id.etTiempo).text.toString()
                        .toIntOrNull() ?: 0
                viewModel.registrarEjercicioCompletado(ejercicioId, series, repeticiones, tiempo)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun openVideo(videoUrl: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, videoUrl.toUri()))
        } catch (e: Exception) {
            mostrarToast("${getString(R.string.general_error)}: ${e.message}")
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
