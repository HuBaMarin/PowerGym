package com.amarina.powergym.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amarina.powergym.PowerGymApplication
import com.amarina.powergym.R
import com.amarina.powergym.ui.viewmodel.auth.LoginViewModel
import com.amarina.powergym.utils.LanguageHelper
import kotlinx.coroutines.launch

class AutenticacionActivity : AppCompatActivity() {

    private lateinit var spinnerIdioma: Spinner
    private lateinit var viewModel: LoginViewModel

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.establecerIdioma(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel =
            (application as PowerGymApplication).loginViewModelFactory.create(LoginViewModel::class.java)


        val sessionManager = (application as PowerGymApplication).sessionManager
        if (sessionManager.estaAutenticado()) {
            val targetActivity = if (sessionManager.esAdmin()) {
                AdminExerciseActivity::class.java
            } else {
                PrincipalActivity::class.java
            }

            val intent = Intent(this, targetActivity)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_autenticacion)

        val btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)
        val btnIniciarSesion = findViewById<Button>(R.id.btnIniciarSesion)

        initListeners(btnRegistrarse, btnIniciarSesion)
        setupLanguajes()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvents.collect { event ->
                    when (event) {
                        is LoginViewModel.NavigationEvent.ToMain -> {
                            val targetActivity =
                                if ((application as PowerGymApplication).sessionManager.esAdmin()) {
                                    AdminExerciseActivity::class.java
                                } else {
                                    PrincipalActivity::class.java
                                }
                            val intent = Intent(this@AutenticacionActivity, targetActivity)
                            startActivity(intent)
                            finish()
                        }
                        is LoginViewModel.NavigationEvent.ToRegister -> {
                            val intent = Intent(this@AutenticacionActivity, RegisterActivity::class.java)
                            startActivity(intent)
                        }
                        is LoginViewModel.NavigationEvent.ToAdminVerification -> {
                            val intent = Intent(
                                this@AutenticacionActivity,
                                AdminExerciseActivity::class.java
                            )
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun initListeners(btnRegistrarse: Button?, btnIniciarSesion: Button?) {
        btnRegistrarse?.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnIniciarSesion?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupLanguajes() {
        spinnerIdioma = findViewById(R.id.spinnerIdioma)

        val idiomas = listOf(
            getString(R.string.language_spanish),
            getString(R.string.language_english),
            getString(R.string.language_french),
            getString(R.string.language_german),
            getString(R.string.language_japanese)
        )
        val codigos = listOf("es", "en", "fr", "de", "ja")

        val adapter = ArrayAdapter(this, R.layout.spinner_item, idiomas)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerIdioma.adapter = adapter

        // Establecer el idioma actual
        val currentLang = LanguageHelper.obtenerIdioma(this)
        spinnerIdioma.setSelection(codigos.indexOf(currentLang))

        // Agregar listener para cambios de idioma
        spinnerIdioma.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedLanguageCode = codigos[position]
                val currentLanguage = LanguageHelper.obtenerIdioma(this@AutenticacionActivity)
                if (selectedLanguageCode != currentLanguage) {
                    LanguageHelper.configurarIdioma(
                        this@AutenticacionActivity,
                        selectedLanguageCode
                    )
                    recreate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun recreateActivity() {
        val intent = Intent(this, AutenticacionActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }


}