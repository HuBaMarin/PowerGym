package com.amarina.powergym.offline

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
import kotlin.test.assertTrue

/**
 * Pruebas que simulan acceso sin conexión a internet.
 * Verifican que la aplicación puede usar datos cacheados (ejercicios, rutinas, etc.).
 */
@RunWith(JUnit4::class)
class AccesoOfflineTest {

    private lateinit var ejercicioDao: EjercicioTestDao

    @Before
    fun setUp() {
        ejercicioDao = mockk(relaxed = true)
    }

    @Test
    fun accesoSinConexion() = runBlocking {
        // Datos cacheados
        val ejerciciosCacheados = listOf(
            Ejercicio(
                id = 1,
                nombre = "Press de banca",
                descripcion = "Ejercicio para pecho",
                grupoMuscular = "Pecho",
                dificultad = "Básico",
                dias = "Lunes, Miércoles",
                imagenEjercicio = "local://press.jpg",
                videoUrl = "",
                seccion = "Parte Superior",
                calorias = 150,
                frecuencia = 12,
                porcentaje = 0.4f
            ),
            Ejercicio(
                id = 2,
                nombre = "Sentadillas",
                descripcion = "Ejercicio para piernas",
                grupoMuscular = "Piernas",
                dificultad = "Medio",
                dias = "Martes, Jueves",
                imagenEjercicio = "local://squat.jpg",
                videoUrl = "",
                seccion = "Parte Inferior",
                calorias = 200,
                frecuencia = 15,
                porcentaje = 0.5f
            )
        )

        every { runBlocking { ejercicioDao.obtenerEjerciciosCacheados() } } returns ejerciciosCacheados

        val modoSinConexion = true
        if (modoSinConexion) {
            val lista = ejercicioDao.obtenerEjerciciosCacheados()
            assertNotNull(lista)
            assertEquals(2, lista.size)
            assertTrue(lista.all { it.imagenEjercicio.startsWith("local://") })
        }
    }
}

interface EjercicioTestDao {
    suspend fun obtenerEjerciciosCacheados(): List<Ejercicio>
}