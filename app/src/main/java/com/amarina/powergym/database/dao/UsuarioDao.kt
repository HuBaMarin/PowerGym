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


    @Query("SELECT * FROM usuarios WHERE email = :email AND password = :password")
    suspend fun iniciarSesion(email: String, password: String): Usuario?
}