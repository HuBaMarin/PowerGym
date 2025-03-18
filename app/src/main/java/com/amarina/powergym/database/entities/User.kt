package com.amarin.powergym.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Usuario(@PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val email : String,
    val password : String,
    val rol : String,
    val notificacionesHabilitadas : Boolean = true
)