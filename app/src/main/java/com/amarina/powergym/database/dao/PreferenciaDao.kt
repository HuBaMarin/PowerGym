package com.amarina.powergym.database.dao

import androidx.room.*
import com.amarina.powergym.database.entities.Preferencia
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenciaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preferencia: Preferencia): Long

    @Update
    suspend fun update(preferencia: Preferencia)

    @Query("SELECT * FROM preferencias WHERE usuarioId = :usuarioId")
    fun getPreferenciasByUsuario(usuarioId: Int): Flow<Preferencia?>

    @Query("SELECT * FROM preferencias WHERE usuarioId = :usuarioId")
    suspend fun getPreferenciasSync(usuarioId: Int): Preferencia?
}
