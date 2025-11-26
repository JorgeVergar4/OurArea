package cl.duoc.ourarea.repository

import android.util.Log
import cl.duoc.ourarea.api.RetrofitClient
import cl.duoc.ourarea.api.models.LoginRequest
import cl.duoc.ourarea.api.models.SignupRequest
import cl.duoc.ourarea.api.models.toUser
import cl.duoc.ourarea.model.User
import cl.duoc.ourarea.model.UserDao
import kotlinx.coroutines.flow.Flow

/**
 * UserRepository - Manages user data from both local (Room) and remote (Xano) sources
 * Implements offline-first architecture:
 * - Local operations work without internet
 * - Remote sync happens when network is available
 * - Local database is the single source of truth
 *
 * Uses Xano Auth API: https://x8ki-letl-twmt.n7.xano.io/api:p4Kx6qbK/
 */
class UserRepository(
    private val userDao: UserDao,
    private val apiService: cl.duoc.ourarea.api.XanoAuthApiService = RetrofitClient.authApiService
) {

    companion object {
        private const val TAG = "UserRepository"
    }

    // ========== LOCAL OPERATIONS (Room Database) ==========

    suspend fun getUserByCredentials(email: String, password: String): User? =
        userDao.getUserByCredentials(email, password)

    suspend fun getUserByEmail(email: String): User? =
        userDao.getUserByEmail(email)

    suspend fun getUserById(userId: Int): User? =
        userDao.getUserById(userId)

    suspend fun insertUser(user: User): Long =
        userDao.insertUser(user)

    suspend fun updateUser(user: User) =
        userDao.updateUser(user)

    fun getAllUsers(): Flow<List<User>> =
        userDao.getAllUsers()

    // ========== XANO API OPERATIONS ==========

    /**
     * Register a new user with Xano API
     * @param name User's full name
     * @param email User's email
     * @param password User's password (will be hashed by Xano)
     * @param role User's role (admin, user) - default: user
     * @return User object or null if failed
     */
    suspend fun signupWithXano(name: String, email: String, password: String, role: String = "user"): User? {
        return try {
            val request = SignupRequest(name, email, password, role)
            val response = apiService.signup(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                val userId = authResponse.userId

                // Create user object for local database
                val localUser = User(
                    id = userId,
                    email = email,
                    password = "xano_managed", // Placeholder - real password is in Xano
                    name = name,
                    role = role
                )

                // Save to local database
                insertUser(localUser)

                Log.d(TAG, "Signup successful: $email with user_id: $userId")
                localUser
            } else {
                Log.e(TAG, "Signup failed: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Signup error: ${e.message}", e)
            null
        }
    }

    /**
     * Login user with Xano API
     * @param email User's email
     * @param password User's password
     * @return User object or null if failed
     */
    suspend fun loginWithXano(email: String, password: String): User? {
        return try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                val userId = authResponse.userId

                // Get user from local database by email
                val existingUser = getUserByEmail(email)

                if (existingUser != null) {
                    // Update user ID if different
                    if (existingUser.id != userId) {
                        val updatedUser = existingUser.copy(id = userId)
                        updateUser(updatedUser)
                        Log.d(TAG, "Login successful: $email (updated user_id to $userId)")
                        updatedUser
                    } else {
                        Log.d(TAG, "Login successful: $email")
                        existingUser
                    }
                } else {
                    // User doesn't exist locally - create with minimal info
                    val newUser = User(
                        id = userId,
                        email = email,
                        password = "xano_managed",
                        name = email.substringBefore("@"), // Use email prefix as name
                        role = "user" // Default role
                    )
                    insertUser(newUser)
                    Log.d(TAG, "Login successful: $email (created local user)")
                    newUser
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Login failed - Code: ${response.code()}, Error: $errorBody")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    /**
     * Logout user (no server-side action needed since we don't use tokens)
     */
    suspend fun logout() {
        Log.d(TAG, "User logged out")
    }

    /**
     * Sync all users from Xano to local database
     * NOTE: This is deprecated as user management is now local-only
     * @return Number of users synced, or null if failed
     */
    @Deprecated("User management is local-only, no Xano sync needed")
    suspend fun syncUsersFromXano(): Int? {
        Log.d(TAG, "User sync skipped - using local authentication only")
        return 0  // Return 0 to indicate no sync needed
    }

    /**
     * Get user by ID from Xano
     * NOTE: This is deprecated as user management is now local-only
     * @param userId User ID
     * @return User object or null if failed
     */
    @Deprecated("User management is local-only")
    suspend fun getUserByIdFromXano(userId: Int): User? {
        Log.d(TAG, "User fetch from Xano skipped - using local data only")
        return getUserById(userId)  // Fallback to local DB
    }

    /**
     * Sync users in background (offline-first approach)
     * NOTE: This is deprecated as user management is now local-only
     */
    @Deprecated("User management is local-only")
    suspend fun syncUsersInBackground() {
        Log.d(TAG, "Background user sync skipped - using local authentication only")
        // No-op: users are managed locally only
    }
}
