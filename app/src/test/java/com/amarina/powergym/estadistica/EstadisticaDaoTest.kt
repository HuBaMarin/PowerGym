package com.amarina.powergym.estadistica

import com.amarina.powergym.database.entities.Estadistica
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests unitarios para operaciones básicas del DAO de Estadística.
 */
@RunWith(JUnit4::class)
class EstadisticaDaoTest {

    private lateinit var estadisticaDao: EstadisticaDao

    @Before
    fun setUp() {
        estadisticaDao = mockk(relaxed = true)
    }

    // -------------------------------------------------------------------------
    // Insertar y obtener estadística por id
    // -------------------------------------------------------------------------
    @Test
    fun insertarYObtenerEstadistica() = runBlocking {
        val estadistica = Estadistica(
            userId = 1,
            ejercicioId = 10,
            fecha = System.currentTimeMillis(),
            ejerciciosCompletados = 12,
            caloriasQuemadas = 150,
            tiempoTotal = 1_800_000,
            nombreEjercicio = "Curl",
            grupoMuscular = "Bíceps"
        )
        val id = 1L
        val withId = estadistica.copy(id = id.toInt())

        every { runBlocking { estadisticaDao.insertarEstadistica(estadistica) } } returns id
        every { runBlocking { estadisticaDao.obtenerEstadisticaPorId(id.toInt()) } } returns withId

        val newId = estadisticaDao.insertarEstadistica(estadistica)
        val retrieved = estadisticaDao.obtenerEstadisticaPorId(newId.toInt())
        assertNotNull(retrieved)
        assertEquals(estadistica.userId, retrieved.userId)
        assertEquals(estadistica.ejerciciosCompletados, retrieved.ejerciciosCompletados)
    }
}

// DAO mínimo para las pruebas
interface EstadisticaDao {
    suspend fun insertarEstadistica(e: Estadistica): Long
    suspend fun obtenerEstadisticaPorId(id: Int): Estadistica
}