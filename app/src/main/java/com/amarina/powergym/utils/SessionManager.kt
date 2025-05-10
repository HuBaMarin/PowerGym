package com.amarina.powergym.utils

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.amarina.powergym.database.entities.Usuario
import com.amarina.powergym.utils.crypto.PasswordUtils
import com.amarina.powergym.PowerGymApplication
import kotlinx.coroutines.flow.MutableStateFlow
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Clase que gestiona la sesión del usuario, incluyendo autenticación, almacenamiento
 * seguro de credenciales y control de acceso.
 */
class SessionManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val editor = prefs.edit()

    private val _currentUser = MutableStateFlow<Usuario?>(null)

    // Configuración de encriptación para datos sensibles
    private val keyStore = KeyStore.getInstance("AndroidKeyStore")
    private val transformation = "$ALGORITHM/$BLOCK_MODE/$PADDING"

    init {
        keyStore.load(null)
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generarClaveSecreta()
        }

        val userId = obtenerIdUsuario()
        val email = obtenerEmail()
        val role = obtenerRol()
        val name = obtenerNombre()

        if (userId != -1 && email.isNotEmpty()) {
            _currentUser.value = Usuario(
                id = userId,
                email = email,
                nombre = name,
                password = "",
                rol = role
            )
        }
    }

    /**
     * Guarda la información de sesión del usuario en SharedPreferences de forma segura.
     *
     * @param usuario El objeto Usuario con la información a guardar
     */
    fun guardarSesionUsuario(usuario: Usuario) {
        editor.putInt(KEY_USER_ID, usuario.id)
        editor.putString(KEY_EMAIL, encriptar(usuario.email))
        editor.putString(KEY_ROLE, encriptar(usuario.rol))
        editor.putString(KEY_NAME, encriptar(usuario.nombre))
        editor.putBoolean(KEY_LOGGED_IN, true)
        editor.putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())

        // Copia de seguridad del email para recuperación (solo se guarda email, no otros datos personales)
        backupUserEmail(usuario.email)

        editor.apply()

        _currentUser.value = usuario.copy(password = "")
    }

    /**
     * Cierra la sesión del usuario actual, eliminando todos los datos almacenados.
     */
    fun cerrarSesion() {
        editor.clear()
        editor.apply()
        _currentUser.value = null
    }

    /**
     * Verifica si el usuario está actualmente autenticado y si la sesión es válida
     * según el tiempo transcurrido desde el inicio de sesión.
     *
     * @return true si el usuario está autenticado y la sesión es válida, false en caso contrario
     */
    fun estaAutenticado(): Boolean {
        val loggedIn = prefs.getBoolean(KEY_LOGGED_IN, false)
        if (!loggedIn) return false


        val loginTimestamp = prefs.getLong(KEY_LOGIN_TIMESTAMP, 0)
        val isAdminUser = esAdmin()

        val sessionTimeoutMs = if (isAdminUser) {

            SESSION_TIMEOUT_ADMIN_HOURS * 60 * 60 * 1000L
        } else {

            SESSION_TIMEOUT_USER_DAYS * 24 * 60 * 60 * 1000L
        }

        val sessionValid = (System.currentTimeMillis() - loginTimestamp) < sessionTimeoutMs


        if (!sessionValid) {
            cerrarSesion()
        }

        return sessionValid
    }

    /**
     * Verifica si la cuenta está bloqueada por demasiados intentos fallidos
     * de inicio de sesión.
     *
     * @return true si la cuenta está bloqueada, false si no lo está
     */
    fun estaCuentaBloqueada(): Boolean {
        // Note: This is now delegated to the UserRepository which tracks account locking
        // in the database. We'll keep this method for backwards compatibility but always return false.
        return false
    }

    /**
     * Registra un intento de inicio de sesión, exitoso o no, para controlar
     * el bloqueo por intentos fallidos.
     *
     * @param exitoso true si el intento fue exitoso, false si falló
     */
    fun registrarIntentoSesion(exitoso: Boolean) {
        // Let UserRepository handle account locking in the database
        if (exitoso) {
            // No action needed here as UserRepository handles account locking
        }
    }

    /**
     * Obtiene el ID del usuario actualmente autenticado.
     *
     * @return ID del usuario o -1 si no hay usuario autenticado
     */
    fun obtenerIdUsuario(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    /**
     * Obtiene el email del usuario actualmente autenticado.
     *
     * @return Email del usuario o cadena vacía si no hay usuario autenticado
     */
    fun obtenerEmail(): String {
        val encryptedEmail = prefs.getString(KEY_EMAIL, "") ?: ""
        return if (encryptedEmail.isNotEmpty()) desencriptar(encryptedEmail) else ""
    }

    /**
     * Obtiene el rol del usuario actualmente autenticado.
     *
     * @return Rol del usuario o cadena vacía si no hay usuario autenticado
     */
    fun obtenerRol(): String {
        val encryptedRole = prefs.getString(KEY_ROLE, "") ?: ""
        return if (encryptedRole.isNotEmpty()) desencriptar(encryptedRole) else ""
    }

    /**
     * Obtiene el nombre del usuario actualmente autenticado.
     *
     * @return Nombre del usuario o cadena vacía si no hay usuario autenticado
     */
    fun obtenerNombre(): String {
        val encryptedName = prefs.getString(KEY_NAME, "") ?: ""
        return if (encryptedName.isNotEmpty()) desencriptar(encryptedName) else ""
    }

    /**
     * Verifica si el usuario actual tiene rol de administrador.
     *
     * @return true si el usuario es administrador, false en caso contrario
     */
    fun esAdmin(): Boolean {
        return obtenerRol().equals("admin", ignoreCase = true)
    }

    /**
     * Obtiene el ID del ejercicio seleccionado actualmente.
     *
     * @return ID del ejercicio o -1 si no hay ejercicio seleccionado
     */
    fun obtenerIdEjercicio(): Int {
        return prefs.getInt(KEY_EJERCICIO_ID, -1)
    }

    /**
     * Establece el ID del ejercicio seleccionado actualmente.
     *
     * @param ejercicioId ID del ejercicio a establecer
     */
    fun establecerIdEjercicio(ejercicioId: Int) {
        editor.putInt(KEY_EJERCICIO_ID, ejercicioId)
        editor.apply()
    }

    /**
     * Guarda una lista de IDs de ejercicios seleccionados.
     *
     * @param ejercicios Lista de IDs de ejercicios a guardar
     */
    fun establecerEjerciciosSeleccionados(ejercicios: List<Int>) {
        editor.putString(KEY_SELECTED_EJERCICIOS, ejercicios.joinToString(","))
        editor.apply()
    }

    /**
     * Genera una clave secreta para la encriptación en el almacén de claves Android.
     */
    private fun generarClaveSecreta() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .setUserAuthenticationRequired(false) // Set to false to prevent authentication issues
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    /**
     * Obtiene la clave secreta del almacén de claves Android.
     *
     * @return La clave secreta para operaciones de encriptación/desencriptación
     */
    private fun obtenerClaveSecreta(): SecretKey {
        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    /**
     * Encripta un texto plano utilizando la clave secreta almacenada.
     *
     * @param plainText Texto a encriptar
     * @return Texto encriptado en formato Base64 o el texto original en caso de error
     */
    private fun encriptar(plainText: String): String {
        if (plainText.isEmpty()) return ""

        try {
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, obtenerClaveSecreta())

            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val result = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, result, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, result, iv.size, encryptedBytes.size)

            return Base64.encodeToString(result, Base64.DEFAULT)
        } catch (e: Exception) {
            return plainText
        }
    }

    /**
     * Desencripta un texto previamente encriptado.
     *
     * @param encryptedText Texto encriptado en formato Base64
     * @return Texto desencriptado o cadena vacía en caso de error
     */
    private fun desencriptar(encryptedText: String): String {
        if (encryptedText.isEmpty()) return ""

        try {
            val encryptedData = Base64.decode(encryptedText, Base64.DEFAULT)

            val cipher = Cipher.getInstance(transformation)
            val iv = encryptedData.copyOfRange(0, GCM_IV_LENGTH)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)

            cipher.init(Cipher.DECRYPT_MODE, obtenerClaveSecreta(), spec)

            val encryptedBytes = encryptedData.copyOfRange(GCM_IV_LENGTH, encryptedData.size)
            val decryptedBytes = cipher.doFinal(encryptedBytes)

            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {

            return ""
        }
    }

    /**
     * Verifica las credenciales de administrador contra valores almacenados de forma segura.
     *
     * @param pin Código PIN del administrador (opcional)
     * @param password Contraseña del administrador
     * @return true si las credenciales son correctas, false en caso contrario
     */
    fun verificarCredencialesAdmin(pin: String = "", password: String): Boolean {
        try {
            // Obtener la instancia de PowerGymApplication para acceder a AdminAuthManager
            val app = context.applicationContext as PowerGymApplication

            // Email predeterminado de administrador
            val email = "admin@powergym.com"

            // Usar AdminAuthManager para verificar credenciales
            val isAdminUser = app.adminAuthManager.verifyAdminCredentials(email, password, pin)

            if (isAdminUser) {
                // Si las credenciales son correctas, actualizar el usuario actual como admin
                val currentUser = _currentUser.value
                if (currentUser != null) {
                    // Actualizar el usuario actual con rol de administrador
                    _currentUser.value = currentUser.copy(rol = "admin")

                    // Guardar rol de administrador en preferencias
                    editor.putString(KEY_ROLE, encriptar("admin"))
                    editor.apply()
                }
            }

            return isAdminUser
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al verificar credenciales de administrador", e)
            return false
        }
    }

    /**
     * Genera un hash de contraseña usando PBKDF2 con una sal aleatoria.
     *
     * @param password Contraseña a hashear
     * @return Cadena codificada en Base64 que contiene la sal y el hash
     */
    fun hashPassword(password: String): String {
        return PasswordUtils.hashPassword(password)
    }

    /**
     * Verifica una contraseña contra un hash almacenado usando PBKDF2.
     *
     * @param password Contraseña a verificar
     * @param storedHash Hash almacenado (incluyendo la sal)
     * @return true si la contraseña coincide, false en caso contrario
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        return PasswordUtils.verifyPassword(password, storedHash)
    }

    /**
     * Almacena credenciales de administrador de forma segura.
     *
     * @param pin Código PIN de administrador
     * @param password Contraseña de administrador
     */
    fun almacenarCredencialesAdmin(pin: String, password: String) {
        // Hash de las credenciales antes de almacenarlas
        val hashedPin = hashPassword(pin)
        val hashedPassword = hashPassword(password)

        // Almacenar las credenciales hasheadas de forma segura
        editor.putString("admin_pin_hash", encriptar(hashedPin))
        editor.putString("admin_password_hash", encriptar(hashedPassword))
        editor.apply()
    }

    /**
     * Crea una copia de seguridad del email del usuario para propósitos de recuperación.
     *
     * @param email Email a guardar como copia de seguridad
     */
    private fun backupUserEmail(email: String) {
        try {
            if (email.isNotEmpty()) {
                // Almacenar email en una ubicación de respaldo separada
                val backupEditor = prefs.edit()
                backupEditor.putString("backup_${email.hashCode()}", encriptar(email))
                backupEditor.apply()
            }
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al crear copia de seguridad del email", e)
        }
    }

    /**
     * Obtiene todos los emails de respaldo para recuperación.
     *
     * @return Lista de emails almacenados como copia de seguridad
     */
    fun getBackupEmails(): List<String> {
        val backupEmails = mutableListOf<String>()
        try {
            val allPrefs = prefs.all
            for ((key, value) in allPrefs) {
                if (key.startsWith("backup_") && value is String) {
                    try {
                        val email = desencriptar(value)
                        if (email.isNotEmpty()) {
                            backupEmails.add(email)
                        }
                    } catch (e: Exception) {
                        // Omitir entradas corruptas
                        Log.e("SessionManager", "Entrada de email de respaldo corrupta", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SessionManager", "Error al recuperar emails de respaldo", e)
        }
        return backupEmails
    }

    companion object {
        private const val PREFS_NAME = "power_gym_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EJERCICIO_ID = "ejercicio_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        private const val KEY_NAME = "name"
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_SELECTED_EJERCICIOS = "selected_ejercicios"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"

        // Constantes de encriptación
        private const val KEY_ALIAS = "PowerGymSecretKey"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val KEY_SIZE = 256
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16

        // Tiempo de expiración de sesión segura
        private const val SESSION_TIMEOUT_ADMIN_HOURS =
            2 // Sesiones de admin expiran después de 2 horas
        private const val SESSION_TIMEOUT_USER_DAYS =
            30 // Usuarios regulares expiran después de 30 días
    }
}