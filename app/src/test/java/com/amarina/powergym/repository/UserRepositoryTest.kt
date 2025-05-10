package com.amarina.powergym.repository

import com.amarina.powergym.database.dao.UsuarioDao
import com.amarina.powergym.database.entities.Usuario
import com.amarina.powergym.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class UserRepositoryTest {

    private lateinit var userDao: UsuarioDao
    private lateinit var userRepository: UserRepository

    @Before
    fun setup() {
        userDao = mockk()
        userRepository = UserRepository(userDao, mockk())
    }

    @Test
    fun login_withValidCredentials_returnsUser() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val expectedUser = Usuario(
            id = 1,
            email = email,
            password = password,
            nombre = "Test User",
            rol = "usuario"
        )

        coEvery { userDao.login(email, password) } returns expectedUser

        val result = userRepository.login(email, password)

        coVerify { userDao.login(email, password) }
        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
    }

    @Test
    fun login_withInvalidCredentials_returnsFailure() = runTest {
        val email = "test@example.com"
        val password = "wrongpassword"

        coEvery { userDao.login(email, password) } returns null

        val result = userRepository.login(email, password)

        coVerify { userDao.login(email, password) }
        assertTrue(result.isFailure)
        assertEquals("Credenciales incorrectas", result.exceptionOrNull()?.message)
    }

    @Test
    fun register_withNewEmail_returnsUserId() = runTest {
        val email = "new@example.com"
        val password = "password123"
        val name = "New User"

        coEvery { userDao.obtenerUsuarioPorEmail(email) } returns null
        coEvery { userDao.insertar(any()) } returns 42L

        val result = userRepository.register(email, password, name)

        coVerify { userDao.obtenerUsuarioPorEmail(email) }
        coVerify { userDao.insertar(any()) }
        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull())
    }

    @Test
    fun register_withExistingEmail_returnsFailure() = runTest {
        val email = "existing@example.com"
        val password = "password123"
        val name = "Existing User"
        val existingUser = Usuario(
            id = 1,
            email = email,
            password = "oldpassword",
            nombre = "Old User",
            rol = "usuario"
        )

        coEvery { userDao.obtenerUsuarioPorEmail(email) } returns existingUser

        val result = userRepository.register(email, password, name)

        coVerify { userDao.obtenerUsuarioPorEmail(email) }
        coVerify(exactly = 0) { userDao.insertar(any()) }
        assertTrue(result.isFailure)
        assertEquals("El email ya está registrado", result.exceptionOrNull()?.message)
    }
}