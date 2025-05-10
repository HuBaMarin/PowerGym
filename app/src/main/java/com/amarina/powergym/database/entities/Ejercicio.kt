package com.amarina.powergym.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de la tabla "ejercicios" en la base de datos.
 * Representa un ejercicio físico completo con todas sus características.
 */
@Entity(tableName = "ejercicios")
data class Ejercicio(
    /**
     * Identificador único del ejercicio, se genera automáticamente
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /**
     * Nombre del ejercicio
     */
    val nombre: String,

    /**
     * Descripción detallada del ejercicio y cómo realizarlo
     */
    val descripcion: String,

    /**
     * Grupo muscular principal que trabaja este ejercicio
     */
    val grupoMuscular: String,

    /**
     * Nivel de dificultad del ejercicio (fácil, medio, difícil)
     */
    val dificultad: String,

    /**
     * Días recomendados para realizar el ejercicio (formato separado por comas)
     */
    val dias: String,

    /**
     * Ruta o identificador de la imagen ilustrativa del ejercicio
     */
    val imagenEjercicio: String,

    /**
     * URL del vídeo demostrativo, si existe
     */
    val videoUrl: String = "",

    /**
     * Sección o categoría a la que pertenece el ejercicio
     */
    val seccion: String,

    /**
     * Estimación de calorías quemadas por repetición o sesión
     */
    val calorias: Int = 0,

    /**
     * Frecuencia recomendada del ejercicio (veces por semana)
     */
    val frecuencia: Int,

    /**
     * Porcentaje de efectividad o impacto del ejercicio
     */
    val porcentaje: Float
)