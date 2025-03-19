package com.amarina.powergym.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amarina.powergym.database.dao.EjercicioDao
import com.amarina.powergym.database.dao.UsuarioDao
import com.amarina.powergym.database.entities.Ejercicio
import com.amarina.powergym.database.entities.Usuario
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class PowerGymDatabaseTest {

    private lateinit var userDao: UsuarioDao
    private lateinit var ejercicioDao: EjercicioDao
    private lateinit var db: PowerGymDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, PowerGymDatabase::class.java
        ).allowMainThreadQueries().build()
        userDao = db.userDao()
        ejercicioDao = db.ejercicioDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetUser() = runBlocking {
        val user = Usuario(
            email = "test@example.com",
            password = "password123",
            nombre = "Test User",
            rol = "usuario"
        )
        val userId = userDao.insert(user)

        val retrievedUser = userDao.getUserById(userId.toInt())

        assertNotNull(retrievedUser)
        assertEquals(user.email, retrievedUser?.email)
        assertEquals(user.nombre, retrievedUser?.nombre)
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
        userDao.insert(user)

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
        userDao.insert(user)

        val retrievedUser = userDao.login("test@example.com", "wrongpassword")

        assertNull(retrievedUser)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetEjercicio() = runBlocking {
        val ejercicio = Ejercicio(
            nombre = "Press de banca",
            descripcion = "Ejercicio para pecho",
            grupoMuscular = "Pecho",
            dificultad = "Medio",
            dias = "Lunes, Miércoles",
            urlEjercicio = "https://example.com/press.jpg",
            videoUrl = "https://example.com/video.mp4",
            seccion = "Parte Superior",
            calorias = 150
        )
        val id = ejercicioDao.insert(ejercicio)

        val retrievedEjercicio = ejercicioDao.getEjercicioById(id.toInt())

        assertNotNull(retrievedEjercicio)
        assertEquals(ejercicio.nombre, retrievedEjercicio?.nombre)
        assertEquals(ejercicio.grupoMuscular, retrievedEjercicio?.grupoMuscular)
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
            urlEjercicio = "https://example.com/press.jpg",
            videoUrl = "https://example.com/video.mp4",
            seccion = "Parte Superior",
            calorias = 150
        )
        val ejercicio2 = Ejercicio(
            nombre = "Sentadillas",
            descripcion = "Ejercicio para piernas",
            grupoMuscular = "Piernas",
            dificultad = "Básico",
            dias = "Martes, Jueves",
            urlEjercicio = "https://mundoentrenamiento.com/wp-content/uploads/2015/11/Squat-sentadilla-pesas-musculacion-musculo-pierna-fuerza-508x300.png",
            videoUrl = "https://example.com/video2.mp4",
            seccion = "Parte Inferior",
            calorias = 200
        )
        ejercicioDao.insert(ejercicio1)
        ejercicioDao.insert(ejercicio2)

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
    }
}
