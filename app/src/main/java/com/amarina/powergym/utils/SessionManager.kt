package com.amarina.powergym.utils

import android.content.Context
import android.content.SharedPreferences
import com.amarina.powergym.database.entities.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val editor = prefs.edit()

    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()

    init {
        // Cargar el usuario desde SharedPreferences
        val userId = getUserId()
        val email = getEmail()
        val role = getRole()
        val name = getName()

        if (userId != -1 && email.isNotEmpty()) {
            _currentUser.value = Usuario(
                id = userId,
                email = email,
                nombre = name,
                password = "", // No guardamos la contraseña en memoria
                rol = role
            )
        }
    }

    fun saveUserSession(usuario: Usuario) {
        editor.putInt(KEY_USER_ID, usuario.id)
        editor.putString(KEY_EMAIL, usuario.email)
        editor.putString(KEY_ROLE, usuario.rol)
        editor.putString(KEY_NAME, usuario.nombre)
        editor.putBoolean(KEY_LOGGED_IN, true)
        editor.apply()

        _currentUser.value = usuario
    }

    fun logout() {
        editor.clear()
        editor.apply()
        _currentUser.value = null
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_LOGGED_IN, false)
    }

    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    private fun getEmail(): String {
        return prefs.getString(KEY_EMAIL, "") ?: ""
    }

    private fun getRole(): String {
        return prefs.getString(KEY_ROLE, "") ?: ""
    }

    private fun getName(): String {
        return prefs.getString(KEY_NAME, "") ?: ""
    }

    companion object {
        private const val PREFS_NAME = "power_gym_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        private const val KEY_NAME = "name"
        private const val KEY_LOGGED_IN = "logged_in"
    }
}
