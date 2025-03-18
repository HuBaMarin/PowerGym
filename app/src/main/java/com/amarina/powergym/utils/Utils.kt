package com.amarin.powergym.utils

import com.amarina.powergym.ui.authex

object Utils {
    fun esEmailValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun esPasswordValido(password: String): Boolean {
        return password.length >= 6
    }

    fun esNombreValido(nombre: String): Boolean {
        return nombre.length >= 3
    }

    fun mostrarMensaje(autenticacionActivity: authex, s: String) {
        android.widget.Toast.makeText(autenticacionActivity, s, android.widget.Toast.LENGTH_SHORT).show()

    }


}