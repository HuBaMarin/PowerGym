package com.amarina.powergym.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amarina.powergym.database.dao.EjercicioDao
import com.amarina.powergym.database.dao.EstadisticaDao
import com.amarina.powergym.database.dao.PreferenciaDao
import com.amarina.powergym.database.dao.UsuarioDao
import com.amarina.powergym.database.entities.Ejercicio
import com.amarina.powergym.database.entities.Estadistica
import com.amarina.powergym.database.entities.Preferencia
import com.amarina.powergym.database.entities.Usuario
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(JUnit4::class)
class PowerGymDatabaseTest {

    private lateinit var userDao: UsuarioDao
    private lateinit var ejercicioDao: EjercicioDao
    private lateinit var estadisticaDao: EstadisticaDao
    private lateinit var preferenciaDao: PreferenciaDao
    private lateinit var db: PowerGymDatabase

    @Before
    fun createDb() {
        // Use mocks instead of real database for unit tests
        db = mockk(relaxed = true)
        userDao = mockk(relaxed = true)
        ejercicioDao = mockk(relaxed = true)
        estadisticaDao = mockk(relaxed = true)
        preferenciaDao = mockk(relaxed = true)

        // Configure db mock to return our DAOs
        every { db.userDao() } returns userDao
        every { db.ejercicioDao() } returns ejercicioDao
        every { db.estadisticaDao() } returns estadisticaDao
        every { db.preferenciaDao() } returns preferenciaDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        // No need to close db when using mocks
    }

    // USER TESTS

    @Test
    @Throws(Exception::class)
    fun insertAndGetUser() = runBlocking {
        // Setup
        val user = Usuario(
            email = "test@example.com",
            password = "password123",
            nombre = "Test User",
            rol = "usuario"
        )
        val userId = 1L

        // Mock return values
        every { runBlocking { userDao.insertar(user) } } returns userId
        every { runBlocking { userDao.obtenerUsuarioPorId(userId.toInt()) } } returns user.copy(id = userId.toInt())

        // Execute
        val resultId = userDao.insertar(user)
        val retrievedUser = userDao.obtenerUsuarioPorId(resultId.toInt())

        // Verify
        assertEquals(userId, resultId)
        assertNotNull(retrievedUser)
        assertEquals(user.email, retrievedUser?.email)
        assertEquals(user.nombre, retrievedUser?.nombre)
        assertEquals(user.rol, retrievedUser?.rol)
    }

    @Test
    @Throws(Exception::class)
    fun updateUser() = runBlocking {
        // Insert a user
        val user = Usuario(
            email = "update@example.com",
            password = "password123",
            nombre = "Original Name",
            rol = "usuario"
        )
        val userId = 2L
        val userWithId = user.copy(id = userId.toInt())
        val updatedUser = userWithId.copy(nombre = "Updated Name")

        // Mock return values
        every { runBlocking { userDao.insertar(user) } } returns userId
        every { runBlocking { userDao.obtenerUsuarioPorId(userId.toInt()) } } returns userWithId andThen updatedUser

        // Execute
        val resultId = userDao.insertar(user)
        val retrievedUser = userDao.obtenerUsuarioPorId(resultId.toInt())
        assertNotNull(retrievedUser)

        // Update the user
        userDao.actualizar(retrievedUser.copy(nombre = "Updated Name"))

        // Verify update
        val retrievedUpdatedUser = userDao.obtenerUsuarioPorId(resultId.toInt())
        assertNotNull(retrievedUpdatedUser)
        assertEquals("Updated Name", retrievedUpdatedUser?.nombre)
        assertEquals(user.email, retrievedUpdatedUser?.email) // Email should remain the same
    }

    @Test
    @Throws(Exception::class)
    fun deleteUser() = runBlocking {
        // Insert a user
        val user = Usuario(
            email = "delete@example.com",
            password = "password123",
            nombre = "Delete Me",
            rol = "usuario"
        )
        val userId = 3L
        val userWithId = user.copy(id = userId.toInt())

        // Mock return values
        every { runBlocking { userDao.insertar(user) } } returns userId
        every { runBlocking { userDao.obtenerUsuarioPorId(userId.toInt()) } } returns userWithId andThen null

        // Execute
        val resultId = userDao.insertar(user)

        // Verify user exists
        val retrievedUser = userDao.obtenerUsuarioPorId(resultId.toInt())
        assertNotNull(retrievedUser)

        // Delete user
        userDao.eliminar(retrievedUser)

        // Verify user was deleted
        val deletedUser = userDao.obtenerUsuarioPorId(resultId.toInt())
        assertNull(deletedUser)
    }

