package com.amarina.powergym.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amarina.powergym.PowerGymApplication
import com.amarina.powergym.databinding.ActivityProfileBinding
import com.amarina.powergym.utils.Utils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.amarina.powergym.R
import com.amarina.powergym.utils.LanguageHelper
import com.amarina.powergym.utils.mostrarToast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.amarina.powergym.ui.viewholder.profile.ProfileViewModel

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: ProfileViewModel

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.establecerIdioma(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            (application as PowerGymApplication).profileViewModelFactory
        )[ProfileViewModel::class.java]

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
        binding.btnEditName.setOnClickListener {
            showEditNameDialog()
        }

        binding.btnEditEmail.setOnClickListener {
            showEditEmailDialog()
        }

        binding.btnEditPassword.setOnClickListener {
            showEditPasswordDialog()
        }

        binding.btnSettings.setOnClickListener {
            viewModel.navigateToSettings()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        binding.btnSave.setOnClickListener {
            viewModel.saveUserProfile()
        }


    }

    private fun showEditNameDialog() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_name, null)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.etDialogInput)
        editText.setText(binding.tvName.text)

        MaterialAlertDialogBuilder(this)
            .setTitle("Editar nombre")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.updateUserName(newName)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditEmailDialog() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_email, null)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.etDialogInput)
        val inputLayout = dialogView.findViewById<TextInputLayout>(R.id.tilDialogInput)

        inputLayout.hint = "Email"
        editText.setText(binding.tvEmail.text)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Editar email")
            .setView(dialogView)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val newEmail = editText.text.toString().trim()
            if (!Utils.esEmailValido(newEmail)) {
                inputLayout.error = "Email no válido"
            } else {
                dialog.dismiss()
                viewModel.updateUserEmail(newEmail)
            }
        }
    }

    private fun showEditPasswordDialog() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_password, null)
        val etCurrentPassword = dialogView.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        val tilCurrentPassword = dialogView.findViewById<TextInputLayout>(R.id.tilCurrentPassword)
        val tilNewPassword = dialogView.findViewById<TextInputLayout>(R.id.tilNewPassword)
        val tilConfirmPassword = dialogView.findViewById<TextInputLayout>(R.id.tilConfirmPassword)

       val dialog = MaterialAlertDialogBuilder(this)
           .setTitle("Cambiar contraseña")
           .setView(dialogView as View)
           .setPositiveButton("Guardar", null)
           .setNegativeButton("Cancelar", null)
           .create()

        dialog.show()

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val currentPassword = etCurrentPassword.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            var isValid = true

            if (currentPassword.isEmpty()) {
                tilCurrentPassword.error = "Campo requerido"
                isValid = false
            } else {
                tilCurrentPassword.error = null
            }

            if (!Utils.esContrasenaValida(newPassword)) {
                tilNewPassword.error = "La contraseña debe tener al menos 6 caracteres"
                isValid = false
            } else {
                tilNewPassword.error = null
            }

            if (newPassword != confirmPassword) {
                tilConfirmPassword.error = "Las contraseñas no coinciden"
                isValid = false
            } else {
                tilConfirmPassword.error = null
            }

            if (isValid) {
                if (viewModel.updateUserPassword(currentPassword, newPassword)) {
                    dialog.dismiss()
                    mostrarToast("Contraseña actualizada")
                } else {
                    tilCurrentPassword.error = "Contraseña actual incorrecta"
                }
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                viewModel.logout()
                val intent = Intent(this, AutenticacionActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showDeleteAccountConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar cuenta")
            .setMessage("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteUserAccount()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.profileState.collectLatest { state ->
                when (state) {
                    is ProfileViewModel.ProfileState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.contentLayout.visibility = View.GONE
                    }
                    is ProfileViewModel.ProfileState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.contentLayout.visibility = View.VISIBLE

                        val user = state.user
                        binding.tvName.text = user.nombre.takeIf { it.isNotEmpty() } ?: "Sin nombre"
                        binding.tvEmail.text = user.email
                        binding.tvName.text = user.nombre
                        binding.tvRegistrationDate.text = Utils.formatearFecha(user.fechaRegistro)
                    }
                    is ProfileViewModel.ProfileState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        mostrarToast(state.message)
                        finish()
                    }

                }
            }
        }

        lifecycleScope.launch {
            viewModel.updateState.collectLatest { state ->
                when (state) {
                    is ProfileViewModel.UpdateState.Idle -> {
                        // Estado inicial, no hacer nada
                    }
                    is ProfileViewModel.UpdateState.Updating -> {
                        binding.btnSave.isEnabled = false
                        binding.btnSave.text = "Guardando..."
                    }
                    is ProfileViewModel.UpdateState.Success -> {
                        binding.btnSave.isEnabled = true
                        binding.btnSave.text = "Guardar cambios"
                    }
                    is ProfileViewModel.UpdateState.Error -> {
                        binding.btnSave.isEnabled = true
                        binding.btnSave.text = "Error al guardar"
                        mostrarToast(state.message)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.navigationEvents.collectLatest { event ->
                when (event) {
                    is ProfileViewModel.NavigationEvent.Back -> {
                        finish()
                    }
                    is ProfileViewModel.NavigationEvent.ToSettings -> {
                        startActivity(Intent(this@ProfileActivity, SettingsActivity::class.java))
                    }
                    is ProfileViewModel.NavigationEvent.ToLogin -> {
                        val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}
