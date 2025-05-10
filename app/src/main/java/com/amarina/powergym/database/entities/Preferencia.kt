package com.amarina.powergym.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa las preferencias de un usuario en la base de datos.
 * Almacena todas las configuraciones personalizables para cada usuario.
 */
@Entity(
    tableName = "preferencias",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioId"])
    ]
)
data class Preferencia(
    /**
     * Identificador único de la preferencia, se genera automáticamente
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /**
     * Identificador del usuario al que pertenecen estas preferencias
     */
    val usuarioId: Int,

    /**
     * Indica si el usuario tiene habilitadas las notificaciones generales
     */
    val notificacionesHabilitadas: Boolean = false,

    /**
     * Indica si el usuario prefiere utilizar el tema oscuro en la aplicación
     */
    var temaOscuro: Boolean = true,

    /**
     * Indica si el usuario tiene activados los recordatorios de entrenamiento
     */
    val recordatorios: Boolean = false,

    /**
     * Frecuencia con la que se mostrarán los recordatorios (DIARIA, SEMANAL, etc.)
     */
    val frecuencia: String = "DIARIA"
)