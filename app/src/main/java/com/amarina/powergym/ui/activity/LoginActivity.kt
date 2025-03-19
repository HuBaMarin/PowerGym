package com.amarina.powergym.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amarina.powergym.PowerGymApplication
import com.amarina.powergym.databinding.ActivityLoginBinding
import com.amarina.powergym.ui.viewmodel.auth.LoginViewModel
import com.amarina.powergym.ui.viewmodel.auth.factory.LoginViewModelFactory
import com.amarina.powergym.utils.Utils
import com.amarina.powergym.utils.showToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, LoginViewModelFactory(
            (application as PowerGymApplication).userRepository,
            (application as PowerGymApplication).sessionManager
        )
        )[LoginViewModel::class.java]

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            viewModel.navigateToRegister()
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

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

        return isValid
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.loginState.collectLatest { state ->
                when (state) {
                    is LoginViewModel.LoginState.Idle -> {
                        // Estado inicial, no hacer nada
                    }
                    is LoginViewModel.LoginState.Loading -> {
                        binding.pbLogin.visibility = View.VISIBLE
                        binding.btnLogin.isEnabled = false
                    }
                    is LoginViewModel.LoginState.Success -> {
                        binding.pbLogin.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                    }
                    is LoginViewModel.LoginState.Error -> {
                        binding.pbLogin.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        showToast(state.message)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.navigationEvents.collectLatest { event ->
                when (event) {
                    is LoginViewModel.NavigationEvent.ToMain -> {
                        navigateToMain()
                    }
                    is LoginViewModel.NavigationEvent.ToRegister -> {
                        navigateToRegister()
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, PowerGymApplication::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
