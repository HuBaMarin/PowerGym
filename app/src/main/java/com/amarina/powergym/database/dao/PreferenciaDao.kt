package com.amarina.powergym.database.dao

import androidx.room.*
import com.amarina.powergym.database.entities.Preferencia
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenciaDao {
   
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(preferencia: Preferencia): Long

    @Update
    suspend fun actualizar(preferencia: Preferencia)

    /**
     * Obtiene las preferencias de un usuario de forma síncrona.
     *
     * @param usuarioId El ID del usuario
     * @return Las preferencias del usuario o null si no existen
     */
    @Query("SELECT * FROM preferencias WHERE usuarioId = :usuarioId")
    suspend fun obtenerPreferenciaSync(usuarioId: Int): Preferencia?
}