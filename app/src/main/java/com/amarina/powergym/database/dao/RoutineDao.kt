package com.amarin.powergym.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RoutineDao
{
    @Insert
    suspend fun insertRoutine(routine: Routine): Long

    @Query("SELECT * FROM Routine")
    suspend fun getAllRoutines(): List<Routine>

    @Query("SELECT * FROM Routine WHERE name = :name")
    suspend fun getRoutineByName(name: String): List<Routine>

    @Update
    suspend fun updateRoutine(routine: Routine)

    @Delete
    suspend fun deleteRoutine(routine: Routine)
}