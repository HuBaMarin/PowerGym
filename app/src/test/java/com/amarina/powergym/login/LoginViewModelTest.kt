package com.amarina.powergym.login

import com.amarina.powergym.database.entities.Usuario
import com.amarina.powergym.repository.UserRepository
import com.amarina.powergym.ui.viewmodel.auth.LoginViewModel
import com.amarina.powergym.utils.SessionManager
import com.amarina.powergym.utils.crypto.AdminAuthManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var userRepository: UserRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var adminAuthManager: AdminAuthManager
    private lateinit var viewModel: LoginViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk(relaxed = true)
        sessionManager = mockk(relaxed = true)
        adminAuthManager = mockk(relaxed = true)
        viewModel = LoginViewModel(userRepository, sessionManager, adminAuthManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when login with valid credentials, then success state emitted`() = runTest {
        // Given
        val email = "user@example.com"
        val password = "password123"
        val user = Usuario(
            id = 1,
            email = email,
            password = password,
            nombre = "Test User",
            rol = "usuario"
        )
        every { sessionManager.estaCuentaBloqueada() } returns false
        coEvery { userRepository.iniciarSesion(email, password) } returns Result.success(user)

        // When
        viewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.loginState.value
        assertTrue(state is LoginViewModel.LoginState.Success)
        verify { sessionManager.registrarIntentoSesion(exitoso = true) }
        verify { sessionManager.guardarSesionUsuario(user) }
    }

    @Test
    fun `when login with admin email, then redirect state emitted`() = runTest {
        // Given
        val email = "admin@powergym.com"
        val password = "admin123"

        every { sessionManager.estaCuentaBloqueada() } returns false

        // When
        viewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.loginState.value
        assertTrue(state is LoginViewModel.LoginState.AdminRedirect)
        assertEquals(email, (state as LoginViewModel.LoginState.AdminRedirect).email)
        assertEquals(password, state.password)
    }

    @Test
    fun `when login with invalid credentials, then error state emitted`() = runTest {
        // Given
        val email = "user@example.com"
        val password = "wrongpassword"
        val errorMessage = "Credenciales incorrectas"

        every { sessionManager.estaCuentaBloqueada() } returns false
        coEvery { userRepository.iniciarSesion(email, password) } returns Result.failure(
            Exception(
                errorMessage
            )
        )

        // When
        viewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.loginState.value
        assertTrue(state is LoginViewModel.LoginState.Error)
        assertEquals(errorMessage, (state as LoginViewModel.LoginState.Error).message)
        verify { sessionManager.registrarIntentoSesion(exitoso = false) }
    }

    @Test
    fun `when account is locked, then locked state emitted`() = runTest {
        // Given
        val email = "user@example.com"
        val password = "password123"

        every { sessionManager.estaCuentaBloqueada() } returns true

        // When
        viewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.loginState.value
        assertTrue(state is LoginViewModel.LoginState.Locked)
    }

    @Test
    fun `when navigateToRegister called, then register navigation event emitted`() = runTest {
        // When
        viewModel.navigateToRegister()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val event = viewModel.navigationEvents.first()
        assertTrue(event is LoginViewModel.NavigationEvent.ToRegister)
    }
}