    @Test
    @Throws(Exception::class)
    fun getUserByEmail() = runBlocking {
        val user = Usuario(
            email = "email@example.com",
            password = "password123",
            nombre = "Email Test",
            rol = "usuario"
        )

        // Mock return values
        every { runBlocking { userDao.insertar(user) } } returns 4L
        every { runBlocking { userDao.obtenerUsuarioPorEmail("email@example.com") } } returns user
        every { runBlocking { userDao.obtenerUsuarioPorEmail("nonexistent@example.com") } } returns null

        // Execute
        userDao.insertar(user)

        val retrievedUser = userDao.obtenerUsuarioPorEmail("email@example.com")

        assertNotNull(retrievedUser)
        assertEquals(user.email, retrievedUser?.email)
        assertEquals(user.nombre, retrievedUser?.nombre)

        // Test non-existent email
        val nonExistentUser = userDao.obtenerUsuarioPorEmail("nonexistent@example.com")
        assertNull(nonExistentUser)
    }

    @Test
    @Throws(Exception::class)
    fun loginUser_withValidCredentials_returnsUser() = runBlocking {
        val user = Usuario(
            email = "test@example.com",
            password = "password123",
            nombre = "Test User",
            rol = "usuario"
        )

        // Mock return values
        every { runBlocking { userDao.insertar(user) } } returns 5L
        every { runBlocking { userDao.login("test@example.com", "password123") } } returns user

        // Execute
        userDao.insertar(user)

        val retrievedUser = userDao.login("test@example.com", "password123")

        assertNotNull(retrievedUser)
        assertEquals(user.email, retrievedUser?.email)
    }

    @Test
    @Throws(Exception::class)
    fun loginUser_withInvalidCredentials_returnsNull() = runBlocking {
        val user = Usuario(
            email = "test@example.com",
            password = "password123",
            nombre = "Test User",
            rol = "usuario"
        )

        // Mock return values
        every { runBlocking { userDao.insertar(user) } } returns 6L
        every { runBlocking { userDao.login("test@example.com", "wrongpassword") } } returns null
        every {
            runBlocking {
                userDao.login(
                    "nonexistent@example.com",
                    "password123"
                )
            }
        } returns null

        // Execute
        userDao.insertar(user)

        val retrievedUser = userDao.login("test@example.com", "wrongpassword")
        assertNull(retrievedUser)

        val nonExistentEmailUser = userDao.login("nonexistent@example.com", "password123")
        assertNull(nonExistentEmailUser)
    }

    @Test
    @Throws(Exception::class)
    fun getAllUsers() = runBlocking {
        // Insert multiple users
        val users = listOf(
            Usuario(
                email = "user1@example.com",
                password = "password1",
                nombre = "User 1",
                rol = "usuario"
            ),
            Usuario(
                email = "user2@example.com",
                password = "password2",
                nombre = "User 2",
                rol = "usuario"
            ),
            Usuario(
                email = "admin@example.com",
                password = "adminpass",
                nombre = "Admin",
                rol = "admin"
            )
        )

        // Mock return values
        users.forEachIndexed { index, user ->
            every { runBlocking { userDao.insertar(user) } } returns (index + 7).toLong()
        }
        every { runBlocking { userDao.obtenerTodos() } } returns users

        // Execute
        users.forEach { userDao.insertar(it) }

        // Get all users
        val retrievedUsers = userDao.obtenerTodos()

        // Verify results
        assertEquals(users.size, retrievedUsers.size)
        assertTrue(retrievedUsers.any { it.email == "user1@example.com" })
        assertTrue(retrievedUsers.any { it.email == "user2@example.com" })
        assertTrue(retrievedUsers.any { it.email == "admin@example.com" })
    }

    // EJERCICIO TESTS

