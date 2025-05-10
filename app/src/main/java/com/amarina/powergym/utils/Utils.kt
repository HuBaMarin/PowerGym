package com.amarina.powergym.utils

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    /**
     * Valida si una cadena de texto tiene formato de correo electrónico válido.
     *
     * @param email El correo electrónico a validar
     * @return true si el correo es válido, false en caso contrario
     */
    fun esEmailValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Valida si una contraseña cumple con los requisitos mínimos de seguridad.
     *
     * @param password La contraseña a validar
     * @return true si la contraseña tiene al menos 6 caracteres, false en caso contrario
     */
    fun esContrasenaValida(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Convierte un timestamp en milisegundos a una cadena de texto con formato de fecha.
     *
     * @param timestamp El tiempo en milisegundos desde epoch (1 enero 1970)
     * @return Una cadena con la fecha en formato dd/MM/yyyy
     */
    fun formatearFecha(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }


}

/**
 * Función de extensión para mostrar un mensaje Toast corto en una actividad.
 *
 * @param message El mensaje a mostrar en el Toast
 */
fun AppCompatActivity.mostrarToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}