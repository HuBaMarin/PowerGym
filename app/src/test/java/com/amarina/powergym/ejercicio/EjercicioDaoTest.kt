package com.amarina.powergym.ejercicio

import com.amarina.powergym.database.PowerGymDatabase
import com.amarina.powergym.database.dao.EjercicioDao
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Conjunto de tests unitarios para el DAO de Ejercicios.
 */
@RunWith(JUnit4::class)
class EjercicioDaoTest {

    private lateinit var ejercicioDao: EjercicioDao
    private lateinit var db: PowerGymDatabase

    @Before
    fun setUp() {
        db = mockk(relaxed = true)
        ejercicioDao = mockk(relaxed = true)
        every { db.ejercicioDao() } returns ejercicioDao
    }

    // --------------------------------------------------------------
    // Insertar y obtener un ejercicio
    // --------------------------------------------------------------
    @Test
    fun insertarYObtenerEjercicio() = runBlocking {
        val ejercicio = baseExercise("Press de banca", "Pecho")
        val id = 1L
        val ejercicioWithId = ejercicio.copy(id = id.toInt())

        every { runBlocking { ejercicioDao.insertar(ejercicio) } } returns id
        every { runBlocking { ejercicioDao.obtenerEjercicioPorId(id.toInt()) } } returns ejercicioWithId

        val resultId = ejercicioDao.insertar(ejercicio)
        val retrieved = ejercicioDao.obtenerEjercicioPorId(resultId.toInt())

        assertNotNull(retrieved)
        assertEquals(ejercicio.nombre, retrieved.nombre)
        assertEquals(ejercicio.grupoMuscular, retrieved.grupoMuscular)
    }

    // --------------------------------------------------------------
    // Insertar varios ejercicios y obtener lista
    // --------------------------------------------------------------
    @Test
    fun insertarVariosEjercicios() = runBlocking {
        val ejercicios = listOf(
            baseExercise("Press de banca", "Pecho"),
            baseExercise("Sentadillas", "Piernas")
        )

        every { runBlocking { ejercicioDao.insertarTodos(ejercicios) } } returns Unit
        every { runBlocking { ejercicioDao.obtenerTodosEjercicios() } } returns ejercicios

        ejercicioDao.insertarTodos(ejercicios)

        val list = ejercicioDao.obtenerTodosEjercicios()
        assertEquals(2, list.size)
    }

    // --------------------------------------------------------------
    // Eliminar ejercicio
    // --------------------------------------------------------------
    @Test
    fun eliminarEjercicio() = runBlocking {
        val ejercicio = baseExercise("Eliminable", "Test")
        val id = 2L
        val withId = ejercicio.copy(id = id.toInt())

        every { runBlocking { ejercicioDao.insertar(ejercicio) } } returns id
        every { runBlocking { ejercicioDao.obtenerEjercicioPorId(id.toInt()) } } returns withId andThen null

        val resultId = ejercicioDao.insertar(ejercicio)
        val retrieved = ejercicioDao.obtenerEjercicioPorId(resultId.toInt())
        assertNotNull(retrieved)

        ejercicioDao.eliminar(retrieved)
        val deleted = ejercicioDao.obtenerEjercicioPorId(resultId.toInt())
        assertNull(deleted)
    }

    // --------------------------------------------------------------
    // Secciones únicas
    // --------------------------------------------------------------
    @Test
    fun obtenerSeccionesDistintas() = runBlocking {
        val ejercicios = listOf(
            baseExercise("E1", "Pecho", seccion = "Parte Superior"),
            baseExercise("E2", "Piernas", seccion = "Parte Inferior"),
            baseExercise("E3", "Espalda", seccion = "Parte Superior"),
            baseExercise("E4", "Full", seccion = "Cardio")
        )
        every { runBlocking { ejercicioDao.insertarTodos(ejercicios) } } returns Unit
        every { runBlocking { ejercicioDao.obtenerTodasSecciones() } } returns listOf(
            "Parte Superior",
            "Parte Inferior",
            "Cardio"
        )

        ejercicioDao.insertarTodos(ejercicios)
        val secciones = ejercicioDao.obtenerTodasSecciones()
        assertEquals(3, secciones.size)
        assertTrue(secciones.contains("Cardio"))
    }

    // --------------------------------------------------------------
    // Grupos musculares
    // --------------------------------------------------------------
    @Test
    fun obtenerGruposMusculares() = runBlocking {
        every { runBlocking { ejercicioDao.obtenerTodosGruposMusculares() } } returns listOf(
            "Pecho",
            "Piernas"
        )
        val grupos = ejercicioDao.obtenerTodosGruposMusculares()
        assertEquals(2, grupos.size)
        assertTrue(grupos.contains("Pecho"))
    }

    // --------------------------------------------------------------
    // Valores dificultad
    // --------------------------------------------------------------
    @Test
    fun obtenerValoresDeDificultad() = runBlocking {
        every { runBlocking { ejercicioDao.obtenerValoresDificultad() } } returns listOf(
            "Básico",
            "Medio",
            "Avanzado"
        )
        val difs = ejercicioDao.obtenerValoresDificultad()
        assertEquals(3, difs.size)
        assertTrue(difs.contains("Medio"))
    }

    // --------------------------------------------------------------
    // Filtros
    // --------------------------------------------------------------
    @Test
    fun filtrarEjerciciosPorCampos() = runBlocking {
        val pecho = baseExercise("Press de banca", "Pecho", dificultad = "Medio", dias = "Lunes")
        val piernas = baseExercise("Sentadillas", "Piernas", dificultad = "Básico", dias = "Martes")

        every { runBlocking { ejercicioDao.insertar(pecho) } } returns 10L
        every { runBlocking { ejercicioDao.insertar(piernas) } } returns 11L

        every { runBlocking { ejercicioDao.obtenerEjerciciosFiltrados(dificultad = "Medio") } } returns listOf(
            pecho
        )
        every { runBlocking { ejercicioDao.obtenerEjerciciosFiltrados(grupoMuscular = "Piernas") } } returns listOf(
            piernas
        )
        every { runBlocking { ejercicioDao.obtenerEjerciciosFiltrados(dias = "Lunes") } } returns listOf(
            pecho
        )

        ejercicioDao.insertar(pecho)
        ejercicioDao.insertar(piernas)

        assertEquals(1, ejercicioDao.obtenerEjerciciosFiltrados(dificultad = "Medio").size)
        assertEquals(1, ejercicioDao.obtenerEjerciciosFiltrados(grupoMuscular = "Piernas").size)
        assertEquals(1, ejercicioDao.obtenerEjerciciosFiltrados(dias = "Lunes").size)
    }

    // --------------------------------------------------------------
    // Helper para crear ejercicios base
    // --------------------------------------------------------------
    private fun baseExercise(
        nombre: String,
        grupo: String,
        seccion: String = "Parte Superior",
        dificultad: String = "Básico",
        dias: String = "Lunes, Miércoles"
    ) = Ejercicio(
        nombre = nombre,
        descripcion = "Desc $nombre",
        grupoMuscular = grupo,
        dificultad = dificultad,
        dias = dias,
        imagenEjercicio = "https://example.com/img.jpg",
        videoUrl = "",
        seccion = seccion,
        calorias = 100,
        frecuencia = 10,
        porcentaje = 0.3f
    )
}
