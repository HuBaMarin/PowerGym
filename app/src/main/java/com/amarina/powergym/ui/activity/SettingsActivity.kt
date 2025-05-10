package com.amarina.powergym.ui.activity

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.amarina.powergym.R
import com.amarina.powergym.databinding.ActivitySettingsBinding
import com.amarina.powergym.utils.LanguageHelper
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences

    companion object {
        const val PREFS_NAME = "app_settings"
        const val THEME_PREF = "theme_pref"
        const val LANGUAGE_PREF = "language_pref"

        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setupToolbar()
        setupThemeToggles()
        setupLanguageSelection()
        setupAppVersion()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupThemeToggles() {
        // Get current theme settings
        val isDarkMode = isDarkModeActive()
        val isSystemDefault = isSystemDefault()

        // Update UI to reflect current settings
        binding.switchDarkMode.isChecked = isDarkMode
        binding.switchSystemDefault.isChecked = isSystemDefault

        // Disable dark mode toggle if system default is on
        binding.switchDarkMode.isEnabled = !isSystemDefault

        // Set up toggle listeners
        binding.darkModeContainer.setOnClickListener {
            if (!binding.switchSystemDefault.isChecked) {
                val newDarkMode = !binding.switchDarkMode.isChecked
                binding.switchDarkMode.isChecked = newDarkMode
                setDarkMode(newDarkMode)
            }
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (!binding.switchSystemDefault.isChecked) {
                setDarkMode(isChecked)
            }
        }

        binding.systemDefaultContainer.setOnClickListener {
            val newSystemDefault = !binding.switchSystemDefault.isChecked
            binding.switchSystemDefault.isChecked = newSystemDefault
            setSystemDefault(newSystemDefault)
            binding.switchDarkMode.isEnabled = !newSystemDefault
        }

        binding.switchSystemDefault.setOnCheckedChangeListener { _, isChecked ->
            setSystemDefault(isChecked)
            binding.switchDarkMode.isEnabled = !isChecked
        }
    }

    private fun setupLanguageSelection() {
        // Get current language setting
        val currentLanguage = getCurrentLanguage()

        // Set up the radio group
        val radioGroup = binding.radioGroupLanguage

        // Clear any existing radio buttons
        radioGroup.removeAllViews()

        // Create a map of supported languages with their display names
        val languages = mapOf(
            "en" to getString(R.string.english),
            "es" to getString(R.string.spanish),
            "de" to getString(R.string.german),
            "fr" to getString(R.string.french),
            "ja" to getString(R.string.japanese)
        )

        // Create radio buttons for each language
        for ((langCode, langName) in languages) {
            val radioButton = RadioButton(this)
            radioButton.text = langName
            radioButton.id = View.generateViewId()
            radioButton.isChecked = langCode == currentLanguage
            radioButton.setTextColor(getColor(R.color.on_surface))
            radioButton.setPadding(16, 16, 16, 16)

            // Add click listener
            radioButton.setOnClickListener {
                setLanguage(langCode)
                recreate()
            }

            // Add to radio group
            radioGroup.addView(radioButton)

            // Add divider except after the last item
            if (langCode != languages.keys.last()) {
                val divider = View(this)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
                divider.layoutParams = params
                divider.setBackgroundColor(getColor(R.color.divider_color))
                divider.alpha = 0.12f
                radioGroup.addView(divider)
            }
        }
    }

    private fun setupAppVersion() {
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        binding.appVersionNumber.text = versionName
    }

    private fun isDarkModeActive(): Boolean {
        return when (prefs.getInt(THEME_PREF, THEME_SYSTEM)) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            else -> {
                // If system default, check the current system night mode
                val currentNightMode =
                    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                currentNightMode == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    private fun isSystemDefault(): Boolean {
        return prefs.getInt(THEME_PREF, THEME_SYSTEM) == THEME_SYSTEM
    }

    private fun setDarkMode(darkMode: Boolean) {
        val themeMode = if (darkMode) THEME_DARK else THEME_LIGHT
        prefs.edit { putInt(THEME_PREF, themeMode) }

        val nightMode = if (darkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun setSystemDefault(useSystemDefault: Boolean) {
        if (useSystemDefault) {
            prefs.edit { putInt(THEME_PREF, THEME_SYSTEM) }
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun getCurrentLanguage(): String {
        return prefs.getString(LANGUAGE_PREF, Locale.getDefault().language) ?: "en"
    }

    private fun setLanguage(languageCode: String) {
        prefs.edit { putString(LANGUAGE_PREF, languageCode) }
        LanguageHelper.configurarIdioma(this, languageCode)

        // Force restart the activity to apply language changes
        recreate()

        // In a real app, we might want to restart the entire app
        // to ensure all activities have the new language
    }
}