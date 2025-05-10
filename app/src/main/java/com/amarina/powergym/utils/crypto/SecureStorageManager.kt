package com.amarina.powergym.utils.crypto

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException

/**
 * SecureStorageManager using Android Jetpack Security's EncryptedSharedPreferences
 * for securely storing sensitive user data
 */
class SecureStorageManager(private val context: Context) {

    private var securePrefs: SharedPreferences? = null
    private var aead: Aead? = null

    init {
        try {
            // Initialize Tink
            AeadConfig.register()

            // Initialize secure preferences
            securePrefs = context.getSharedPreferences(ENCRYPTED_PREFS_NAME, Context.MODE_PRIVATE)

            // Initialize encryption
            aead = initializeAead()
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, "Error creating secure storage", e)
            // Fall back to regular shared preferences if absolutely necessary
            securePrefs = context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private fun initializeAead(): Aead {
        // Create or retrieve a keyset for encryption/decryption
        val keysetManager = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, KEYSET_PREF_NAME)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://" + MASTER_KEY_ALIAS)
            .build()

        return keysetManager.keysetHandle.getPrimitive(Aead::class.java)
    }

    private fun encrypt(value: String): String {
        return try {
            if (aead == null) return value
            val encryptedBytes = aead!!.encrypt(
                value.toByteArray(StandardCharsets.UTF_8),
                null // Associated data, not needed here
            )
            android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT)
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, "Encryption failed", e)
            value // Return unencrypted value as fallback
        }
    }

    private fun decrypt(encryptedValue: String): String {
        return try {
            if (aead == null) return encryptedValue
            val encryptedBytes =
                android.util.Base64.decode(encryptedValue, android.util.Base64.DEFAULT)
            val decryptedBytes = aead!!.decrypt(encryptedBytes, null)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, "Decryption failed", e)
            encryptedValue // Return encrypted value as fallback
        }
    }

    // String operations
    fun putString(key: String, value: String) {
        securePrefs?.edit()?.putString(key, encrypt(value))?.apply()
    }
    
    fun getString(key: String, default: String = ""): String {
        val encryptedValue = securePrefs?.getString(key, null) ?: return default
        return decrypt(encryptedValue)
    }
    
    // Boolean operations
    fun putBoolean(key: String, value: Boolean) {
        securePrefs?.edit()?.putString(key, encrypt(value.toString()))?.apply()
    }
    
    fun getBoolean(key: String, default: Boolean = false): Boolean {
        val encryptedValue = securePrefs?.getString(key, null) ?: return default
        return decrypt(encryptedValue).toBoolean()
    }
    
    // Int operations
    fun putInt(key: String, value: Int) {
        securePrefs?.edit()?.putString(key, encrypt(value.toString()))?.apply()
    }
    
    fun getInt(key: String, default: Int = 0): Int {
        val encryptedValue = securePrefs?.getString(key, null) ?: return default
        return decrypt(encryptedValue).toIntOrNull() ?: default
    }
    
    // Long operations
    fun putLong(key: String, value: Long) {
        securePrefs?.edit()?.putString(key, encrypt(value.toString()))?.apply()
    }
    
    fun getLong(key: String, default: Long = 0L): Long {
        val encryptedValue = securePrefs?.getString(key, null) ?: return default
        return decrypt(encryptedValue).toLongOrNull() ?: default
    }
    
    fun contains(key: String): Boolean {
        return securePrefs?.contains(key) ?: false
    }
    
    fun remove(key: String) {
        securePrefs?.edit()?.remove(key)?.apply()
    }
    
    fun clear() {
        securePrefs?.edit()?.clear()?.apply()
    }
    
    companion object {
        private const val TAG = "SecureStorageManager"
        private const val MASTER_KEY_ALIAS = "powergym_master_key"
        private const val ENCRYPTED_PREFS_NAME = "powergym_secure_prefs"
        private const val FALLBACK_PREFS_NAME = "powergym_regular_prefs"
        private const val KEYSET_NAME = "powergym_tink_keyset"
        private const val KEYSET_PREF_NAME = "powergym_tink_pref"
        
        // Define standard keys
        const val KEY_USER_ID = "user_id"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE = "role"
        const val KEY_NAME = "name"
        const val KEY_LOGGED_IN = "logged_in"
        const val KEY_ADMIN_EMAIL = "admin_email"
        const val KEY_ADMIN_PASSWORD_HASH = "admin_password_hash"
        const val KEY_ADMIN_PIN_HASH = "admin_pin_hash"
        const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
    }
}