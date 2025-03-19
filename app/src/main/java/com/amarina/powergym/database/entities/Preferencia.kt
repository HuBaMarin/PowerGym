package com.amarina.powergym.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "preferencias",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Preferencia(
    @PrimaryKey
    val usuarioId: Int,
    val notificacionesHabilitadas: Boolean = true,
    val temaDark: Boolean = false,
    val recordatorios: Boolean = true,
    val frecuencia: String = "DIARIA" // DIARIA, SEMANAL, NUNCA
)
