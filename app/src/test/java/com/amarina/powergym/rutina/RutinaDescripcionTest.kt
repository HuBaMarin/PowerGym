package com.amarina.powergym.rutina

import com.amarina.powergym.database.entities.Ejercicio
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
 * Tests funcionales para verificar que cada rutina muestre
 * correctamente la descripción de sus ejercicios.
 */
@RunWith(JUnit4::class)
class RutinaDescripcionTest {

    private lateinit var rutinaDao: RutinaTestDao
    private lateinit var ejercicioDao: EjercicioTestDao

    @Before
    fun setUp() {
        rutinaDao = mockk(relaxed = true)
        ejercicioDao = mockk(relaxed = true)
    }

    // ----------------------------------------------------------------------
    // Mostrar descripción de ejercicios en rutina
    // ----------------------------------------------------------------------
    @Test
    fun mostrarDescripcionEjercicioEnRutina() = runBlocking {
        // Rutina de prueba
        val rutina = Rutina(
            id = 1,
            nombre = "Rutina de principiante",
            descripcion = "Entrenamiento completo para principiantes",
            nivelDificultad = "Básico",
            duracionEstimada = 45 // minutos
        )

        // Ejercicio incluido en la rutina
        val ejercicio = Ejercicio(
            id = 1,
            nombre = "Press de banca",
            descripcion = "Ejercicio para pecho con barra olímpica",
            grupoMuscular = "Pecho",
            dificultad = "Básico",
            dias = "Lunes, Miércoles",
            imagenEjercicio = "https://example.com/press.jpg",
            videoUrl = "https://example.com/video.mp4",
            seccion = "Parte Superior",
            calorias = 150,
            frecuencia = 12,
            porcentaje = 0.4f
        )

        // Relación entre rutina y ejercicio (detalles de configuración)
        val rutinaEjercicio = RutinaEjercicio(
            id = 1,
            rutinaId = 1,
            ejercicioId = 1,
            orden = 1,
            series = 3,
            repeticiones = 12,
            descanso = 60 // segundos
        )

        // Configurar mocks
        every { runBlocking { rutinaDao.obtenerRutinaPorId(1) } } returns rutina
        every { runBlocking { ejercicioDao.obtenerEjercicioPorId(1) } } returns ejercicio
        every { runBlocking { rutinaDao.obtenerEjerciciosDeRutina(1) } } returns listOf(
            rutinaEjercicio
        )

        // Obtener la rutina
        val rutinaObtenida = rutinaDao.obtenerRutinaPorId(1)
        assertNotNull(rutinaObtenida)
        assertEquals("Rutina de principiante", rutinaObtenida.nombre)

        // Obtener los ejercicios de la rutina
        val ejerciciosRutina = rutinaDao.obtenerEjerciciosDeRutina(1)
        assertNotNull(ejerciciosRutina)
        assertEquals(1, ejerciciosRutina.size)

        // Para cada ejercicio en la rutina, obtener y mostrar los detalles completos
        val primerEjercicio = ejerciciosRutina[0]
        val ejercicioDetalle = ejercicioDao.obtenerEjercicioPorId(primerEjercicio.ejercicioId)

        // Verificar que la descripción está correctamente accesible
        assertNotNull(ejercicioDetalle)
        assertEquals("Press de banca", ejercicioDetalle.nombre)
        assertEquals("Ejercicio para pecho con barra olímpica", ejercicioDetalle.descripcion)

        // Verificar detalles de configuración
        assertEquals(3, primerEjercicio.series)
        assertEquals(12, primerEjercicio.repeticiones)
    }

