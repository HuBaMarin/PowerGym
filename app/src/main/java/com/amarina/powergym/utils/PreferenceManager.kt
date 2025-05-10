package com.amarina.powergym.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.amarina.powergym.database.PowerGymDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.content.edit

class PreferenceManager(private val context: Context) {
    private val database = PowerGymDatabase.getInstance(context)
    private val preferenciaDao = database.preferenciaDao()
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("power_gym_prefs", Context.MODE_PRIVATE)
    }

    private val authPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("auth_preferences", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_LANGUAGE = "language_code"
    }

    // Methods for authentication/admin-related preferences
    fun getInt(key: String, defaultValue: Int): Int {
        return authPrefs.getInt(key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return authPrefs.getLong(key, defaultValue)
    }

    fun putInt(key: String, value: Int) {
        authPrefs.edit { putInt(key, value) }
    }

    fun putLong(key: String, value: Long) {
        authPrefs.edit { putLong(key, value) }
    }

    suspend fun obtenerPreferenciasUsuario(userId: Int) = withContext(Dispatchers.IO) {
        preferenciaDao.obtenerPreferenciaSync(userId)
    }





    /**
     * Set the language preference
     */
    fun setLanguage(languageCode: String) {
        sharedPreferences.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    /**
     * Get the current language preference, defaults to system language if not set
     */
    fun getLanguage(): String? {
        return sharedPreferences.getString(KEY_LANGUAGE, null)
    }
}