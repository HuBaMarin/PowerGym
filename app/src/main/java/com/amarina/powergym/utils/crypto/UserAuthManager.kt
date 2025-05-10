package com.amarina.powergym.utils.crypto

import android.content.Context
import android.util.Log
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.subtle.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets

/**
 * Handles secure user authentication using Google Tink
 */
class UserAuthManager(private val context: Context) {

    private val aead: Aead

    init {
        // Initialize Tink
        AeadConfig.register()

        // Get or create a keyset for encryption
        val keysetManager = AndroidKeysetManager.Builder()
            .withSharedPref(context, "user_keyset", "user_pref_file")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://user_master_key")
            .build()

        aead = keysetManager.keysetHandle.getPrimitive(Aead::class.java)
    }

    /**
     * Store user credentials securely
     */
    suspend fun storeUserCredentials(email: String, password: String) {
        withContext(Dispatchers.IO) {
            try {
                val encryptedEmail = encryptData(email)
                val encryptedPassword = encryptData(password)

                val prefs = context.getSharedPreferences(USER_PREFS_FILENAME, Context.MODE_PRIVATE)

                val editor = prefs.edit()
                editor.putString(getUserEmailKey(email), encryptedEmail)
                editor.putString(getUserPasswordKey(email), encryptedPassword)
                editor.apply()

                Log.d(TAG, "User credentials stored successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error storing user credentials", e)
            }
        }
    }

    /**
     * Verifies user credentials against securely stored encrypted values
     */
    suspend fun verifyUserCredentials(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences(USER_PREFS_FILENAME, Context.MODE_PRIVATE)

                val encryptedEmail =
                    prefs.getString(getUserEmailKey(email), null) ?: return@withContext false
                val encryptedPassword =
                    prefs.getString(getUserPasswordKey(email), null) ?: return@withContext false

                val storedEmail = decryptData(encryptedEmail)
                val storedPassword = decryptData(encryptedPassword)

                return@withContext email == storedEmail && password == storedPassword
            } catch (e: Exception) {
                Log.e(TAG, "Error verifying user credentials", e)
                return@withContext false
            }
        }
    }

    /**
     * Delete user credentials
     */
    suspend fun deleteUserCredentials(email: String) {
        withContext(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences(USER_PREFS_FILENAME, Context.MODE_PRIVATE)

                val editor = prefs.edit()
                editor.remove(getUserEmailKey(email))
                editor.remove(getUserPasswordKey(email))
                editor.apply()

                Log.d(TAG, "User credentials deleted")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting user credentials", e)
            }
        }
    }

    /**
     * Check if a user with the given email exists
     */
    suspend fun userExists(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences(USER_PREFS_FILENAME, Context.MODE_PRIVATE)
                return@withContext prefs.contains(getUserEmailKey(email))
            } catch (e: Exception) {
                Log.e(TAG, "Error checking if user exists", e)
                return@withContext false
            }
        }
    }

    /**
     * Encrypts data using Tink AEAD
     */
    private fun encryptData(data: String): String {
        val dataBytes = data.toByteArray(StandardCharsets.UTF_8)
        val encryptedBytes = aead.encrypt(dataBytes, null)
        return Base64.encode(encryptedBytes)
    }

    /**
     * Decrypts data using Tink AEAD
     */
    private fun decryptData(encryptedData: String): String {
        val encryptedBytes = Base64.decode(encryptedData)
        val decryptedBytes = aead.decrypt(encryptedBytes, null)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }

    /**
     * Generate a unique key for storing the email based on the email itself
     */
    private fun getUserEmailKey(email: String): String {
        return "user_email_${email.hashCode()}"
    }

    /**
     * Generate a unique key for storing the password based on the email
     */
    private fun getUserPasswordKey(email: String): String {
        return "user_password_${email.hashCode()}"
    }

    companion object {
        private const val TAG = "UserAuthManager"
        private const val USER_PREFS_FILENAME = "user_auth_prefs"
    }
}