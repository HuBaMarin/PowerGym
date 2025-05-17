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
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(JUnit4::class)
class UserRepositoryTest {

    private lateinit var userDao: UsuarioDao
    private lateinit var userRepository: UserRepository

    @Before
    fun preparar() {
        userDao = mockk()
        userRepository = UserRepository(userDao, mockk())
    }

    @Test
    fun iniciarSesion_conCredencialesValidas_devuelveUsuario() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val usuarioEsperado = Usuario(
            id = 1,
            email = email,
            password = password,
            nombre = "Test User",
            rol = "usuario"
        )

        coEvery { userDao.iniciarSesion(email, password) } returns usuarioEsperado

        val resultado = userRepository.iniciarSesion(email, password)

        coVerify { userDao.iniciarSesion(email, password) }
        assertTrue(resultado.isSuccess)
        assertEquals(usuarioEsperado, resultado.getOrNull())
    }

    @Test
    fun iniciarSesion_conCredencialesInvalidas_devuelveFallo() = runTest {
        val email = "test@example.com"
        val password = "wrongpassword"

        coEvery { userDao.iniciarSesion(email, password) } returns null

        val resultado = userRepository.iniciarSesion(email, password)

        coVerify { userDao.iniciarSesion(email, password) }
        assertTrue(resultado.isFailure)
        assertEquals("Credenciales incorrectas", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun registrar_conEmailNuevo_devuelveIdUsuario() = runTest {
        val email = "new@example.com"
        val password = "password123"
        val nombre = "New User"

        coEvery { userDao.obtenerUsuarioPorEmail(email) } returns null
        coEvery { userDao.insertar(any()) } returns 42L

        val resultado = userRepository.registrar(email, password, nombre)

        coVerify { userDao.obtenerUsuarioPorEmail(email) }
        coVerify { userDao.insertar(any()) }
        assertTrue(resultado.isSuccess)
        assertEquals(42L, resultado.getOrNull())
    }

    @Test
    fun registrar_conEmailExistente_devuelveFallo() = runTest {
        val email = "existing@example.com"
        val password = "password123"
        val nombre = "Existing User"
        val usuarioExistente = Usuario(
            id = 1,
            email = email,
            password = "oldpassword",
            nombre = "Old User",
            rol = "usuario"
        )

        coEvery { userDao.obtenerUsuarioPorEmail(email) } returns usuarioExistente

        val resultado = userRepository.registrar(email, password, nombre)

        coVerify { userDao.obtenerUsuarioPorEmail(email) }
        coVerify(exactly = 0) { userDao.insertar(any()) }
        assertTrue(resultado.isFailure)
        assertEquals("El email ya está registrado", resultado.exceptionOrNull()?.message)
    }
}