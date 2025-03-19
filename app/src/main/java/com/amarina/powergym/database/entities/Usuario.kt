package com.amarina.powergym.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val email: String,
    val password: String,
    val nombre: String = "",
    val rol: String = "usuario",
    val notificacionesHabilitadas: Boolean = true,
    val fechaRegistro: Long = System.currentTimeMillis()
)
