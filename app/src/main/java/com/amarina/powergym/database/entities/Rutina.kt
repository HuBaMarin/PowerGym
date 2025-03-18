package com.amarin.powergym.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Rutina(@PrimaryKey(autoGenerate = true)
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val objetivo: String,
    val videoUrl: String
)