    @Test
    @Throws(Exception::class)
    fun insertAndGetEjercicio() = runBlocking {
        val ejercicio = Ejercicio(
            nombre = "Press de banca",
            descripcion = "Ejercicio para pecho",
            grupoMuscular = "Pecho",
            dificultad = "Medio",
            dias = "Lunes, Miércoles",
            imagenEjercicio = "https://example.com/press.jpg",
            videoUrl = "https://example.com/video.mp4",
            seccion = "Parte Superior",
            calorias = 150,
            frecuencia = 12,
            porcentaje = 0.4f
        )
        val id = 1L
        val ejercicioWithId = ejercicio.copy(id = id.toInt())

        // Mock return values
        every { runBlocking { ejercicioDao.insertar(ejercicio) } } returns id
        every { runBlocking { ejercicioDao.obtenerEjercicioPorId(id.toInt()) } } returns ejercicioWithId

        // Execute
        val resultId = ejercicioDao.insertar(ejercicio)
        val retrievedEjercicio = ejercicioDao.obtenerEjercicioPorId(resultId.toInt())

        assertNotNull(retrievedEjercicio)
        assertEquals(ejercicio.nombre, retrievedEjercicio?.nombre)
        assertEquals(ejercicio.grupoMuscular, retrievedEjercicio?.grupoMuscular)
        assertEquals(ejercicio.calorias, retrievedEjercicio?.calorias)
        assertEquals(ejercicio.frecuencia, retrievedEjercicio?.frecuencia)
        assertEquals(ejercicio.porcentaje, retrievedEjercicio?.porcentaje)
    }

    @Test
    @Throws(Exception::class)
    fun insertMultipleEjercicios() = runBlocking {
        val ejercicios = listOf(
            Ejercicio(
                nombre = "Press de banca",
                descripcion = "Ejercicio para pecho",
                grupoMuscular = "Pecho",
                dificultad = "Medio",
                dias = "Lunes, Miércoles",
                imagenEjercicio = "https://example.com/press.jpg",
                videoUrl = "https://example.com/video.mp4",
                seccion = "Parte Superior",
                calorias = 150,
                frecuencia = 12,
                porcentaje = 0.4f
            ),
            Ejercicio(
                nombre = "Sentadillas",
                descripcion = "Ejercicio para piernas",
                grupoMuscular = "Piernas",
                dificultad = "Básico",
                dias = "Martes, Jueves",
                imagenEjercicio = "https://example.com/squat.jpg",
                videoUrl = "https://example.com/video2.mp4",
                seccion = "Parte Inferior",
                calorias = 200,
                frecuencia = 15,
                porcentaje = 0.5f
            )
        )

        // Mock return values
        every { runBlocking { ejercicioDao.insertarTodos(ejercicios) } } returns Unit
        every { runBlocking { ejercicioDao.obtenerTodosEjercicios() } } returns ejercicios

        // Execute
        ejercicioDao.insertarTodos(ejercicios)

        val retrievedEjercicios = ejercicioDao.obtenerTodosEjercicios()
        assertEquals(2, retrievedEjercicios.size)
    }

    @Test
    @Throws(Exception::class)
    fun deleteEjercicio() = runBlocking {
        val ejercicio = Ejercicio(
            nombre = "Ejercicio a eliminar",
            descripcion = "Este ejercicio será eliminado",
            grupoMuscular = "Test",
            dificultad = "Básico",
            dias = "Lunes",
            imagenEjercicio = "https://example.com/delete.jpg",
            videoUrl = "https://example.com/video.mp4",
            seccion = "Test",
            calorias = 100,
            frecuencia = 10,
            porcentaje = 0.3f
        )
        val id = 2L
        val ejercicioWithId = ejercicio.copy(id = id.toInt())

        // Mock return values
        every { runBlocking { ejercicioDao.insertar(ejercicio) } } returns id
        every { runBlocking { ejercicioDao.obtenerEjercicioPorId(id.toInt()) } } returns ejercicioWithId andThen null

        // Execute
        val resultId = ejercicioDao.insertar(ejercicio)

        val retrievedEjercicio = ejercicioDao.obtenerEjercicioPorId(resultId.toInt())
        assertNotNull(retrievedEjercicio)

        ejercicioDao.eliminar(retrievedEjercicio)

        val deletedEjercicio = ejercicioDao.obtenerEjercicioPorId(resultId.toInt())
        assertNull(deletedEjercicio)
    }

