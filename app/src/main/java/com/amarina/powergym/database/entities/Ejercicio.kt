package com.amarina.powergym.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de la tabla "ejercicios" en la base de datos.
 * Representa un ejercicio físico completo con todas sus características.
 */
@Entity(tableName = "ejercicios")
data class Ejercicio(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,


    val nombre: String,


    val descripcion: String,


    val grupoMuscular: String,


    val dificultad: String,


    val dias: String,


    val imagenEjercicio: String,


    val videoUrl: String = "",


    val seccion: String,


    val calorias: Int = 0,


    val frecuencia: Int,


    val porcentaje: Float
)