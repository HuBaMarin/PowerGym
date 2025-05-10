package com.amarina.powergym.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SettingsHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun getIdioma(): String = prefs.getString("idioma", "Español") ?: "Español"
    fun setIdioma(value: String) { prefs.edit() { putString("idioma", value) } }

    fun isSonidoActivo(): Boolean = prefs.getBoolean("sonido", true)
    fun setSonidoActivo(value: Boolean) { prefs.edit() { putBoolean("sonido", value) } }

    fun isRecordatoriosActivo(): Boolean = prefs.getBoolean("recordatorios", true)
    fun setRecordatoriosActivo(value: Boolean) { prefs.edit() { putBoolean("recordatorios", value) } }

    fun getFrecuenciaRecordatorios(): String = prefs.getString("frecuencia_recordatorios", "Diario") ?: "Diario"
    fun setFrecuenciaRecordatorios(value: String) { prefs.edit() {
        putString(
            "frecuencia_recordatorios",
            value
        )
    } }
}

