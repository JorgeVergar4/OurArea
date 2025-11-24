package cl.duoc.ourarea.repository

import cl.duoc.ourarea.model.User
import cl.duoc.ourarea.model.UserDao
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
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


}
