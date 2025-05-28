package com.amarina.powergym.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import com.amarina.powergym.database.PowerGymDatabase
import java.util.Locale

/**
 * Clase auxiliar para gestionar la configuración de idiomas en la aplicación
 */
object LanguageHelper {
    /**
     * Configura el idioma de la aplicación según el código de idioma proporcionado.
     * Actualiza la configuración del sistema y refresca las traducciones en la base de datos.
     *
     * @param context El contexto de la aplicación
     * @param languageCode El código del idioma a configurar (ej. "es", "en")
     */
    fun configurarIdioma(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        configuration.setLocale(locale)

        // Apply the configuration change immediately
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }

        PreferenceManager(context).setLanguage(languageCode)

        // Actualiza los nombres de ejercicios en la base de datos con el nuevo idioma
        refreshExerciseNames(context)

        // Llama directamente a updateTranslations para asegurar que ocurre inmediatamente
        try {
            PowerGymDatabase.updateExerciseTranslations(context)
        } catch (e: Exception) {
            Log.e("LanguageHelper", "Error actualizando traducciones: ${e.message}")
        }
    }

    /**
     * Obtiene el idioma actualmente configurado en las preferencias.
     *
     * @param context El contexto de la aplicación
     * @return El código del idioma configurado o el idioma predeterminado del sistema
     */
    fun obtenerIdioma(context: Context): String {
        return PreferenceManager(context).getLanguage() ?: Locale.getDefault().language
    }

    /**
     * Configura el contexto de la aplicación con la preferencia de idioma guardada.
     *
     * @param context El contexto original de la aplicación
     * @return Un nuevo contexto configurado con el idioma preferido
     */
    fun establecerIdioma(context: Context): Context {
        val languageCode = obtenerIdioma(context)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        val newContext = context.createConfigurationContext(configuration)

        // Actualiza los nombres de ejercicios en la base de datos con el nuevo idioma
        refreshExerciseNames(newContext)

        return newContext
    }

    /**
     * Actualiza los nombres de ejercicios en la base de datos con el idioma actual.
     *
     * @param context El contexto de la aplicación
     */
    private fun refreshExerciseNames(context: Context) {
        try {
            // This launches a coroutine that might not complete before the function returns
            // Let's use a more direct approach to ensure translations are applied
            PowerGymDatabase.updateExerciseTranslations(context)

            // Log detailed information about the language change
            val currentLocale = Locale.getDefault()
            Log.d(
                "LanguageHelper",
                "Exercise translation update requested: " +
                        "Language=${currentLocale.language}, " +
                        "Display Language=${currentLocale.displayLanguage}, " +
                        "Country=${currentLocale.country}"
            )
        } catch (e: Exception) {
            Log.e("LanguageHelper", "Error updating exercise translations: ${e.message}", e)
        }
    }
}
