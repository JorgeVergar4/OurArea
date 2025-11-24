package cl.duoc.ourarea.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.ourarea.data.PasswordHasher
import cl.duoc.ourarea.data.UserPreferencesManager
import cl.duoc.ourarea.model.User
import cl.duoc.ourarea.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _isCheckingSession = MutableStateFlow(true)
    val isCheckingSession: StateFlow<Boolean> = _isCheckingSession

    private val _emailExists = MutableStateFlow(false)
    val emailExists: StateFlow<Boolean> = _emailExists

    private val _emailCheckError = MutableStateFlow<String?>(null)
    val emailCheckError: StateFlow<String?> = _emailCheckError

    init {
        // Verificar si hay sesión activa al iniciar
        checkExistingSession()
    }

    /**
     * Verifica si existe una sesión guardada y la restaura
     */
    private fun checkExistingSession() {
        viewModelScope.launch {
            try {
                preferencesManager.isLoggedInFlow.first { isLoggedIn ->
                    if (isLoggedIn) {
                        val userId = preferencesManager.userIdFlow.first()
                        if (userId != null) {
                            // Cargar usuario desde la BD
                            val user = userRepository.getUserById(userId)
                            if (user != null) {
                                _currentUser.value = user
                                _isLoggedIn.value = true
                            } else {
                                // Si el usuario no existe en BD, limpiar sesión
                                preferencesManager.clearUserSession()
                                _isLoggedIn.value = false
                            }
                        }
                    }
                    true // Completar el flow
                }
            } catch (e: Exception) {
                // Si hay error, mantener estado de no logueado
                _isLoggedIn.value = false
                _currentUser.value = null
            } finally {
                // Marcar que terminó de verificar la sesión
                _isCheckingSession.value = false
            }
        }
    }

    fun checkEmailExists(email: String) {
        viewModelScope.launch {
            try {
                if (email.isNotBlank()) {
                    val user = userRepository.getUserByEmail(email)
                    _emailExists.value = user != null
                    _emailCheckError.value = if (user != null) "Este email ya está registrado" else null
                } else {
                    _emailExists.value = false
                    _emailCheckError.value = null
                }
            } catch (e: Exception) {
                _emailCheckError.value = null
            }
        }
    }

    fun clearEmailCheck() {
        _emailExists.value = false
        _emailCheckError.value = null
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Buscar usuario por email
                val user = userRepository.getUserByEmail(email)

                if (user != null) {
                    // Verificar contraseña con hash
                    val isPasswordValid = PasswordHasher.verifyPassword(password, user.password)

                    if (isPasswordValid) {
                        _currentUser.value = user
                        _isLoggedIn.value = true

                        // Guardar sesión en DataStore
                        preferencesManager.saveUserSession(
                            userId = user.id,
                            name = user.name,
                            email = user.email
                        )
                    } else {
                        _error.value = "Credenciales incorrectas"
                    }
                } else {
                    _error.value = "Credenciales incorrectas"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al iniciar sesión"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Verificar si el usuario ya existe
                val existingUser = userRepository.getUserByEmail(email)
                if (existingUser != null) {
                    _error.value = "El email ya está registrado"
                    _isLoading.value = false
                    return@launch
                }

                // Hashear la contraseña
                val hashedPassword = PasswordHasher.hashPassword(password)

                // Crear nuevo usuario con contraseña hasheada
                val newUser = User(
                    email = email,
                    password = hashedPassword,
                    name = name
                )

                val userId = userRepository.insertUser(newUser)
                if (userId > 0) {
                    val createdUser = newUser.copy(id = userId.toInt())
                    _currentUser.value = createdUser
                    _isLoggedIn.value = true

                    // Guardar sesión en DataStore
                    preferencesManager.saveUserSession(
                        userId = createdUser.id,
                        name = createdUser.name,
                        email = createdUser.email
                    )
                } else {
                    _error.value = "Error al crear la cuenta"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al registrar usuario"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                // Limpiar sesión en DataStore
                preferencesManager.clearUserSession()

                // Limpiar estado local
                _currentUser.value = null
                _isLoggedIn.value = false
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al cerrar sesión"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

class AuthViewModelFactory(
    private val repository: UserRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
