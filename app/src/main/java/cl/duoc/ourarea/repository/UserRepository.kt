package cl.duoc.ourarea.repository

import cl.duoc.ourarea.model.User
import cl.duoc.ourarea.model.UserDao

class UserRepository(private val userDao: UserDao) {
    suspend fun register(user: User) = userDao.insertUser(user)
    suspend fun userExists(email: String) = userDao.getUserByEmail(email) != null
    suspend fun login (email: String, password: String) = userDao.getUser(email, password)
}