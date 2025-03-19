package com.amarina.powergym.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "estadisticas",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Estadistica(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val fecha: Long = System.currentTimeMillis(),
    val ejerciciosCompletados: Int = 0,
    val tiempoTotal: Long = 0, // en segundos
    val caloriasQuemadas: Int = 0
)
