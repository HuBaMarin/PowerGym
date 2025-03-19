package com.amarina.powergym.repository

import com.amarina.powergym.database.dao.PreferenciaDao
import com.amarina.powergym.database.entities.Preferencia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PreferenciaRepository(private val preferenciaDao: PreferenciaDao) {

    fun getPreferenciasByUsuario(usuarioId: Int): Flow<Preferencia?> {
        return preferenciaDao.getPreferenciasByUsuario(usuarioId)
    }

    suspend fun getPreferenciasSync(usuarioId: Int): Result<Preferencia?> = withContext(Dispatchers.IO) {
        try {
            val preferencias = preferenciaDao.getPreferenciasSync(usuarioId)
            Result.success(preferencias)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun savePreferencias(preferencia: Preferencia): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = preferenciaDao.insert(preferencia)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePreferencias(preferencia: Preferencia): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            preferenciaDao.update(preferencia)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
