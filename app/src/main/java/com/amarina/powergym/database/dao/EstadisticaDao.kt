package com.amarina.powergym.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.amarina.powergym.database.entities.Estadistica
import kotlinx.coroutines.flow.Flow

@Dao
interface EstadisticaDao {
    @Insert
    suspend fun insert(estadistica: Estadistica): Long

    @Update
    suspend fun update(estadistica: Estadistica)

    @Query("SELECT * FROM estadisticas WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun getEstadisticasByUsuario(usuarioId: Int): Flow<List<Estadistica>>

    @Query("SELECT * FROM estadisticas WHERE usuarioId = :usuarioId AND fecha BETWEEN :startDate AND :endDate ORDER BY fecha DESC")
    suspend fun getEstadisticasByDateRange(usuarioId: Int, startDate: Long, endDate: Long): List<Estadistica>

    @Query("SELECT SUM(ejerciciosCompletados) FROM estadisticas WHERE usuarioId = :usuarioId")
    suspend fun getTotalEjerciciosCompletados(usuarioId: Int): Int?

    @Query("SELECT SUM(caloriasQuemadas) FROM estadisticas WHERE usuarioId = :usuarioId")
    suspend fun getTotalCaloriasQuemadas(usuarioId: Int): Int?

    @Query("SELECT SUM(tiempoTotal) FROM estadisticas WHERE usuarioId = :usuarioId")
    suspend fun getTotalTiempoEntrenamiento(usuarioId: Int): Long?
}
