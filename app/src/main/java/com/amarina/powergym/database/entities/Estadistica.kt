package com.amarina.powergym.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa las estadísticas de un usuario en la base de datos.
 * Almacena los registros de actividad física y rendimiento del usuario por cada ejercicio.
 */
@Entity(
    tableName = "estadisticas",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"])
    ]
)
data class Estadistica(
    /**
     * Identificador único de la estadística, se genera automáticamente
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /**
     * Identificador del usuario al que pertenece esta estadística
     */
    val userId: Int,

    /**
     * Identificador del ejercicio al que hace referencia
     */
    val ejercicioId: Int,

    /**
     * Fecha de registro de la estadística en milisegundos desde epoch
     */
    val fecha: Long = System.currentTimeMillis(),

    /**
     * Número de ejercicios completados en la sesión
     */
    val ejerciciosCompletados: Int = 1,

    /**
     * Estimación de calorías quemadas durante la sesión
     */
    val caloriasQuemadas: Int = 0,

    /**
     * Tiempo total dedicado al ejercicio en milisegundos
     */
    val tiempoTotal: Long = 0,

    /**
     * Número de repeticiones realizadas
     */
    val repeticiones: Int = 0,

    /**
     * Número de series completadas
     */
    val series: Int = 0,

    /**
     * Nombre del ejercicio (duplicado para acceso rápido sin join)
     */
    val nombreEjercicio: String,

    /**
     * Grupo muscular trabajado (duplicado para acceso rápido sin join)
     */
    val grupoMuscular: String,

    /**
     * URL de la imagen del ejercicio (opcional)
     */
    val imagenUrl: String? = null
)