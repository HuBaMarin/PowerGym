package com.amarina.powergym.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amarina.powergym.PowerGymApplication
import com.amarina.powergym.R
import com.amarina.powergym.databinding.ActivityEjercicioDetailBinding
import com.amarina.powergym.ui.ejercicio.EjercicioDetailViewModel
import com.amarina.powergym.utils.showToast
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EjercicioDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEjercicioDetailBinding
    private lateinit var viewModel: EjercicioDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEjercicioDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            (application as PowerGymApplication).ejercicioDetailViewModelFactory
        )[EjercicioDetailViewModel::class.java]

        setupToolbar()
        setupListeners()
        loadEjercicio()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnVerVideo.setOnClickListener {
            viewModel.ejercicioState.value.let { state ->
                if (state is EjercicioDetailViewModel.EjercicioState.Success) {
                    val videoUrl = state.ejercicio.videoUrl
                    if (videoUrl.isNotEmpty()) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                        startActivity(intent)
                    } else {
                        showToast("No hay video disponible para este ejercicio")
                    }
                }
            }
        }
    }

    private fun loadEjercicio() {
        val ejercicioId = intent.getIntExtra(EXTRA_EJERCICIO_ID, -1)
        if (ejercicioId == -1) {
            showToast("Error al cargar el ejercicio")
            finish()
            return
        }

        viewModel.loadEjercicio(ejercicioId)
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

                        binding.toolbar.title = ejercicio.nombre
                        binding.tvGrupoMuscular.text = ejercicio.grupoMuscular
                        binding.tvDificultad.text = ejercicio.dificultad
                        binding.tvDias.text = ejercicio.dias
                        binding.tvDescripcion.text = ejercicio.descripcion.takeIf { it.isNotEmpty() }
                            ?: "No hay descripción disponible para este ejercicio."
                        binding.tvCalorias.text = "${ejercicio.calorias} calorías por sesión"

                        Picasso.get()
                            .load(ejercicio.urlEjercicio)
                            .placeholder(R.drawable.placeholder_ejercicio)
                            .error(R.drawable.error_imagen)
                            .into(binding.ivEjercicio)

                        binding.btnVerVideo.isEnabled = ejercicio.videoUrl.isNotEmpty()
                    }
                    is EjercicioDetailViewModel.EjercicioState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        showToast(state.message)
                        finish()
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_EJERCICIO_ID = "ejercicio_id"
    }
}
