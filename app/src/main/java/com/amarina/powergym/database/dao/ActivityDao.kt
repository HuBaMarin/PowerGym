package com.amarin.powergym.database.dao

import android.app.Activity
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ActivityDao {

    @Insert
    suspend fun insertActivity(activity: Activity): Long

    @Query("SELECT * FROM Actividad")
    suspend fun getAllActivities(): List<Activity>

    @Query("SELECT * FROM Actividad WHERE grupoEdad = :group")
    suspend fun getActivitiesByAgeGroup(group: String): List<Activity>

    @Update
    suspend fun updateActivity(activity: Activity)

    @Delete
    suspend fun deleteActivity(activity: Activity)
}