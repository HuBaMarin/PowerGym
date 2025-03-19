package com.amarina.powergym.ui.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amarina.powergym.PowerGymApplication
import com.amarina.powergym.R
import com.amarina.powergym.databinding.ActivitySettingsBinding
import com.amarina.powergym.ui.viewmodel.settings.SettingsViewModel
import com.amarina.powergym.utils.showToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            (application as PowerGymApplication).settingsViewModelFactory
        )[SettingsViewModel::class.java]

        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            viewModel.navigateBack()
        }
    }

    private fun setupListeners() {
        binding.switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateNotificaciones(isChecked)
        }

        binding.switchTema.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateTema(isChecked)
        }

        binding.switchRecordatorios.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateRecordatorios(isChecked)
        }

        binding.layoutFrecuencia.setOnClickListener {
            showFrecuenciaDialog()
        }

        binding.btnGuardar.setOnClickListener {
            viewModel.saveSettings()
        }
    }

    private fun showFrecuenciaDialog() {
        val frecuencias = resources.getStringArray(R.array.frecuencias)
        val currentFrecuencia = binding.tvFrecuenciaValue.text.toString()
        val currentIndex = frecuencias.indexOf(currentFrecuencia).coerceAtLeast(0)

        MaterialAlertDialogBuilder(this)
            .setTitle("Frecuencia de recordatorios")
            .setSingleChoiceItems(frecuencias, currentIndex) { dialog, which ->
                viewModel.updateFrecuencia(frecuencias[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.preferenciaState.collectLatest { state ->
                when (state) {
                    is SettingsViewModel.PreferenciaState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.contentLayout.visibility = View.GONE
                    }
                    is SettingsViewModel.PreferenciaState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.contentLayout.visibility = View.VISIBLE

                        val preferencia = state.preferencia
                        binding.switchNotificaciones.isChecked = preferencia.notificacionesHabilitadas
                        binding.switchTema.isChecked = preferencia.temaDark
                        binding.switchRecordatorios.isChecked = preferencia.recordatorios
                        binding.tvFrecuenciaValue.text = preferencia.frecuencia
                    }
                    is SettingsViewModel.PreferenciaState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        showToast(state.message)
                        finish()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.saveState.collectLatest { state ->
                when (state) {
                    is SettingsViewModel.SaveState.Idle -> {
                        // Estado inicial, no hacer nada
                    }
                    is SettingsViewModel.SaveState.Saving -> {
                        binding.btnGuardar.isEnabled = false
                        binding.btnGuardar.text = "Guardando..."
                    }
                    is SettingsViewModel.SaveState.Success -> {
                        binding.btnGuardar.isEnabled = true
                        binding.btnGuardar.text = "Guardar"
                        showToast("Preferencias guardadas")
                    }
                    is SettingsViewModel.SaveState.Error -> {
                        binding.btnGuardar.isEnabled = true
                        binding.btnGuardar.text = "Guardar"
                        showToast(state.message)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.navigationEvents.collectLatest { event ->
                when (event) {
                    is SettingsViewModel.NavigationEvent.Back -> {
                        finish()
                    }
                }
            }
        }
    }
}
