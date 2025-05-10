package com.amarina.powergym.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amarina.powergym.PowerGymApplication
import com.amarina.powergym.R
import com.amarina.powergym.databinding.ActivityRegisterBinding
import com.amarina.powergym.ui.viewmodel.auth.RegisterViewModel
import com.amarina.powergym.utils.LanguageHelper
import com.amarina.powergym.utils.Utils
import com.amarina.powergym.utils.mostrarToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.establecerIdioma(newBase))
    }

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
            val name = binding.etNameRegister.text.toString().trim()
            val email = binding.etEmailRegister.text.toString().trim()
            val password = binding.etPasswordRegister.text.toString().trim()
            val confirmPassword = binding.etConfirmPasswordRegister.text.toString().trim()

            if (validateInputs(name, email, password, confirmPassword)) {
                viewModel.register(email, password, name)
            }
        }
    }

    private fun validateInputs(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (name.isBlank()) {
            binding.etNameRegister.error = getString(R.string.required_name)
            isValid = false
        } else {
            binding.etNameRegister.error = null
        }

        if (!Utils.esEmailValido(email)) {
            binding.etEmailRegister.error = getString(R.string.invalid_email)
            isValid = false
        } else {
            binding.etEmailRegister.error = null
        }

        if (!Utils.esContrasenaValida(password)) {
            binding.etPasswordRegister.error = getString(R.string.invalid_password)
            isValid = false
        } else {
            binding.etPasswordRegister.error = null
        }

        if (password != confirmPassword) {
            binding.etConfirmPasswordRegister.error = getString(R.string.passwords_dont_match)
            isValid = false
        } else {
            binding.etConfirmPasswordRegister.error = null
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
                        mostrarToast(state.message)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.navigationEvents.collectLatest { event ->
                when (event) {
                    is RegisterViewModel.NavigationEvent.ToLogin -> {
                        mostrarToast("Registro exitoso, inicia sesión")
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