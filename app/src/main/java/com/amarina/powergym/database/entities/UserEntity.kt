package com.amarin.powergym.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val password: String,
    val role: String = "user"  // 'user' or 'admin'
)