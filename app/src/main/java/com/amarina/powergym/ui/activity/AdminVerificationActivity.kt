package com.amarina.powergym.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.amarina.powergym.R
import com.amarina.powergym.databinding.ActivityAdminVerificationBinding
import com.amarina.powergym.utils.PreferenceManager
import com.amarina.powergym.utils.crypto.AdminAuthManager
import kotlinx.coroutines.launch

class AdminVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminVerificationBinding
    private lateinit var adminAuthManager: AdminAuthManager
    private lateinit var preferenceManager: PreferenceManager

    private var failedAttempts = 0
    private var lockoutTimer: CountDownTimer? = null
    private val maxFailedAttempts = 3
    private val lockoutTimeMs = 30000L // 30 seconds

    // Store email/password from login attempt
    private var adminEmail: String? = null
    private var adminPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.let {
            adminEmail = it.getStringExtra("email")
            adminPassword = it.getStringExtra("password")
        }

        initManagers()
        setupListeners()

        // Hide password input field if already provided
        if (!adminPassword.isNullOrEmpty()) {
            binding.txtPasswordLabel.visibility = View.GONE
            binding.edtAdminPassword.visibility = View.GONE
            binding.txtDescription.text = getString(R.string.admin_pin_only)
        }
    }

    private fun initManagers() {
        adminAuthManager = AdminAuthManager(this)
        preferenceManager = PreferenceManager(this)

        lifecycleScope.launch {
            adminAuthManager.initializeAdminCredentials()
            checkFirstTimeCredentials()
        }

        // Get previous failed attempts if any
        failedAttempts = preferenceManager.getInt("admin_failed_attempts", 0)
        checkLockoutStatus()
    }

    private fun setupListeners() {
        // Set up cancel button click listener
        binding.btnCancel.setOnClickListener {
            finish() // Close this activity and return to previous one
        }

        // Set up verify button click listener
        binding.btnVerifyWithPin.setOnClickListener {
            verifyCredentials()
        }
    }

    /**
     * Check for first-time credentials and show them to the admin if available
     */
    private fun checkFirstTimeCredentials() {
        val prefs = getSharedPreferences(AdminAuthManager.ADMIN_PREFS_FILENAME, MODE_PRIVATE)
        val isInitialized = prefs.getBoolean(AdminAuthManager.KEY_ADMIN_INITIALIZED, false)


        // For first run, we'll need to tell the user the generated credentials
        if (isInitialized) {
            val firstRunPrefs = getSharedPreferences("first_run_credentials", MODE_PRIVATE)
            val tempPassword = firstRunPrefs.getString("temp_password", null)
            val tempPin = firstRunPrefs.getString("temp_pin", null)

            if (tempPassword != null && tempPin != null) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.admin_verification)
                    .setMessage(
                        getString(
                            R.string.admin_first_time_credentials,
                            adminAuthManager.getDecryptedEmail(), tempPassword, tempPin
                        )
                    )
                    .setPositiveButton(R.string.accept) { _, _ ->
                        // Despues de aceptar limpiar credenciales
                        firstRunPrefs.edit().apply {
                            clear()
                            apply()
                        }


                    }
                    .setCancelable(false)
                    .show()
            } else {

                Toast.makeText(this, R.string.admin_verify_credentials, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyCredentials() {
        // Si hay muchos errores de autenticación
        if (isLockedOut()) {
            Toast.makeText(this, R.string.access_denied, Toast.LENGTH_SHORT).show()
            return
        }

        val password = adminPassword ?: binding.edtAdminPassword.text.toString().trim()
        val pin = binding.edtAdminPin.text.toString().trim()

        if (password.isEmpty() || pin.isEmpty()) {
            Toast.makeText(this, R.string.enter_all_credentials, Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences(AdminAuthManager.ADMIN_PREFS_FILENAME, MODE_PRIVATE)
        val email =
            adminEmail ?: prefs.getString(AdminAuthManager.KEY_ADMIN_EMAIL, "admin@powergym.com")
            ?: "admin@powergym.com"

        // Debug log to help troubleshoot (will be removed in production)
        Log.d("AdminVerification", "Verifying - Email: $email, PIN length: ${pin.length}")

        val isValid = adminAuthManager.verifyAdminCredentials(email, password, pin)

        if (isValid) {
            // Reset failed attempts on success
            failedAttempts = 0
            preferenceManager.putInt("admin_failed_attempts", 0)

            // Navigate to admin exercise activity
            val intent = Intent(this, AdminExerciseActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            handleFailedAttempt()
        }
    }

    private fun handleFailedAttempt() {
        failedAttempts++
        preferenceManager.putInt("admin_failed_attempts", failedAttempts)

        if (failedAttempts >= maxFailedAttempts) {
            // Start lockout
            preferenceManager.putLong(
                "admin_lockout_time",
                System.currentTimeMillis() + lockoutTimeMs
            )
            startLockoutTimer(lockoutTimeMs)
            Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show()
        } else {
            val remainingAttempts = maxFailedAttempts - failedAttempts
            Toast.makeText(
                this,
                getString(R.string.wrong_credentials, remainingAttempts),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isLockedOut(): Boolean {
        val lockoutEndTime = preferenceManager.getLong("admin_lockout_time", 0)
        val currentTime = System.currentTimeMillis()

        if (lockoutEndTime > currentTime) {
            // Still locked out
            startLockoutTimer(lockoutEndTime - currentTime)
            return true
        }

        return false
    }

    private fun checkLockoutStatus() {
        if (isLockedOut()) {
            binding.btnVerifyWithPin.isEnabled = false
            binding.edtAdminPassword.isEnabled = false
            binding.edtAdminPin.isEnabled = false
        }
    }

    private fun startLockoutTimer(timeLeftMs: Long) {
        lockoutTimer?.cancel()

        binding.btnVerifyWithPin.isEnabled = false
        binding.edtAdminPassword.isEnabled = false
        binding.edtAdminPin.isEnabled = false
        binding.txtLockoutTimer.visibility = View.VISIBLE

        lockoutTimer = object : CountDownTimer(timeLeftMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.txtLockoutTimer.text = getString(R.string.time_remaining, secondsLeft)
            }

            override fun onFinish() {
                binding.btnVerifyWithPin.isEnabled = true
                binding.edtAdminPassword.isEnabled = true
                binding.edtAdminPin.isEnabled = true
                binding.txtLockoutTimer.visibility = View.GONE
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        lockoutTimer?.cancel()
    }
}