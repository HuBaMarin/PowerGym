package com.amarina.powergym.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import com.amarina.powergym.database.entities.Ejercicio
import com.amarina.powergym.database.entities.Estadistica
import kotlinx.coroutines.flow.Flow

@Dao
interface EstadisticaDao {


    /**
     * Obtiene todas las estadísticas de un usuario.
     * Incluye información adicional de los ejercicios relacionados.
     *
     * @param userId ID del usuario
     * @return Flujo de lista completa de estadísticas ordenadas por fecha descendente
     */
    @Transaction
    @Query("""
    SELECT e.*, ej.nombre as nombreEjercicio, ej.grupoMuscular, ej.imagenEjercicio as imagenUrl
    FROM estadisticas e
    JOIN ejercicios ej ON e.ejercicioId = ej.id
    WHERE e.userId = :userId
    ORDER BY e.fecha DESC
""")
    fun obtenerTodasEstadisticasDeUsuario(userId: Int): Flow<List<Estadistica>>

    /**
     * Obtiene las estadísticas de un usuario para un ejercicio específico.
     *
     * @param userId ID del usuario
     * @param ejercicioId ID del ejercicio
     * @return Flujo de lista de estadísticas del ejercicio ordenadas por fecha descendente
     */
    @Query("SELECT * FROM estadisticas WHERE userId = :userId AND ejercicioId = :ejercicioId ORDER BY fecha DESC")
    fun obtenerEstadisticasPorEjercicio(userId: Int, ejercicioId: Int): Flow<List<Estadistica>>

    /**
     * Inserta una nueva estadística en la base de datos.
     *
     * @param estadistica La estadística a insertar
     * @return El ID generado para la nueva estadística
     */
    @Insert
    suspend fun insertarEstadistica(estadistica: Estadistica): Long

    /**
     * Obtiene una estadística específica según su ID.
     *
     * @param id El ID de la estadística a buscar
     * @return La estadística encontrada o null si no existe
     */
    @Query("SELECT * FROM estadisticas WHERE id = :id")
    suspend fun obtenerEstadisticaPorId(id: Int): Estadistica?

    /**
     * Obtiene los 5 ejercicios más frecuentemente realizados por un usuario.
     *
     * @param userId ID del usuario
     * @return Flujo de lista de ejercicios ordenados por frecuencia descendente
     */
    @Query("""
        SELECT 
            ej.id,
            ej.nombre,
            ej.descripcion,
            ej.grupoMuscular,
            ej.dificultad,
            ej.dias,
            ej.imagenEjercicio,
            ej.videoUrl,
            ej.seccion,
            ej.calorias,
            COUNT(e.ejercicioId) AS frecuencia,
            0.0 AS porcentaje
        FROM 
            estadisticas e
        JOIN 
            ejercicios ej ON e.ejercicioId = ej.id
        WHERE e.userId = :userId
        GROUP BY e.ejercicioId
        ORDER BY frecuencia DESC
        LIMIT 5
    """)
    fun obtenerEjerciciosMasFrecuentes(userId: Int): Flow<List<Ejercicio>>



    /**
     * Actualiza el nombre del ejercicio en todas las estadísticas relacionadas.
     *
     * @param ejercicioId ID del ejercicio cuyo nombre se actualizará
     * @param nombreEjercicio Nuevo nombre del ejercicio
     */
    @Query("UPDATE estadisticas SET nombreEjercicio = :nombreEjercicio WHERE ejercicioId = :ejercicioId")
    suspend fun actualizarNombreEjercicio(ejercicioId: Int, nombreEjercicio: String)

    /**
     * Actualiza el grupo muscular en todas las estadísticas relacionadas.
     *
     * @param ejercicioId ID del ejercicio cuyo grupo muscular se actualizará
     * @param grupoMuscular Nuevo grupo muscular del ejercicio
     */
    @Query("UPDATE estadisticas SET grupoMuscular = :grupoMuscular WHERE ejercicioId = :ejercicioId")
    suspend fun actualizarGrupoMuscular(ejercicioId: Int, grupoMuscular: String)
}