package com.amarina.powergym.login

import com.amarina.powergym.repository.UserRepository
import com.amarina.powergym.ui.viewmodel.auth.RegisterViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: RegisterViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk(relaxed = true)
        viewModel = RegisterViewModel(userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when register with valid data, then success state emitted`() = runTest {
        // Given
        val email = "new@example.com"
        val password = "password123"
        val name = "New User"

        coEvery { userRepository.registrar(email, password, name) } returns Result.success(42L)

        // When
        viewModel.register(email, password, name)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.registerState.value
        assertTrue(state is RegisterViewModel.RegisterState.Success)

        // Verify navigation event to login screen
        val navigationEvent = viewModel.navigationEvents.first()
        assertTrue(navigationEvent is RegisterViewModel.NavigationEvent.ToLogin)

        // Verify repository was called with correct parameters
        coVerify { userRepository.registrar(email, password, name) }
    }

    @Test
    fun `when register with existing email, then error state emitted`() = runTest {
        // Given
        val email = "existing@example.com"
        val password = "password123"
        val name = "Existing User"
        val errorMessage = "El email ya está registrado"

        coEvery {
            userRepository.registrar(
                email,
                password,
                name
            )
        } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.register(email, password, name)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.registerState.value
        assertTrue(state is RegisterViewModel.RegisterState.Error)

        // Verify repository was called with correct parameters
        coVerify { userRepository.registrar(email, password, name) }
    }

    @Test
    fun `when navigateBack called, then back navigation event emitted`() = runTest {
        // When
        viewModel.navigateBack()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val event = viewModel.navigationEvents.first()
        assertTrue(event is RegisterViewModel.NavigationEvent.Back)
    }
}