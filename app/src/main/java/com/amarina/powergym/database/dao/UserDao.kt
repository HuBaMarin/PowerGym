package com.amarin.powergym.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.amarin.powergym.database.entities.Usuario

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: Usuario): Long

    @Query("SELECT * FROM Usuario WHERE email = :email AND password = :password")
    suspend fun loginUser(email: String, password: String): Usuario?

    @Query("SELECT * FROM Usuario")
    suspend fun getAllUsers(): List<Usuario>

    @Update
    suspend fun updateUser(user: Usuario)

    @Delete
    suspend fun deleteUser(user: Usuario)
}