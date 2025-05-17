package com.amarina.powergym.repository

import android.content.Context
import android.util.Base64
import com.amarina.powergym.R
import com.amarina.powergym.database.dao.UsuarioDao
import com.amarina.powergym.database.entities.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class UserRepository(private val userDao: UsuarioDao, private val appContext: Context) {

    /**
     * Inicia sesión de un usuario verificando sus credenciales.
     *
     * @param email Correo electrónico del usuario
     * @param password Contraseña del usuario
     * @return Result con el usuario autenticado (sin contraseña) o un error
     */
    suspend fun iniciarSesion(email: String, password: String): Result<Usuario> =
        withContext(Dispatchers.IO) {
            try {
                // Obtener el usuario a través del correo electrónico
                val user = userDao.obtenerUsuarioPorEmail(email)

                // Verificar hash de contraseña
                if (user != null && verifyPassword(password, user.password)) {
                    // Ver si es un administrador
                    if (user.rol.equals(
                            "admin",
                            ignoreCase = true
                        ) && !user.oneTimeToken.isNullOrEmpty()
                    ) {
                        // Limpiar el token
                        val updatedUser = user.copy(oneTimeToken = null)
                        userDao.actualizar(updatedUser)
                        return@withContext Result.success(updatedUser.copy(password = ""))
                    }

                    // Verificar si la cuenta está bloqueada
                    if (user.accountLocked) {
                        return@withContext Result.failure(Exception("La cuenta está bloqueada"))
                    }

                    // Reset failed login attempts on successful login
                    if (user.failedLoginAttempts > 0) {
                        val resetUser = user.copy(failedLoginAttempts = 0)
                        userDao.actualizar(resetUser)
                    }

                    // Devolver al usuario
                    val secureUser = user.copy(password = "")
                    Result.success(secureUser)
                } else {
                    // Incrementar el número de intentos fallidos si el usuario existe
                    user?.let {
                        val updatedFailCount = it.failedLoginAttempts + 1
                        val updatedUser = it.copy(failedLoginAttempts = updatedFailCount)
                        userDao.actualizar(updatedUser)

                        // Bloquear la cuenta si se supera el límite de intentos
                        if (updatedFailCount >= 5) {
                            val lockedUser = updatedUser.copy(accountLocked = true)
                            userDao.actualizar(lockedUser)
                            return@withContext Result.failure(Exception("La cuenta está bloqueada debido a demasiados intentos fallidos"))
                        }
                    }

                    Result.failure(Exception(appContext.getString(R.string.error_login)))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param email Correo electrónico del nuevo usuario
     * @param password Contraseña del nuevo usuario
     * @param nombre Nombre del usuario (opcional)
     * @return Result con el ID del usuario creado o un error
     */
    suspend fun registrar(email: String, password: String, nombre: String = ""): Result<Long> =
        withContext(Dispatchers.IO) {
            try {
                // Comprobar si el usuario existe
                val existingUser = userDao.obtenerUsuarioPorEmail(email)
                if (existingUser != null) {
                    return@withContext Result.failure(Exception("El email ya está registrado"))
                }

                // Hash de contraseña
                val hashedPassword = hashPassword(password)

                val userId = userDao.insertar(
                    Usuario(
                        email = email,
                        password = hashedPassword,
                        nombre = nombre,
                        rol = "usuario",
                        accountLocked = false,
                        failedLoginAttempts = 0
                    )
                )
                Result.success(userId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }


    /**
     * Obtiene un usuario por su ID.
     *
     * @param userId ID del usuario a buscar
     * @return Result con el usuario (sin contraseña) o un error
     */
    suspend fun obtenerUsuarioPorId(userId: Int): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.obtenerUsuarioPorId(userId)
            if (user != null) {
                // Devolver usuario sin contraseña por seguridad
                val secureUser = user.copy(password = "")
                Result.success(secureUser)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza la información de un usuario existente.
     *
     * @param user Usuario con los datos actualizados
     * @return Result con éxito o un error
     */
    suspend fun actualizarUsuario(user: Usuario): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Primero obtener el usuario existente
            val usuarioExistente = userDao.obtenerUsuarioPorId(user.id)
                ?: return@withContext Result.failure(Exception("Usuario no encontrado"))

            // Actualizar usuario con hash de contraseña si la contraseña fue cambiada
            val updatedUser =
                if (user.password.isNotEmpty() && user.password != usuarioExistente.password) {
                    // Reset failed attempts and unlock account on password change
                    user.copy(
                        password = hashPassword(user.password),
                        failedLoginAttempts = 0,
                        accountLocked = false
                    )
                } else {
                    // Mantener el hash de contraseña existente
                    user.copy(password = usuarioExistente.password)
                }

            userDao.actualizar(updatedUser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un usuario del sistema.
     *
     * @param user Usuario a eliminar
     * @return Result con éxito o un error
     */
    suspend fun eliminarUsuario(user: Usuario): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userDao.eliminar(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Genera un hash seguro para una contraseña utilizando PBKDF2.
     *
     * @param password Contraseña a hashear
     * @return String con el hash en formato "iteraciones:sal:hash"
     */
    private fun hashPassword(password: String): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)

        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            KEY_LENGTH
        )

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded

        // Formato: iteraciones:sal:hash
        return "$PBKDF2_ITERATIONS:${
            Base64.encodeToString(
                salt,
                Base64.NO_WRAP
            )
        }:${Base64.encodeToString(hash, Base64.NO_WRAP)}"
    }

    /**
     * Verifica una contraseña contra un hash almacenado.
     *
     * @param password Contraseña a verificar
     * @param storedHash Hash almacenado para comparación
     * @return true si la contraseña coincide, false en caso contrario
     */
    private fun verifyPassword(password: String, storedHash: String): Boolean {
        try {
            // Formato: iteraciones:sal:hash
            val parts = storedHash.split(":")
            if (parts.size != 3) {
                // Para inicialización de base de datos - comprobación directa de contraseña
                return password == storedHash
            }

            val iterations = parts[0].toInt()
            val salt = Base64.decode(parts[1], Base64.NO_WRAP)
            val hash = Base64.decode(parts[2], Base64.NO_WRAP)

            val spec = PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                hash.size * 8
            )

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val testHash = factory.generateSecret(spec).encoded

            // Comparar hashes en tiempo constante para prevenir ataques de temporización
            return MessageDigest.isEqual(hash, testHash)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    companion object {
        // Constantes de hash de contraseña
        private const val PBKDF2_ITERATIONS = 10000
        private const val KEY_LENGTH = 256
    }
}