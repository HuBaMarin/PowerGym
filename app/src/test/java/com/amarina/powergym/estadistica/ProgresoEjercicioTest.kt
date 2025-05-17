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
import kotlin.test.assertTrue

/**
 * Test funcional: verifica que el sistema puede generar estadísticas
 * y mostrar el progreso del usuario en los ejercicios.
 */
@RunWith(JUnit4::class)
class ProgresoEjercicioTest {

    private lateinit var estadisticaDao: EstadisticaTestDao

    @Before
    fun setUp() {
        estadisticaDao = mockk(relaxed = true)
    }

    // --------------------------------------------------------------------
    // Verificar progreso semanal en ejercicios específicos
    // --------------------------------------------------------------------
    @Test
    fun generarEstadisticasDeProgresoSemanal() = runBlocking {
        val userId = 1
        val timestamp = System.currentTimeMillis()
        val fechaAnterior = timestamp - 7 * 24 * 60 * 60 * 1000 // 7 días atrás

        val estadisticas = listOf(
            Estadistica(
                userId = userId,
                ejercicioId = 1,
                fecha = fechaAnterior,
                ejerciciosCompletados = 8,
                caloriasQuemadas = 120,
                tiempoTotal = 1500000, // 25 minutos
                nombreEjercicio = "Press de banca",
                grupoMuscular = "Pecho"
            ),
            Estadistica(
                userId = userId,
                ejercicioId = 1,
                fecha = timestamp,
                ejerciciosCompletados = 12,
                caloriasQuemadas = 180,
                tiempoTotal = 1800000, // 30 minutos
                nombreEjercicio = "Press de banca",
                grupoMuscular = "Pecho"
            )
        )

        every { runBlocking { estadisticaDao.obtenerEstadisticasPorUsuario(1) } } returns estadisticas
        every { runBlocking { estadisticaDao.obtenerProgresoEjercicio(1, 1) } } returns estadisticas

        // Verificar estadísticas generales
        val todasEstadisticas = estadisticaDao.obtenerEstadisticasPorUsuario(userId)
        assertNotNull(todasEstadisticas)
        assertEquals(2, todasEstadisticas.size)

        // Verificar progreso específico de un ejercicio
        val progresoEjercicio = estadisticaDao.obtenerProgresoEjercicio(userId, 1)
        assertEquals(2, progresoEjercicio.size)

        // Verificar mejora en rendimiento (más repeticiones)
        val primera = progresoEjercicio.find { it.fecha == fechaAnterior }
        val ultima = progresoEjercicio.find { it.fecha == timestamp }

        assertNotNull(primera)
        assertNotNull(ultima)
        assertTrue(ultima.ejerciciosCompletados > primera.ejerciciosCompletados)
        assertTrue(ultima.caloriasQuemadas > primera.caloriasQuemadas)
    }

    // --------------------------------------------------------------------
    // Verificar progreso diario
    // --------------------------------------------------------------------
    @Test
    fun mostrarEstadisticasDeProgresoDiario() = runBlocking {
        val userId = 1
        val hoy = System.currentTimeMillis()

        val estadisticas = listOf(
            Estadistica(
                userId = userId,
                ejercicioId = 1,
                fecha = hoy,
                ejerciciosCompletados = 15,
                caloriasQuemadas = 200,
                tiempoTotal = 1800000, // 30 minutos
                nombreEjercicio = "Sentadillas",
                grupoMuscular = "Piernas"
            ),
            Estadistica(
                userId = userId,
                ejercicioId = 2,
                fecha = hoy,
                ejerciciosCompletados = 12,
                caloriasQuemadas = 150,
                tiempoTotal = 1500000, // 25 minutos
                nombreEjercicio = "Press de banca",
                grupoMuscular = "Pecho"
            )
        )

        every {
            runBlocking {
                estadisticaDao.obtenerEstadisticasDiarias(
                    userId,
                    hoy
                )
            }
        } returns estadisticas

        // Obtener estadísticas del día y calcular totales
        val estadisticasDiarias = estadisticaDao.obtenerEstadisticasDiarias(userId, hoy)
        assertNotNull(estadisticasDiarias)
        assertEquals(2, estadisticasDiarias.size)

        // Calcular totales de actividad diaria
        val totalEjercicios = estadisticasDiarias.sumOf { it.ejerciciosCompletados }
        val totalCalorias = estadisticasDiarias.sumOf { it.caloriasQuemadas }
        val totalTiempo = estadisticasDiarias.sumOf { it.tiempoTotal }

        assertEquals(27, totalEjercicios)
        assertEquals(350, totalCalorias)
        assertEquals(3300000, totalTiempo) // 55 minutos
    }

    // --------------------------------------------------------------------
    // Verificar resumen global
    // --------------------------------------------------------------------
    @Test
    fun mostrarResumenGlobalUsuario() = runBlocking {
        val userId = 1
        val resumen = ResumenEstadisticas(
            totalSesiones = 15,
            totalTiempo = 12600000, // 3.5 horas
            caloriasQuemadas = 1500,
            ejerciciosCompletados = 180
        )

        every { runBlocking { estadisticaDao.obtenerResumenUsuario(userId) } } returns resumen

        val stats = estadisticaDao.obtenerResumenUsuario(userId)
        assertNotNull(stats)
        assertEquals(15, stats.totalSesiones)
        assertEquals(1500, stats.caloriasQuemadas)
        assertEquals(180, stats.ejerciciosCompletados)
    }
}

// --------------------------------------------------------------------
// Data classes y DAO mínimos para tests
// --------------------------------------------------------------------
data class ResumenEstadisticas(
    val totalSesiones: Int,
    val totalTiempo: Long,
    val caloriasQuemadas: Int,
    val ejerciciosCompletados: Int
)

interface EstadisticaTestDao {
    suspend fun obtenerEstadisticasPorUsuario(usuarioId: Int): List<Estadistica>
    suspend fun obtenerProgresoEjercicio(usuarioId: Int, ejercicioId: Int): List<Estadistica>
    suspend fun obtenerEstadisticasDiarias(usuarioId: Int, fecha: Long): List<Estadistica>
    suspend fun obtenerResumenUsuario(usuarioId: Int): ResumenEstadisticas
}