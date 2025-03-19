package com.amarina.powergym.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amarina.powergym.PowerGymApplication
import com.amarina.powergym.databinding.ActivityRegisterBinding
import com.amarina.powergym.ui.viewmodel.auth.RegisterViewModel
import com.amarina.powergym.utils.Utils
import com.amarina.powergym.utils.showToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            (application as PowerGymApplication).registerViewModelFactory
        )[RegisterViewModel::class.java]

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInputs(name, email, password, confirmPassword)) {
                viewModel.register(email, password, name)
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            viewModel.navigateBack()
        }
    }

    private fun validateInputs(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (name.isBlank()) {
            binding.etName.error = "Nombre requerido"
            isValid = false
        } else {
            binding.etName.error = null
        }

        if (!Utils.isValidEmail(email)) {
            binding.etEmail.error = "Email no válido"
            isValid = false
        } else {
            binding.etEmail.error = null
        }

        if (!Utils.isValidPassword(password)) {
            binding.etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        } else {
            binding.etPassword.error = null
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Las contraseñas no coinciden"
            isValid = false
        } else {
            binding.etConfirmPassword.error = null
        }

        return isValid
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.registerState.collectLatest { state ->
                when (state) {
                    is RegisterViewModel.RegisterState.Idle -> {
                        // Estado inicial, no hacer nada
                    }
                    is RegisterViewModel.RegisterState.Loading -> {
                        binding.progressBarRegister.visibility = View.VISIBLE
                        binding.btnRegister.isEnabled = false
                    }
                    is RegisterViewModel.RegisterState.Success -> {
                        binding.progressBarRegister.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                    }
                    is RegisterViewModel.RegisterState.Error -> {
                        binding.progressBarRegister.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        showToast(state.message)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.navigationEvents.collectLatest { event ->
                when (event) {
                    is RegisterViewModel.NavigationEvent.ToLogin -> {
                        showToast("Registro exitoso, inicia sesión")
                        navigateToLogin()
                    }
                    is RegisterViewModel.NavigationEvent.Back -> {
                        finish()
                    }
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
