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
import com.amarina.powergym.databinding.ActivityLoginBinding
import com.amarina.powergym.ui.viewmodel.auth.LoginViewModel
import com.amarina.powergym.utils.LanguageHelper
import com.amarina.powergym.utils.Utils
import com.amarina.powergym.utils.mostrarToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.establecerIdioma(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            (application as PowerGymApplication).loginViewModelFactory
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

        if (!Utils.esEmailValido(email)) {
            binding.etEmail.error = getString(R.string.invalid_email)
            isValid = false
        } else {
            binding.etEmail.error = null
        }

        if (!Utils.esContrasenaValida(password)) {
            binding.etPassword.error = getString(R.string.invalid_password)
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
                        binding.pbLogin.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                    }
                    is LoginViewModel.LoginState.Loading -> {
                        binding.pbLogin.visibility = View.VISIBLE
                        binding.btnLogin.isEnabled = false
                    }
                    is LoginViewModel.LoginState.Success -> {
                        binding.pbLogin.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                    }
                    is LoginViewModel.LoginState.AdminSuccess -> {
                        binding.pbLogin.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                    }
                    is LoginViewModel.LoginState.AdminRedirect -> {
                        binding.pbLogin.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                    }
                    is LoginViewModel.LoginState.Locked -> {
                        binding.pbLogin.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        mostrarToast(getString(R.string.account_locked_message))

                        // Show a dialog with more information
                        androidx.appcompat.app.AlertDialog.Builder(this@LoginActivity)
                            .setTitle(R.string.account_locked_title)
                            .setMessage(R.string.account_locked_message)
                            .setPositiveButton(R.string.accept, null)
                            .show()
                    }
                    is LoginViewModel.LoginState.Error -> {
                        binding.pbLogin.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        mostrarToast(state.message)
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
                    is LoginViewModel.NavigationEvent.ToAdminVerification -> {
                        navigateToAdminVerification()
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, PrincipalActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToAdminVerification() {
        val intent = Intent(this, AdminVerificationActivity::class.java)

        val state = viewModel.loginState.value
        if (state is LoginViewModel.LoginState.AdminRedirect) {
            intent.putExtra("email", state.email)
            intent.putExtra("password", state.password)
        }

        startActivity(intent)
    }
}
