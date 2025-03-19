package com.amarina.powergym.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ejercicios")
data class Ejercicio(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val descripcion: String = "",
    val grupoMuscular: String,
    val dificultad: String,
    val dias: String,
    val urlEjercicio: String,
    val videoUrl: String = "",
    val seccion: String,
    val calorias: Int = 0
)