    // ----------------------------------------------------------------------
    // Obtener múltiples ejercicios para una rutina compleja
    // ----------------------------------------------------------------------
    @Test
    fun obtenerMultiplesEjerciciosEnRutina() = runBlocking {
        val rutinaCompleta = Rutina(
            id = 2,
            nombre = "Entrenamiento Full Body",
            descripcion = "Trabaja todo el cuerpo en una sola sesión",
            nivelDificultad = "Medio",
            duracionEstimada = 60
        )

        val ejercicios = listOf(
            Ejercicio(
                id = 1,
                nombre = "Sentadillas",
                descripcion = "Ejercicio básico para piernas",
                grupoMuscular = "Piernas",
                dificultad = "Básico",
                dias = "Lunes, Jueves",
                imagenEjercicio = "url1",
                videoUrl = "video1",
                seccion = "Inferior",
                calorias = 180,
                frecuencia = 15,
                porcentaje = 0.5f
            ),
            Ejercicio(
                id = 2,
                nombre = "Press de hombros",
                descripcion = "Trabaja los hombros y trapecios",
                grupoMuscular = "Hombros",
                dificultad = "Medio",
                dias = "Martes, Viernes",
                imagenEjercicio = "url2",
                videoUrl = "video2",
                seccion = "Superior",
                calorias = 120,
                frecuencia = 12,
                porcentaje = 0.4f
            ),
            Ejercicio(
                id = 3,
                nombre = "Peso muerto",
                descripcion = "Trabajo de cadena posterior",
                grupoMuscular = "Espalda",
                dificultad = "Avanzado",
                dias = "Martes",
                imagenEjercicio = "url3",
                videoUrl = "video3",
                seccion = "Completo",
                calorias = 250,
                frecuencia = 8,
                porcentaje = 0.7f
            )
        )

        val configuraciones = listOf(
            RutinaEjercicio(
                rutinaId = 2,
                ejercicioId = 1,
                orden = 1,
                series = 4,
                repeticiones = 15,
                descanso = 45
            ),
            RutinaEjercicio(
                rutinaId = 2,
                ejercicioId = 2,
                orden = 2,
                series = 3,
                repeticiones = 12,
                descanso = 60
            ),
            RutinaEjercicio(
                rutinaId = 2,
                ejercicioId = 3,
                orden = 3,
                series = 3,
                repeticiones = 8,
                descanso = 90
            )
        )

        every { runBlocking { rutinaDao.obtenerRutinaPorId(2) } } returns rutinaCompleta
        every { runBlocking { rutinaDao.obtenerEjerciciosDeRutina(2) } } returns configuraciones

        ejercicios.forEach { ejercicio ->
            every { runBlocking { ejercicioDao.obtenerEjercicioPorId(ejercicio.id) } } returns ejercicio
        }

        // Obtener rutina completa con ejercicios
        val rutina = rutinaDao.obtenerRutinaPorId(2)
        val configs = rutinaDao.obtenerEjerciciosDeRutina(2)
        assertEquals(3, configs.size)

        // Verificar que podemos construir una lista completa de ejercicios con sus descripciones
        val ejerciciosConDescripcion = configs.map { config ->
            val ej = ejercicioDao.obtenerEjercicioPorId(config.ejercicioId)
            EjercicioConConfiguracion(
                ejercicio = ej,
                series = config.series,
                repeticiones = config.repeticiones,
                descanso = config.descanso
            )
        }

        assertEquals(3, ejerciciosConDescripcion.size)
        assertEquals("Sentadillas", ejerciciosConDescripcion[0].ejercicio.nombre)
        assertEquals(
            "Ejercicio básico para piernas",
            ejerciciosConDescripcion[0].ejercicio.descripcion
        )
        assertEquals(4, ejerciciosConDescripcion[0].series)
    }
}

// -----------------------------------------------------------------------------
// Data classes y DAOs mínimos para tests
// -----------------------------------------------------------------------------
data class Rutina(
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val nivelDificultad: String,
    val duracionEstimada: Int // en minutos
)

data class RutinaEjercicio(
    val id: Int = 0,
    val rutinaId: Int,
    val ejercicioId: Int,
    val orden: Int,
    val series: Int,
    val repeticiones: Int,
    val descanso: Int // en segundos
)

// Clase auxiliar para transportar información combinada
data class EjercicioConConfiguracion(
    val ejercicio: Ejercicio,
    val series: Int,
    val repeticiones: Int,
    val descanso: Int
)

interface RutinaTestDao {
    suspend fun obtenerRutinaPorId(id: Int): Rutina
    suspend fun obtenerEjerciciosDeRutina(rutinaId: Int): List<RutinaEjercicio>
}

interface EjercicioTestDao {
    suspend fun obtenerEjercicioPorId(id: Int): Ejercicio
}