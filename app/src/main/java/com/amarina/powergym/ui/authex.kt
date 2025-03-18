package com.amarina.powergym.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.amarin.powergym.R
import com.amarin.powergym.database.PowerGymDatabase
import com.amarin.powergym.database.dao.UserDao
import com.amarin.powergym.database.entities.Usuario
import com.amarin.powergym.utils.Utils
import kotlinx.coroutines.launch

class authex : AppCompatActivity() {
    private lateinit var baseDatos: PowerGymDatabase
    private lateinit var usuarioDao: UserDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_autenticacion)

        initUI()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)
        val btnIniciarSesion = findViewById<Button>(R.id.btnIniciarSesion)

        initListeners(etEmail, etPassword, btnRegistrarse, btnIniciarSesion)

    }

    private fun initListeners(etEmail: EditText?, etPassword: EditText?, btnRegistrarse: Button, btnIniciarSesion: Button) {
        btnRegistrarse.setOnClickListener {
            val email = etEmail?.text.toString()
            val password = etPassword?.text.toString()

            if (!Utils.esEmailValido(email)) {
                Utils.mostrarMensaje(this, "Email no válido")
                return@setOnClickListener
            }

            if (!Utils.esPasswordValido(password)) {
                Utils.mostrarMensaje(this, "Contrasena no válida")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val nuevoUsuario = Usuario(
                    email = email,
                    password = password,
                    rol = "usuario",
                    notificacionesHabilitadas = true
                )
                val id = usuarioDao.insertUser(nuevoUsuario)
                runOnUiThread {
                    Utils.mostrarMensaje(this@authex, "Usuario registrado con id $id")
                }
            }
        }

        btnIniciarSesion.setOnClickListener {
            val email = etEmail?.text.toString()
            val password = etPassword?.text.toString()

            if (!Utils.esEmailValido(email)) {
                Utils.mostrarMensaje(this, "Email no válido")
                return@setOnClickListener
            }

            if (!Utils.esPasswordValido(password)) {
                Utils.mostrarMensaje(this, "Contrasena no válida")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val usuario = usuarioDao.loginUser(email, password)
                if (usuario == null) {
                    runOnUiThread {
                        Utils.mostrarMensaje(this@authex, "Usuario no encontrado")
                    }
                } else {
                    if (usuario.password == password) {
                        runOnUiThread {
                            Utils.mostrarMensaje(this@authex, "Usuario autenticado")
                        }
                    } else {
                        runOnUiThread {
                            Utils.mostrarMensaje(this@authex, "Contraseña incorrecta")
                        }
                    }
                }
            }
        }
    }

    private fun initUI() {
        baseDatos = Room.databaseBuilder(
            applicationContext,
            PowerGymDatabase::class.java, "powergym-baseDatos"
        ).build()
    }
}