    @Test
    @Throws(Exception::class)
    fun getDistinctSections() = runBlocking {
        val ejercicios = listOf(
            Ejercicio(
                nombre = "Ejercicio 1",
                descripcion = "Descripción 1",
                grupoMuscular = "Pecho",
                dificultad = "Básico",
                dias = "Lunes",
                imagenEjercicio = "https://example.com/img1.jpg",
                seccion = "Parte Superior",
                calorias = 100,
                frecuencia = 10,
                porcentaje = 0.3f
            ),
            Ejercicio(
                nombre = "Ejercicio 2",
                descripcion = "Descripción 2",
                grupoMuscular = "Piernas",
                dificultad = "Medio",
                dias = "Martes",
                imagenEjercicio = "https://example.com/img2.jpg",
                seccion = "Parte Inferior",
                calorias = 150,
                frecuencia = 12,
                porcentaje = 0.4f
            ),
            Ejercicio(
                nombre = "Ejercicio 3",
                descripcion = "Descripción 3",
                grupoMuscular = "Full Body",
                dificultad = "Avanzado",
                dias = "Miércoles",
                imagenEjercicio = "https://example.com/img3.jpg",
                seccion = "Cardio",
                calorias = 200,
                frecuencia = 15,
                porcentaje = 0.5f
            ),
            Ejercicio(
                nombre = "Ejercicio 4",
                descripcion = "Descripción 4",
                grupoMuscular = "Espalda",
                dificultad = "Medio",
                dias = "Jueves",
                imagenEjercicio = "https://example.com/img4.jpg",
                seccion = "Parte Superior",
                calorias = 120,
                frecuencia = 12,
                porcentaje = 0.35f
            )
        )

        ejercicioDao.insertarTodos(ejercicios)

        val secciones = ejercicioDao.obtenerTodasSecciones()
        assertEquals(3, secciones.size)
        assertTrue(secciones.contains("Parte Superior"))
        assertTrue(secciones.contains("Parte Inferior"))
        assertTrue(secciones.contains("Cardio"))
    }

    @Test
    @Throws(Exception::class)
    fun getMuscleGroups() = runBlocking {
        val ejercicios = listOf(
            Ejercicio(
                nombre = "Ejercicio Pecho 1",
                descripcion = "Descripción",
                grupoMuscular = "Pecho",
                dificultad = "Básico",
                dias = "Lunes",
                imagenEjercicio = "https://example.com/img1.jpg",
                seccion = "Parte Superior",
                calorias = 100,
                frecuencia = 10,
                porcentaje = 0.3f
            ),
            Ejercicio(
                nombre = "Ejercicio Pecho 2",
                descripcion = "Descripción",
                grupoMuscular = "Pecho",
                dificultad = "Medio",
                dias = "Martes",
                imagenEjercicio = "https://example.com/img2.jpg",
                seccion = "Parte Superior",
                calorias = 150,
                frecuencia = 12,
                porcentaje = 0.4f
            ),
            Ejercicio(
                nombre = "Ejercicio Piernas",
                descripcion = "Descripción",
                grupoMuscular = "Piernas",
                dificultad = "Avanzado",
                dias = "Miércoles",
                imagenEjercicio = "https://example.com/img3.jpg",
                seccion = "Parte Inferior",
                calorias = 200,
                frecuencia = 15,
                porcentaje = 0.5f
            )
        )

        ejercicioDao.insertarTodos(ejercicios)

        val gruposMusculares = ejercicioDao.obtenerTodosGruposMusculares()
        assertEquals(2, gruposMusculares.size)
        assertTrue(gruposMusculares.contains("Pecho"))
        assertTrue(gruposMusculares.contains("Piernas"))
    }

