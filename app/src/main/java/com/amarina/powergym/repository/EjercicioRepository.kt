package com.amarina.powergym.repository

import com.amarina.powergym.database.dao.EjercicioDao
import com.amarina.powergym.database.entities.Ejercicio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EjercicioRepository(private val ejercicioDao: EjercicioDao) {

    suspend fun getAllEjercicios(): Result<List<Ejercicio>> = withContext(Dispatchers.IO) {
        try {
            val ejercicios = ejercicioDao.getAllEjercicios()
            Result.success(ejercicios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEjerciciosByGroup(group: String): Result<List<Ejercicio>> = withContext(Dispatchers.IO) {
        try {
            val ejercicios = ejercicioDao.getEjerciciosByGroup(group)
            Result.success(ejercicios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllMuscleGroups(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val groups = ejercicioDao.getAllMuscleGroups()
            Result.success(groups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllSections(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val sections = ejercicioDao.getAllSections()
            Result.success(sections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFilteredEjercicios(
        dias: String? = null,
        dificultad: String? = null,
        grupoMuscular: String? = null,
        query: String = ""
    ): Result<List<Ejercicio>> = withContext(Dispatchers.IO) {
        try {
            val ejercicios = ejercicioDao.getFilteredEjercicios(
                dias = dias,
                dificultad = dificultad,
                grupoMuscular = grupoMuscular,
                query = query
            )
            Result.success(ejercicios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEjercicioById(id: Int): Result<Ejercicio> = withContext(Dispatchers.IO) {
        try {
            val ejercicio = ejercicioDao.getEjercicioById(id)
            if (ejercicio != null) {
                Result.success(ejercicio)
            } else {
                Result.failure(Exception("Ejercicio no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
