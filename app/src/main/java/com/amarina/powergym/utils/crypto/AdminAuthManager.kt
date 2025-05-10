package com.amarina.powergym.utils.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

/**
 * Handles secure admin authentication using Argon2id hashes and Android KeyStore for encryption
 */
class AdminAuthManager(private val context: Context) {

    companion object {
        private const val TAG = "AdminAuthManager"
        const val ADMIN_PREFS_FILENAME = "admin_auth_prefs"
        const val KEY_ADMIN_EMAIL = "admin_email"
        const val KEY_ADMIN_PASSWORD_HASH = "admin_password_hash"
        const val KEY_ADMIN_PIN_HASH = "admin_pin_hash"
        const val KEY_ADMIN_INITIALIZED = "admin_initialized"
        private const val KEYSTORE_ALIAS = "powergym_admin_key"

        private const val DEFAULT_ADMIN_EMAIL_DOMAIN = "@powergym.com"
    }

    // Initialize encryption key
    private fun getOrCreateEncryptionKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).apply {
                setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                setRandomizedEncryptionRequired(true)
                setUserAuthenticationRequired(false)
            }.build()

            keyGenerator.init(keyGenParameterSpec)
            return keyGenerator.generateKey()
        } else {
            return (keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        }
    }

    // Encrypt data using Android KeyStore
    private fun encrypt(data: String): String {
        val secretKey = getOrCreateEncryptionKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))

        val outputStream = ByteArrayOutputStream()
        outputStream.write(iv.size)  // Write IV size as first byte
        outputStream.write(iv)
        outputStream.write(encryptedBytes)

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    // Decrypt data using Android KeyStore
    private fun decrypt(encryptedData: String): String {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKey =
            (keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
                ?: throw Exception("Encryption key not found")

        val encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
        val inputStream = ByteArrayInputStream(encryptedBytes)

        val ivSize = inputStream.read()  // Read IV size from first byte
        val iv = ByteArray(ivSize)
        inputStream.read(iv)

        val cipherBytes = ByteArray(encryptedBytes.size - ivSize - 1)
        inputStream.read(cipherBytes)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))

        return String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8)
    }

    private fun generateHash(input: String): String {
        val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val parameters = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withSalt(salt)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(3)
            .withMemoryAsKB(65536)
            .withParallelism(4)
            .build()
        val generator = Argon2BytesGenerator()
        generator.init(parameters)
        val hash = ByteArray(32)
        generator.generateBytes(input.toByteArray(StandardCharsets.UTF_8), hash)
        return Base64.encodeToString(
            parameters.salt,
            Base64.DEFAULT
        ) + ":" + Base64.encodeToString(hash, Base64.DEFAULT)
    }

    private fun verifyHash(input: String, storedValue: String): Boolean {
        val parts = storedValue.split(":")
        if (parts.size != 2) return false

        val salt = Base64.decode(parts[0], Base64.DEFAULT)
        val storedHash = Base64.decode(parts[1], Base64.DEFAULT)

        val parameters = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(3)
            .withMemoryAsKB(65536)
            .withParallelism(4)
            .withSalt(salt)
            .build()

        val generator = Argon2BytesGenerator()
        generator.init(parameters)
        val testHash = ByteArray(32)
        generator.generateBytes(input.toByteArray(StandardCharsets.UTF_8), testHash)

        // Fix for PIN comparison: Ensure byte arrays are the same length before comparing
        if (testHash.size != storedHash.size) {
            return false
        }

        return constantTimeCompare(testHash, storedHash)
    }

    private fun constantTimeCompare(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false

        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].toInt() xor b[i].toInt())
        }
        return diff == 0
    }

    /**
     * Generates a random secure email for admin
     */
    private fun generateRandomEmail(): String {
        val usernameChars = "abcdefghijklmnopqrstuvwxyz0123456789"
        val random = SecureRandom()
        val usernameLength = random.nextInt(5) + 5 // 5-9 chars

        val username = StringBuilder()
        for (i in 0 until usernameLength) {
            username.append(usernameChars[random.nextInt(usernameChars.length)])
        }

        return "admin.${username}$DEFAULT_ADMIN_EMAIL_DOMAIN"
    }

    /**
     * Generates a random secure password
     */
    private fun generateRandomPassword(length: Int): String {
        val passwordChars =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:,.<>?"
        val random = SecureRandom()

        val password = StringBuilder()
        for (i in 0 until length) {
            password.append(passwordChars[random.nextInt(passwordChars.length)])
        }

        return password.toString()
    }

    /**
     * Generates a random secure PIN
     */
    private fun generateRandomPin(length: Int): String {
        val pinChars = "0123456789"
        val random = SecureRandom()

        val pin = StringBuilder()
        for (i in 0 until length) {
            pin.append(pinChars[random.nextInt(pinChars.length)])
        }

        return pin.toString()
    }

    /**
     * Initializes the admin credentials securely if they don't exist
     */
    suspend fun initializeAdminCredentials() {
        withContext(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences(
                    ADMIN_PREFS_FILENAME,
                    Context.MODE_PRIVATE
                )

                if (!prefs.contains(KEY_ADMIN_INITIALIZED)) {
                    Log.d(TAG, "Initializing admin credentials")

                    // Generar valores aleatorios
                    val adminEmail = generateRandomEmail()
                    val adminPassword = generateRandomPassword(12)
                    val adminPin = generateRandomPin(6)

                    val passwordHash = generateHash(adminPassword)
                    val pinHash = generateHash(adminPin)

                    // Guardarlos encriptados
                    val encryptedEmail = encrypt(adminEmail)
                    val encryptedPasswordHash = encrypt(passwordHash)
                    val encryptedPinHash = encrypt(pinHash)

                    prefs.edit().apply {
                        putString(KEY_ADMIN_EMAIL, encryptedEmail)
                        putString(KEY_ADMIN_PASSWORD_HASH, encryptedPasswordHash)
                        putString(KEY_ADMIN_PIN_HASH, encryptedPinHash)
                        putBoolean(KEY_ADMIN_INITIALIZED, true)
                        apply()
                    }

                    // Guardarlos temporalmente (un solo uso)
                    val firstRunPrefs =
                        context.getSharedPreferences("first_run_credentials", Context.MODE_PRIVATE)
                    firstRunPrefs.edit().apply {
                        putString("temp_password", adminPassword)
                        putString("temp_pin", adminPin)
                        apply()
                    }


                } else {
                   //no mostrar nada
                }
            } catch (e: Exception) {
                //no mostrar nada
            }
        }
    }

    /**
     * Verifies admin credentials with PIN against securely stored encrypted hashed values
     */
    fun verifyAdminCredentials(email: String, password: String, pin: String): Boolean {
        try {
            val prefs = context.getSharedPreferences(
                ADMIN_PREFS_FILENAME,
                Context.MODE_PRIVATE
            )

            // Recoger esos valores encryptados
            val encryptedEmail = prefs.getString(KEY_ADMIN_EMAIL, null) ?: return false
            val encryptedPasswordHash =
                prefs.getString(KEY_ADMIN_PASSWORD_HASH, null) ?: return false
            val encryptedPinHash = prefs.getString(KEY_ADMIN_PIN_HASH, null) ?: return false

            // Desencriptarlos
            val storedEmail = decrypt(encryptedEmail)
            val storedPasswordHash = decrypt(encryptedPasswordHash)
            val storedPinHash = decrypt(encryptedPinHash)

            // Verificación
            val result = email == storedEmail &&
                    verifyHash(password, storedPasswordHash) &&
                    verifyHash(pin, storedPinHash)

            return result
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Gets the admin email in decrypted form - only used for first-time initialization
     */
    fun getDecryptedEmail(): String {
        try {
            val prefs = context.getSharedPreferences(
                ADMIN_PREFS_FILENAME,
                Context.MODE_PRIVATE
            )

            val encryptedEmail = prefs.getString(KEY_ADMIN_EMAIL, null)
            return if (encryptedEmail != null) {
                decrypt(encryptedEmail)
            } else {
                "admin@powergym.com" // Default fallback
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting decrypted admin email", e)
            return "admin@powergym.com" // Default fallback
        }
    }
}