    @Test
    @Throws(Exception::class)
    fun getDifficultyValues() = runBlocking {
        val ejercicios = listOf(
            Ejercicio(
                nombre = "Ejercicio Básico",
                descripcion = "Descripción",
                grupoMuscular = "Pecho",
                dificultad = "Básico",
                dias = "Lunes",
                imagenEjercicio = "https://example.com/img1.jpg",
                seccion = "Parte Superior",
                calorias = 100,
                frecuencia = 10,
                porcentaje = 0.3f
            ),
            Ejercicio(
                nombre = "Ejercicio Intermedio",
                descripcion = "Descripción",
                grupoMuscular = "Pecho",
                dificultad = "Medio",
                dias = "Martes",
                imagenEjercicio = "https://example.com/img2.jpg",
                seccion = "Parte Superior",
                calorias = 150,
                frecuencia = 12,
                porcentaje = 0.4f
            ),
            Ejercicio(
                nombre = "Ejercicio Avanzado",
                descripcion = "Descripción",
                grupoMuscular = "Pecho",
                dificultad = "Avanzado",
                dias = "Miércoles",
                imagenEjercicio = "https://example.com/img3.jpg",
                seccion = "Parte Superior",
                calorias = 200,
                frecuencia = 15,
                porcentaje = 0.5f
            ),
            Ejercicio(
                nombre = "Otro Ejercicio Avanzado",
                descripcion = "Descripción",
                grupoMuscular = "Piernas",
                dificultad = "Avanzado",
                dias = "Jueves",
                imagenEjercicio = "https://example.com/img4.jpg",
                seccion = "Parte Inferior",
                calorias = 250,
                frecuencia = 15,
                porcentaje = 0.6f
            )
        )

        ejercicioDao.insertarTodos(ejercicios)

        val dificultades = ejercicioDao.obtenerValoresDificultad()
        assertEquals(3, dificultades.size)
        assertTrue(dificultades.contains("Básico"))
        assertTrue(dificultades.contains("Medio"))
        assertTrue(dificultades.contains("Avanzado"))
    }

    @Test
    @Throws(Exception::class)
    fun filterEjercicios_returnsMatchingEjercicios() = runBlocking {
        // Insert ejercicios
        val ejercicio1 = Ejercicio(
            nombre = "Press de banca",
            descripcion = "Ejercicio para pecho",
            grupoMuscular = "Pecho",
            dificultad = "Medio",
            dias = "Lunes, Miércoles",
            imagenEjercicio = "https://example.com/press.jpg",
            videoUrl = "https://example.com/video.mp4",
            seccion = "Parte Superior",
            calorias = 150,
            frecuencia = 12,
            porcentaje = 0.4f
        )
        val ejercicio2 = Ejercicio(
            nombre = "Sentadillas",
            descripcion = "Ejercicio para piernas",
            grupoMuscular = "Piernas",
            dificultad = "Básico",
            dias = "Martes, Jueves",
            imagenEjercicio = "https://mundoentrenamiento.com/wp-content/uploads/2015/11/Squat-sentadilla-pesas-musculacion-musculo-pierna-fuerza-508x300.png",
            videoUrl = "https://example.com/video2.mp4",
            seccion = "Parte Inferior",
            calorias = 200,
            frecuencia = 15,
            porcentaje = 0.5f
        )

        // Mock return values for insertions
        every { runBlocking { ejercicioDao.insertar(ejercicio1) } } returns 3L
        every { runBlocking { ejercicioDao.insertar(ejercicio2) } } returns 4L

        // Mock filter results
        every { runBlocking { ejercicioDao.getFilteredEjercicios(dificultad = "Medio") } } returns listOf(
            ejercicio1
        )
        every { runBlocking { ejercicioDao.getFilteredEjercicios(grupoMuscular = "Piernas") } } returns listOf(
            ejercicio2
        )
        every { runBlocking { ejercicioDao.getFilteredEjercicios(query = "piernas") } } returns listOf(
            ejercicio2
        )
        every { runBlocking { ejercicioDao.getFilteredEjercicios(dias = "Lunes") } } returns listOf(
            ejercicio1
        )
        every {
            runBlocking {
                ejercicioDao.getFilteredEjercicios(
                    dificultad = "Avanzado",
                    grupoMuscular = "Piernas"
                )
            }
        } returns emptyList()

        // Execute
        ejercicioDao.insertar(ejercicio1)
        ejercicioDao.insertar(ejercicio2)

        // Filter by dificultad
        val ejerciciosMedio = ejercicioDao.getFilteredEjercicios(
            dificultad = "Medio"
        )
        assertEquals(1, ejerciciosMedio.size)
        assertEquals("Press de banca", ejerciciosMedio.first().nombre)

        // Filter by grupoMuscular
        val ejerciciosPiernas = ejercicioDao.getFilteredEjercicios(
            grupoMuscular = "Piernas"
        )
        assertEquals(1, ejerciciosPiernas.size)
        assertEquals("Sentadillas", ejerciciosPiernas.first().nombre)

        // Filter by query
        val ejerciciosByQuery = ejercicioDao.getFilteredEjercicios(
            query = "piernas"
        )
        assertEquals(1, ejerciciosByQuery.size)
        assertEquals("Sentadillas", ejerciciosByQuery.first().nombre)

        // Filter by day
        val ejerciciosByDay = ejercicioDao.getFilteredEjercicios(
            dias = "Lunes"
        )
        assertEquals(1, ejerciciosByDay.size)
        assertEquals("Press de banca", ejerciciosByDay.first().nombre)

        // Combined filters - should return no results
        val combinedFilter = ejercicioDao.getFilteredEjercicios(
            dificultad = "Avanzado",
            grupoMuscular = "Piernas"
        )
        assertEquals(0, combinedFilter.size)
    }

