package cl.duoc.ourarea.repository

import cl.duoc.ourarea.model.User
import cl.duoc.ourarea.model.UserDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para UserRepository
 * Verifica la correcta delegación de operaciones al DAO
 */
class UserRepositoryTest {

    private lateinit var userDao: UserDao
    private lateinit var userRepository: UserRepository

    @Before
    fun setup() {
        userDao = mockk()
        userRepository = UserRepository(userDao)
    }

    @Test
    fun `getUserByCredentials delega al DAO correctamente`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "hashedPassword123"
        val expectedUser = User(
            id = 1,
            email = email,
            password = password,
            name = "Test User"
        )
        coEvery { userDao.getUserByCredentials(email, password) } returns expectedUser

        // When
        val result = userRepository.getUserByCredentials(email, password)

        // Then
        assertEquals(expectedUser, result)
        coVerify(exactly = 1) { userDao.getUserByCredentials(email, password) }
    }

    @Test
    fun `getUserByCredentials retorna null cuando no existe`() = runTest {
        // Given
        val email = "noexiste@example.com"
        val password = "wrongPassword"
        coEvery { userDao.getUserByCredentials(email, password) } returns null

        // When
        val result = userRepository.getUserByCredentials(email, password)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { userDao.getUserByCredentials(email, password) }
    }

    @Test
    fun `getUserByEmail delega al DAO correctamente`() = runTest {
        // Given
        val email = "test@example.com"
        val expectedUser = User(
            id = 1,
            email = email,
            password = "hashedPassword",
            name = "Test User"
        )
        coEvery { userDao.getUserByEmail(email) } returns expectedUser

        // When
        val result = userRepository.getUserByEmail(email)

        // Then
        assertEquals(expectedUser, result)
        coVerify(exactly = 1) { userDao.getUserByEmail(email) }
    }

    @Test
    fun `getUserById delega al DAO correctamente`() = runTest {
        // Given
        val userId = 1
        val expectedUser = User(
            id = userId,
            email = "test@example.com",
            password = "hashedPassword",
            name = "Test User"
        )
        coEvery { userDao.getUserById(userId) } returns expectedUser

        // When
        val result = userRepository.getUserById(userId)

        // Then
        assertEquals(expectedUser, result)
        coVerify(exactly = 1) { userDao.getUserById(userId) }
    }

    @Test
    fun `insertUser delega al DAO y retorna ID`() = runTest {
        // Given
        val user = User(
            id = 0, // ID temporal, será reemplazado por el DAO
            email = "new@example.com",
            password = "hashedPassword",
            name = "New User"
        )
        val expectedId = 5L
        coEvery { userDao.insertUser(user) } returns expectedId

        // When
        val result = userRepository.insertUser(user)

        // Then
        assertEquals(expectedId, result)
        coVerify(exactly = 1) { userDao.insertUser(user) }
    }

    @Test
    fun `updateUser delega al DAO correctamente`() = runTest {
        // Given
        val user = User(
            id = 1,
            email = "updated@example.com",
            password = "newHashedPassword",
            name = "Updated User"
        )
        coEvery { userDao.updateUser(user) } returns Unit

        // When
        userRepository.updateUser(user)

        // Then
        coVerify(exactly = 1) { userDao.updateUser(user) }
    }

    @Test
    fun `getAllUsers delega al DAO y retorna Flow`() = runTest {
        // Given
        val usersList = listOf(
            User(1, "user1@example.com", "pass1", "User 1"),
            User(2, "user2@example.com", "pass2", "User 2"),
            User(3, "user3@example.com", "pass3", "User 3")
        )
        val usersFlow = flowOf(usersList)
        coEvery { userDao.getAllUsers() } returns usersFlow

        // When
        val result = userRepository.getAllUsers()

        // Then
        result.collect { users ->
            assertEquals(usersList, users)
        }
        coVerify(exactly = 1) { userDao.getAllUsers() }
    }
}
