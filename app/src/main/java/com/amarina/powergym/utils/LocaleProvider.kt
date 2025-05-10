package com.amarina.powergym.utils

import android.content.Context
import java.util.Locale

/**
 * Provides access to the app's current locale based on user preferences
 * This ensures consistent language use throughout the application
 */
object LocaleProvider {
    /**
     * Get the current app locale based on saved preferences
     * @param context Application context
     * @return The current Locale that should be used for translations/formatting
     */
    fun getAppLocale(context: Context): Locale {
        val languageCode = PreferenceManager(context).getLanguage()
        return if (languageCode.isNullOrEmpty()) {
            Locale.getDefault()
        } else {
            Locale(languageCode)
        }
    }

    /**
     * Get a SimpleDateFormat configured with the app's locale
     * @param pattern The date format pattern
     * @param context Application context
     * @return A SimpleDateFormat using the app's locale
     */
    fun getLocalizedDateFormat(pattern: String, context: Context): java.text.SimpleDateFormat {
        return java.text.SimpleDateFormat(pattern, getAppLocale(context))
    }
}