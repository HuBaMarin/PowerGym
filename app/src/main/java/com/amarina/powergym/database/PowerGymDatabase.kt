package com.amarin.powergym.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amarin.powergym.database.dao.ActivityDao
import com.amarin.powergym.database.dao.RoutineDao
import com.amarin.powergym.database.dao.UserDao
import com.amarin.powergym.database.entities.Actividad
import com.amarin.powergym.database.entities.Rutina
import com.amarin.powergym.database.entities.Usuario

@Database(entities = [Usuario::class, Actividad::class, Rutina::class], version = 1, exportSchema = false) abstract class PowerGymDatabase : RoomDatabase() { abstract fun userDao(): UserDao
    abstract fun activityDao(): ActivityDao
    abstract fun routineDao(): RoutineDao
}