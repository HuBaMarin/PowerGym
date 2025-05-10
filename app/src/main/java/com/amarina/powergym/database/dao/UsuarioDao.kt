package com.amarina.powergym.database.dao

import androidx.room.*
import com.amarina.powergym.database.entities.Usuario

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertar(user: Usuario): Long

    @Update
    suspend fun actualizar(user: Usuario)

    @Delete
    suspend fun eliminar(user: Usuario)

    @Query("SELECT * FROM usuarios")
    suspend fun obtenerTodos(): List<Usuario>

    @Query("SELECT * FROM usuarios WHERE id = :userId")
    suspend fun obtenerUsuarioPorId(userId: Int): Usuario?

    @Query("SELECT * FROM usuarios WHERE email = :email")
    suspend fun obtenerUsuarioPorEmail(email: String): Usuario?

    /**
     * Iniciar sesión a un usuario con su correo y contraseña para
     * las pruebas de la aplicación.
     *
     * @param email El correo electrónico del usuario
     * @param password La contraseña del usuario
     * @return El usuario autenticado o null si las credenciales son incorrectas
     */
    @Query("SELECT * FROM usuarios WHERE email = :email AND password = :password")
    suspend fun login(email: String, password: String): Usuario?
}