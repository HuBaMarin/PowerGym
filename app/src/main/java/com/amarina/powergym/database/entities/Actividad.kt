package com.amarin.powergym.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Actividad(@PrimaryKey(autoGenerate = true)
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val grupoEdad: String,
    val adquirido: Boolean = false,
    val videoUrl: String?=null,
)