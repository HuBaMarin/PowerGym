package com.amarina.powergym.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.amarin.powergym.database.PowerGymDatabase
import com.amarin.powergym.database.dao.UserDao
import com.amarin.powergym.database.entities.Usuario
import com.amarin.powergym.utils.Utils
import com.amarina.powergym.R
import kotlinx.coroutines.launch

class AutenticacionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_autenticacion)

        val btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)
        val btnIniciarSesion = findViewById<Button>(R.id.btnIniciarSesion)

        initListeners(btnRegistrarse, btnIniciarSesion)

    }

    private fun initListeners(btnRegistrarse: Button, btnIniciarSesion: Button) {
        btnRegistrarse.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnIniciarSesion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }


}