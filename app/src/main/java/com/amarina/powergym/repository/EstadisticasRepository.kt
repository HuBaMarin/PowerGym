package com.amarina.powergym.repository

import com.amarina.powergym.database.dao.EstadisticaDao
import com.amarina.powergym.database.entities.Estadistica
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class EstadisticaRepository(private val estadisticaDao: EstadisticaDao) {

    fun getEstadisticasByUsuario(usuarioId: Int): Flow<List<Estadistica>> {
        return estadisticaDao.getEstadisticasByUsuario(usuarioId)
    }

    suspend fun getEstadisticasByDateRange(
        usuarioId: Int,
        startDate: Long,
        endDate: Long
    ): Result<List<Estadistica>> = withContext(Dispatchers.IO) {
        try {
            val estadisticas = estadisticaDao.getEstadisticasByDateRange(
                usuarioId = usuarioId,
                startDate = startDate,
                endDate = endDate
            )
            Result.success(estadisticas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addEstadistica(estadistica: Estadistica): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = estadisticaDao.insert(estadistica)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEstadistica(estadistica: Estadistica): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            estadisticaDao.update(estadistica)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTotalEjerciciosCompletados(usuarioId: Int): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val total = estadisticaDao.getTotalEjerciciosCompletados(usuarioId) ?: 0
            Result.success(total)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTotalCaloriasQuemadas(usuarioId: Int): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val total = estadisticaDao.getTotalCaloriasQuemadas(usuarioId) ?: 0
            Result.success(total)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTotalTiempoEntrenamiento(usuarioId: Int): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val total = estadisticaDao.getTotalTiempoEntrenamiento(usuarioId) ?: 0L
            Result.success(total)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
