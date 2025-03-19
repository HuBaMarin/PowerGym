package com.amarina.powergym.database.dao

import androidx.room.*
import com.amarina.powergym.database.entities.Usuario

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insert(user: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE email = :email AND password = :password")
    suspend fun login(email: String, password: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE email = :email")
    suspend fun getUserByEmail(email: String): Usuario?

    @Update
    suspend fun update(user: Usuario)

    @Delete
    suspend fun delete(user: Usuario)

    @Query("SELECT * FROM usuarios")
    suspend fun getAll(): List<Usuario>

    @Query("SELECT * FROM usuarios WHERE id = :userId")
    suspend fun getUserById(userId: Int): Usuario?
}
