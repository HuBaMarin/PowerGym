package com.amarina.powergym.database.dao

import androidx.room.*
import com.amarina.powergym.database.entities.Ejercicio

@Dao
interface EjercicioDao {
    @Insert
    suspend fun insert(ejercicio: Ejercicio): Long

    @Insert
    suspend fun insertAll(ejercicios: List<Ejercicio>)

    @Update
    suspend fun update(ejercicio: Ejercicio)

    @Delete
    suspend fun delete(ejercicio: Ejercicio)

    @Query("SELECT * FROM ejercicios")
    suspend fun getAllEjercicios(): List<Ejercicio>

    @Query("SELECT * FROM ejercicios WHERE grupoMuscular = :grupo")
    suspend fun getEjerciciosByGroup(grupo: String): List<Ejercicio>

    @Query("SELECT DISTINCT seccion FROM ejercicios")
    suspend fun getAllSections(): List<String>

    @Query("SELECT DISTINCT grupoMuscular FROM ejercicios")
    suspend fun getAllMuscleGroups(): List<String>

    @Query("SELECT * FROM ejercicios WHERE id = :id")
    suspend fun getEjercicioById(id: Int): Ejercicio?

    @Query("""
        SELECT * FROM ejercicios 
        WHERE (:dias IS NULL OR dias LIKE '%' || :dias || '%')
        AND (:dificultad IS NULL OR dificultad = :dificultad)
        AND (:grupoMuscular IS NULL OR grupoMuscular = :grupoMuscular)
        AND (nombre LIKE '%' || :query || '%' OR descripcion LIKE '%' || :query || '%')
    """)
    suspend fun getFilteredEjercicios(
        dias: String? = null,
        dificultad: String? = null,
        grupoMuscular: String? = null,
        query: String = ""
    ): List<Ejercicio>
}
