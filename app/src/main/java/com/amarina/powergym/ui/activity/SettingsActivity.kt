package com.amarina.powergym.ui.activity

import android.content.Context
import android.content.Intent
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
import com.amarina.powergym.utils.ReminderManager
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences

    companion object {
        const val PREFS_NAME = "app_settings"
        const val THEME_PREF = "theme_pref"
        const val LANGUAGE_PREF = "language_pref"
        const val NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val NOTIFICATION_FREQUENCY = "notification_frequency"

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
        setupNotifications()
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

            radioButton.setOnClickListener {
                if (langCode != currentLanguage) {
                    radioButton.isChecked = false
                    showLanguageConfirmationDialog(langCode, langName) { confirmed ->
                        if (confirmed) {
                            radioButton.isChecked = true
                            setLanguage(langCode)
                        } else {
                            radioGroup.findViewWithTag<RadioButton>(currentLanguage)?.isChecked =
                                true
                        }
                    }
                }
            }

            radioButton.tag = langCode
            radioGroup.addView(radioButton)

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

    private fun showLanguageConfirmationDialog(
        langCode: String,
        langName: String,
        callback: (Boolean) -> Unit
    ) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.change_language))
            .setMessage(getString(R.string.change_language_confirmation, langName))
            .setPositiveButton(getString(R.string.change)) { _, _ ->
                callback(true)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                callback(false)
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun setupNotifications() {
        val notificationsEnabled = prefs.getBoolean(NOTIFICATIONS_ENABLED, true)
        val notificationFrequency =
            prefs.getString(NOTIFICATION_FREQUENCY, getString(R.string.frequency_daily))

        binding.switchNotifications.isChecked = notificationsEnabled
        binding.tvReminderFrequency.text = notificationFrequency

        binding.notificationsContainer.setOnClickListener {
            val newValue = !binding.switchNotifications.isChecked
            binding.switchNotifications.isChecked = newValue
            setNotificationEnabled(newValue)
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            setNotificationEnabled(isChecked)
        }

        binding.reminderFrequencyContainer.setOnClickListener {
            if (binding.switchNotifications.isChecked) {
                showFrequencySelectionDialog()
            }
        }
    }

    private fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(NOTIFICATIONS_ENABLED, enabled)
        }

        val reminderManager = ReminderManager(this)
        if (enabled) {
            val frequency =
                prefs.getString(NOTIFICATION_FREQUENCY, getString(R.string.frequency_daily))
                    ?: getString(R.string.frequency_daily)
            reminderManager.programarRecordatorios(frequency)
        } else {
            reminderManager.cancelarRecordatorios()
        }
    }

    private fun showFrequencySelectionDialog() {
        val frequencies = arrayOf(
            getString(R.string.frequency_daily),
            getString(R.string.frequency_every_other_day),
            getString(R.string.frequency_twice_weekly),
            getString(R.string.frequency_weekly),
            getString(R.string.frequency_monthly)
        )

        val currentFrequency =
            prefs.getString(NOTIFICATION_FREQUENCY, getString(R.string.frequency_daily))
        val currentIndex = frequencies.indexOf(currentFrequency)

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.reminder_frequency))
            .setSingleChoiceItems(frequencies, currentIndex) { dialog, which ->
                val selectedFrequency = frequencies[which]
                prefs.edit {
                    putString(NOTIFICATION_FREQUENCY, selectedFrequency)
                }
                binding.tvReminderFrequency.text = selectedFrequency

                // Update the reminder schedule if notifications are enabled
                if (prefs.getBoolean(NOTIFICATIONS_ENABLED, true)) {
                    val reminderManager = ReminderManager(this)
                    reminderManager.programarRecordatorios(selectedFrequency)
                }

                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupAppVersion() {
        binding.appVersionNumber.text = getString(R.string.app_version_number)
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

        // Restart the entire app to ensure all activities get the new language
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        // Add extra to indicate we should go to settings
        intent?.putExtra("open_settings", true)

        startActivity(intent)
        finishAffinity()
    }
}
