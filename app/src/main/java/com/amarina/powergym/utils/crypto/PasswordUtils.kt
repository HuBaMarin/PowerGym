package com.amarina.powergym.utils.crypto

import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.experimental.xor

/**
 * Utility class for securely handling passwords using PBKDF2 with HMAC-SHA256
 */
object PasswordUtils {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 310000 // High number of iterations for security
    private const val KEY_LENGTH = 256 // Output key length in bits
    private const val SALT_LENGTH = 32 // Salt length in bytes
    private const val TAG = "PasswordUtils"

    /**
     * Hashes a password with PBKDF2 using a random salt
     *
     * @param password The password to hash
     * @return Base64-encoded string with salt and hash information
     */
    fun hashPassword(password: String): String {
        try {
            // Generate a random salt
            val random = SecureRandom()
            val salt = ByteArray(SALT_LENGTH)
            random.nextBytes(salt)

            // Hash the password
            val hash = hashWithSalt(password.toCharArray(), salt)

            // Combine salt and hash
            val result = ByteArray(salt.size + hash.size)
            System.arraycopy(salt, 0, result, 0, salt.size)
            System.arraycopy(hash, 0, result, salt.size, hash.size)

            // Convert to Base64 for storage
            return Base64.encodeToString(result, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error hashing password", e)
            throw RuntimeException("Error hashing password", e)
        }
    }

    /**
     * Verifies a password against a stored hash
     *
     * @param password The password to verify
     * @param storedHash The stored hash string (includes salt)
     * @return true if password matches, false otherwise
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        try {
            // Decode from Base64
            val decoded = Base64.decode(storedHash, Base64.NO_WRAP)

            // Extract salt
            val salt = decoded.copyOfRange(0, SALT_LENGTH)

            // Hash the input password with the same salt
            val computedHash = hashWithSalt(password.toCharArray(), salt)

            // Extract the stored hash part
            val storedHashPart = decoded.copyOfRange(SALT_LENGTH, decoded.size)

            // Compare hashes in constant time
            return constantTimeEquals(computedHash, storedHashPart)
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying password", e)
            return false
        }
    }

    /**
     * Performs PBKDF2 hashing with the given salt
     */
    private fun hashWithSalt(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        try {
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            return factory.generateSecret(spec).encoded
        } catch (e: Exception) {
            // Fallback to SHA-256 if PBKDF2 is unavailable on this device
            Log.w(TAG, "PBKDF2 unavailable, falling back to SHA-256")
            return fallbackHash(password, salt)
        } finally {
            spec.clearPassword()
        }
    }

    /**
     * Fallback hashing method using SHA-256 with multiple iterations
     * Only used if PBKDF2 is unavailable
     */
    private fun fallbackHash(password: CharArray, salt: ByteArray): ByteArray {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            // Add salt
            digest.update(salt)

            // First hash
            var result = digest.digest(String(password).toByteArray())

            // Multiple iterations to slow down brute force
            for (i in 1 until ITERATIONS / 1000) { // Fewer iterations for performance
                digest.reset()
                result = digest.digest(result)
            }

            return result
        } catch (e: Exception) {
            Log.e(TAG, "Error in fallback hash", e)
            throw RuntimeException("Security error", e)
        }
    }

    /**
     * Compares two byte arrays in constant time to prevent timing attacks
     */
    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) {
            return false
        }

        var result = 0
        for (i in a.indices) {
            result = result or (a[i] xor b[i]).toInt()
        }

        return result == 0
    }
}