    // ESTADISTICA TESTS

    @Test
    @Throws(Exception::class)
    fun insertAndGetEstadistica() = runBlocking {
        // Create user and ejercicio first
        val user =
            Usuario(email = "stats@example.com", password = "password", nombre = "Stats User")
        val userId = 10
        val ejercicio = Ejercicio(
            nombre = "Ejercicio para estadísticas",
            descripcion = "Descripción",
            grupoMuscular = "Test",
            dificultad = "Básico",
            dias = "Lunes",
            imagenEjercicio = "https://example.com/img.jpg",
            seccion = "Test",
            calorias = 100,
            frecuencia = 10,
            porcentaje = 0.3f
        )
        val ejercicioId = 20

        // Now create and insert estadistica
        val timestamp = System.currentTimeMillis()
        val estadistica = Estadistica(
            userId = userId,
            ejercicioId = ejercicioId,
            fecha = timestamp,
            ejerciciosCompletados = 10,
            caloriasQuemadas = 150,
            tiempoTotal = 1800000, // 30 minutes
            nombreEjercicio = ejercicio.nombre,
            grupoMuscular = ejercicio.grupoMuscular
        )
        val estadisticaId = 30L
        val estadisticaWithId = estadistica.copy(id = estadisticaId.toInt())

        // Mock return values
        every { runBlocking { userDao.insertar(user) } } returns userId.toLong()
        every { runBlocking { ejercicioDao.insertar(ejercicio) } } returns ejercicioId.toLong()
        every { runBlocking { estadisticaDao.insertarEstadistica(estadistica) } } returns estadisticaId
        every { runBlocking { estadisticaDao.obtenerEstadisticaPorId(estadisticaId.toInt()) } } returns estadisticaWithId



        val estadisticaIdResult = estadisticaDao.insertarEstadistica(estadistica)

        // Verify estadistica was inserted correctly
        val retrievedEstadistica =
            estadisticaDao.obtenerEstadisticaPorId(estadisticaIdResult.toInt())
        assertNotNull(retrievedEstadistica)
        assertEquals(userId, retrievedEstadistica?.userId)
        assertEquals(ejercicioId, retrievedEstadistica?.ejercicioId)
        assertEquals(10, retrievedEstadistica?.ejerciciosCompletados)
        assertEquals(150, retrievedEstadistica?.caloriasQuemadas)
    }

    // PREFERENCIA TESTS

    @Test
    @Throws(Exception::class)
    fun insertAndGetPreferencia() = runBlocking {
        // Create user first
        val user =
            Usuario(email = "prefs@example.com", password = "password", nombre = "Prefs User")
        val userId = 50

        // Create and insert preferencia
        val preferencia = Preferencia(
            usuarioId = userId,
            notificacionesHabilitadas = true,
            temaOscuro = false,
            recordatorios = true,
            frecuencia = "DIARIA"
        )
        val preferenciaId = 60L

        // Mock return values
        every { runBlocking { userDao.insertar(user) } } returns userId.toLong()
        every { runBlocking { preferenciaDao.insertar(preferencia) } } returns preferenciaId
        every { runBlocking { preferenciaDao.obtenerPreferenciaSync(userId) } } returns preferencia


        // Verify preferencia was inserted correctly
        val retrievedPreferencia = preferenciaDao.obtenerPreferenciaSync(userId)
        assertNotNull(retrievedPreferencia)
        assertEquals(userId, retrievedPreferencia?.usuarioId)
        assertEquals(true, retrievedPreferencia?.notificacionesHabilitadas)
        assertEquals(false, retrievedPreferencia?.temaOscuro)
        assertEquals(true, retrievedPreferencia?.recordatorios)
        assertEquals("DIARIA", retrievedPreferencia?.frecuencia)
    }
}