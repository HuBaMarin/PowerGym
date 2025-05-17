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

    @Query("SELECT * FROM preferencias WHERE usuarioId = :usuarioId")
    suspend fun obtenerPreferenciaSync(usuarioId: Int): Preferencia?
}