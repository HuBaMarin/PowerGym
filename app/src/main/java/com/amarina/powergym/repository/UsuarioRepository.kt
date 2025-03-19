package com.amarina.powergym.repository

import com.amarina.powergym.database.dao.UsuarioDao
import com.amarina.powergym.database.entities.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UsuarioDao) {

    suspend fun login(email: String, password: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.login(email, password)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, nombre: String = ""): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // Verificar que no exista usuario con ese email
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return@withContext Result.failure(Exception("El email ya está registrado"))
            }

            val userId = userDao.insert(
                Usuario(
                    email = email,
                    password = password,
                    nombre = nombre,
                    rol = "usuario"
                )
            )
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: Int): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getUserById(userId)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: Usuario): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userDao.update(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(user: Usuario): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userDao.delete(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
