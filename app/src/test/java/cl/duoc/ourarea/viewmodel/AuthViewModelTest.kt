package cl.duoc.ourarea.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import cl.duoc.ourarea.data.PasswordHasher
import cl.duoc.ourarea.data.UserPreferencesManager
import cl.duoc.ourarea.model.User
import cl.duoc.ourarea.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests unitarios para AuthViewModel
 * Verifica login, registro, logout y persistencia de sesión
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var userRepository: UserRepository
    private lateinit var preferencesManager: UserPreferencesManager
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk(relaxed = true)
        preferencesManager = mockk(relaxed = true)

        // Mock para flows iniciales
        every { preferencesManager.isLoggedInFlow } returns flowOf(false)
        every { preferencesManager.userIdFlow } returns flowOf(null)

        viewModel = AuthViewModel(userRepository, preferencesManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login exitoso actualiza currentUser y isLoggedIn`() = runTest {
        // Given
        val email = "test@example.com"
        val plainPassword = "password123"
        val hashedPassword = PasswordHasher.hashPassword(plainPassword)
        val user = User(1, email, hashedPassword, "Test User")

        coEvery { userRepository.getUserByEmail(email) } returns user
        coEvery { preferencesManager.saveUserSession(any(), any(), any()) } returns Unit

        // When
        viewModel.login(email, plainPassword)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(user, viewModel.currentUser.value)
        assertTrue(viewModel.isLoggedIn.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)

        coVerify { preferencesManager.saveUserSession(user.id, user.name, user.email) }
    }

    @Test
    fun `login con credenciales incorrectas muestra error`() = runTest {
        // Given
        val email = "test@example.com"
        val plainPassword = "wrongPassword"

        coEvery { userRepository.getUserByEmail(email) } returns null

        // When
        viewModel.login(email, plainPassword)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.currentUser.value)
        assertFalse(viewModel.isLoggedIn.value)
        assertEquals("Credenciales incorrectas", viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `login con contraseña incorrecta pero usuario existente`() = runTest {
        // Given
        val email = "test@example.com"
        val correctPassword = "correctPassword"
        val wrongPassword = "wrongPassword"
        val hashedPassword = PasswordHasher.hashPassword(correctPassword)
        val user = User(1, email, hashedPassword, "Test User")

        coEvery { userRepository.getUserByEmail(email) } returns user

        // When
        viewModel.login(email, wrongPassword)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.currentUser.value)
        assertFalse(viewModel.isLoggedIn.value)
        assertEquals("Credenciales incorrectas", viewModel.error.value)
    }

    @Test
    fun `registro exitoso crea usuario y guarda sesión`() = runTest {
        // Given
        val name = "New User"
        val email = "new@example.com"
        val password = "password123"
        val userId = 5L

        coEvery { userRepository.getUserByEmail(email) } returns null
        coEvery { userRepository.insertUser(any()) } returns userId
        coEvery { preferencesManager.saveUserSession(any(), any(), any()) } returns Unit

        // When
        viewModel.register(name, email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNotNull(viewModel.currentUser.value)
        assertEquals(userId.toInt(), viewModel.currentUser.value?.id)
        assertEquals(name, viewModel.currentUser.value?.name)
        assertEquals(email, viewModel.currentUser.value?.email)
        assertTrue(viewModel.isLoggedIn.value)
        assertNull(viewModel.error.value)

        coVerify { userRepository.insertUser(any()) }
        coVerify { preferencesManager.saveUserSession(userId.toInt(), name, email) }
    }

    @Test
    fun `registro con email existente muestra error`() = runTest {
        // Given
        val name = "New User"
        val email = "existing@example.com"
        val password = "password123"
        val existingUser = User(1, email, "hashedPass", "Existing User")

        coEvery { userRepository.getUserByEmail(email) } returns existingUser

        // When
        viewModel.register(name, email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.currentUser.value)
        assertFalse(viewModel.isLoggedIn.value)
        assertEquals("El email ya está registrado", viewModel.error.value)

        coVerify(exactly = 0) { userRepository.insertUser(any()) }
    }

    @Test
    fun `registro hashea la contraseña antes de guardar`() = runTest {
        // Given
        val name = "New User"
        val email = "new@example.com"
        val plainPassword = "password123"
        val userId = 5L

        coEvery { userRepository.getUserByEmail(email) } returns null
        coEvery { userRepository.insertUser(any()) } returns userId
        coEvery { preferencesManager.saveUserSession(any(), any(), any()) } returns Unit

        // When
        viewModel.register(name, email, plainPassword)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            userRepository.insertUser(match { user ->
                user.password != plainPassword && // No es texto plano
                user.password.contains(":") && // Tiene formato salt:hash
                PasswordHasher.verifyPassword(plainPassword, user.password) // Se puede verificar
            })
        }
    }

    @Test
    fun `logout limpia sesión y estado`() = runTest {
        // Given
        val user = User(1, "test@example.com", "hashedPass", "Test User")
        coEvery { preferencesManager.clearUserSession() } returns Unit

        // Simular usuario logueado
        viewModel.currentUser.test {
            viewModel.isLoggedIn.test {
                // When
                viewModel.logout()
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                assertNull(viewModel.currentUser.value)
                assertFalse(viewModel.isLoggedIn.value)
                assertNull(viewModel.error.value)

                coVerify { preferencesManager.clearUserSession() }

                cancelAndConsumeRemainingEvents()
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `checkEmailExists actualiza emailExists cuando existe`() = runTest {
        // Given
        val email = "existing@example.com"
        val existingUser = User(1, email, "hashedPass", "Existing")

        coEvery { userRepository.getUserByEmail(email) } returns existingUser

        // When
        viewModel.checkEmailExists(email)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.emailExists.value)
        assertEquals("Este email ya está registrado", viewModel.emailCheckError.value)
    }

    @Test
    fun `checkEmailExists actualiza emailExists cuando no existe`() = runTest {
        // Given
        val email = "new@example.com"

        coEvery { userRepository.getUserByEmail(email) } returns null

        // When
        viewModel.checkEmailExists(email)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.emailExists.value)
        assertNull(viewModel.emailCheckError.value)
    }

    @Test
    fun `clearEmailCheck limpia estado de verificación`() = runTest {
        // Given (previamente verificado)
        val email = "test@example.com"
        coEvery { userRepository.getUserByEmail(email) } returns mockk()
        viewModel.checkEmailExists(email)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearEmailCheck()

        // Then
        assertFalse(viewModel.emailExists.value)
        assertNull(viewModel.emailCheckError.value)
    }

    @Test
    fun `clearError limpia mensaje de error`() = runTest {
        // Given
        val email = "test@example.com"
        coEvery { userRepository.getUserByEmail(email) } returns null
        viewModel.login(email, "wrongPassword")
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.error.value)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.error.value)
    }

    @Test
    fun `isLoading se activa y desactiva correctamente`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"

        coEvery { userRepository.getUserByEmail(email) } returns null

        // When
        viewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.isLoading.value) // Debe estar en false después de completar
    }

    @Test
    fun `registro con userId 0 o negativo muestra error`() = runTest {
        // Given
        val name = "New User"
        val email = "new@example.com"
        val password = "password123"

        coEvery { userRepository.getUserByEmail(email) } returns null
        coEvery { userRepository.insertUser(any()) } returns 0L // Error de inserción

        // When
        viewModel.register(name, email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.currentUser.value)
        assertFalse(viewModel.isLoggedIn.value)
        assertEquals("Error al crear la cuenta", viewModel.error.value)
    }
}
