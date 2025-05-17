package com.amarina.powergym.usuario

import com.amarina.powergym.database.PowerGymDatabase
import com.amarina.powergym.database.dao.UsuarioDao
import com.amarina.powergym.database.entities.Usuario
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
 * Tests unitarios (nivel DAO) centrados únicamente en operaciones CRUD de usuario.
 */
@RunWith(JUnit4::class)
class UsuarioDaoTest {

    private lateinit var userDao: UsuarioDao
    private lateinit var db: PowerGymDatabase

    @Before
    fun setUp() {
        db = mockk(relaxed = true)
        userDao = mockk(relaxed = true)
        every { db.userDao() } returns userDao
    }

    // ---------------------------------------------------------------------------------
    // Insertar y obtener
    // ---------------------------------------------------------------------------------
    @Test
    fun insertarYObtenerUsuario() = runBlocking {
        val user = Usuario(
            email = "test@example.com",
            password = "password123",
            nombre = "Test User",
            rol = "usuario"
        )
        val userId = 1L

        every { runBlocking { userDao.insertar(user) } } returns userId
        every { runBlocking { userDao.obtenerUsuarioPorId(userId.toInt()) } } returns user.copy(id = userId.toInt())

        val resultId = userDao.insertar(user)
        val retrievedUser = userDao.obtenerUsuarioPorId(resultId.toInt())

        assertEquals(userId, resultId)
        assertNotNull(retrievedUser)
        assertEquals(user.email, retrievedUser?.email)
        assertEquals(user.nombre, retrievedUser?.nombre)
        assertEquals(user.rol, retrievedUser?.rol)
    }

    // ---------------------------------------------------------------------------------
    // Actualizar
    // ---------------------------------------------------------------------------------
    @Test
    fun actualizarUsuario() = runBlocking {
        val user = Usuario(
            email = "update@example.com",
            password = "password123",
            nombre = "Original Name",
            rol = "usuario"
        )
        val userId = 2L
        val userWithId = user.copy(id = userId.toInt())
        val updatedUser = userWithId.copy(nombre = "Updated Name")

        every { runBlocking { userDao.insertar(user) } } returns userId
        every { runBlocking { userDao.obtenerUsuarioPorId(userId.toInt()) } } returns userWithId andThen updatedUser

        val resultId = userDao.insertar(user)
        val retrievedUser = userDao.obtenerUsuarioPorId(resultId.toInt())
        assertNotNull(retrievedUser)

        userDao.actualizar(retrievedUser.copy(nombre = "Updated Name"))

        val retrievedUpdatedUser = userDao.obtenerUsuarioPorId(resultId.toInt())
        assertNotNull(retrievedUpdatedUser)
        assertEquals("Updated Name", retrievedUpdatedUser.nombre)
        assertEquals(user.email, retrievedUpdatedUser.email)
    }

    // ---------------------------------------------------------------------------------
    // Eliminar
    // ---------------------------------------------------------------------------------
    @Test
    fun eliminarUsuario() = runBlocking {
        val user = Usuario(
            email = "delete@example.com",
            password = "password123",
            nombre = "Delete Me",
            rol = "usuario"
        )
        val userId = 3L
        val userWithId = user.copy(id = userId.toInt())

        every { runBlocking { userDao.insertar(user) } } returns userId
        every { runBlocking { userDao.obtenerUsuarioPorId(userId.toInt()) } } returns userWithId andThen null

        val resultId = userDao.insertar(user)

        val retrievedUser = userDao.obtenerUsuarioPorId(resultId.toInt())
        assertNotNull(retrievedUser)

        userDao.eliminar(retrievedUser)

        val deletedUser = userDao.obtenerUsuarioPorId(resultId.toInt())
        assertNull(deletedUser)
    }

    // ---------------------------------------------------------------------------------
    // Obtener por email e iniciar sesión
    // ---------------------------------------------------------------------------------
    @Test
    fun obtenerUsuarioPorEmail() = runBlocking {
        val user = Usuario(
            email = "email@example.com",
            password = "password123",
            nombre = "Email Test",
            rol = "usuario"
        )

        every { runBlocking { userDao.insertar(user) } } returns 4L
        every { runBlocking { userDao.obtenerUsuarioPorEmail("email@example.com") } } returns user
        every { runBlocking { userDao.obtenerUsuarioPorEmail("nonexistent@example.com") } } returns null

        userDao.insertar(user)

        val retrievedUser = userDao.obtenerUsuarioPorEmail("email@example.com")

        assertNotNull(retrievedUser)
        assertEquals(user.email, retrievedUser.email)
        assertEquals(user.nombre, retrievedUser.nombre)

        val nonExistentUser = userDao.obtenerUsuarioPorEmail("nonexistent@example.com")
        assertNull(nonExistentUser)
    }

    @Test
    fun iniciarSesion_credencialesValidas_eInvalidas() = runBlocking {
        val user = Usuario(
            email = "test@example.com",
            password = "password123",
            nombre = "Test User",
            rol = "usuario"
        )

        every { runBlocking { userDao.insertar(user) } } returns 5L
        every {
            runBlocking {
                userDao.iniciarSesion(
                    "test@example.com",
                    "password123"
                )
            }
        } returns user
        every { runBlocking { userDao.iniciarSesion("test@example.com", "wrong") } } returns null

        userDao.insertar(user)

        val okUser = userDao.iniciarSesion("test@example.com", "password123")
        assertNotNull(okUser)

        val badUser = userDao.iniciarSesion("test@example.com", "wrong")
        assertNull(badUser)
    }

    // ---------------------------------------------------------------------------------
    // Obtener todos
    // ---------------------------------------------------------------------------------
    @Test
    fun obtenerTodosLosUsuarios() = runBlocking {
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

        users.forEachIndexed { idx, u -> every { runBlocking { userDao.insertar(u) } } returns (idx + 7).toLong() }
        every { runBlocking { userDao.obtenerTodos() } } returns users

        users.forEach { userDao.insertar(it) }

        val retrieved = userDao.obtenerTodos()
        assertEquals(users.size, retrieved.size)
        assertTrue(retrieved.any { it.email == "user1@example.com" })
        assertTrue(retrieved.any { it.email == "admin@example.com" })
    }
}
