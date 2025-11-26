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
        // Sincronizar usuarios de Xano en segundo plano
        syncUsersFromXano()
    }

    /**
     * Sincroniza usuarios desde Xano a la base de datos local
     * Se ejecuta en segundo plano sin bloquear la UI
     */
    private fun syncUsersFromXano() {
        viewModelScope.launch {
            try {
                android.util.Log.d("AuthViewModel", "Starting user sync from Xano...")
                val syncedCount = userRepository.syncUsersFromXano()
                if (syncedCount != null) {
                    android.util.Log.d("AuthViewModel", "Successfully synced $syncedCount users from Xano")
                } else {
                    android.util.Log.d("AuthViewModel", "User sync failed (possibly offline)")
                }
            } catch (e: Exception) {
                // Silently fail - la app funciona offline
                android.util.Log.d("AuthViewModel", "User sync error: ${e.message}")
            }
        }
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
                // PRIMERO: Intentar login con Xano API
                val user = userRepository.loginWithXano(email, password)

                if (user != null) {
                    // Login exitoso con Xano
                    _currentUser.value = user
                    _isLoggedIn.value = true

                    // Guardar sesión en DataStore
                    preferencesManager.saveUserSession(
                        userId = user.id,
                        name = user.name,
                        email = user.email
                    )

                    android.util.Log.d("AuthViewModel", "Xano login successful: ${user.name}")
                } else {
                    // Si falla Xano, intentar login local (fallback offline)
                    android.util.Log.d("AuthViewModel", "Xano login failed, trying local login for: $email")
                    val user = userRepository.getUserByEmail(email)

                    if (user != null) {
                        android.util.Log.d("AuthViewModel", "User found locally: ${user.email}")
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
                            android.util.Log.d("AuthViewModel", "Local login successful")
                        } else {
                            android.util.Log.e("AuthViewModel", "Password verification failed")
                            _error.value = "Email o contraseña incorrectos"
                        }
                    } else {
                        android.util.Log.e("AuthViewModel", "User not found locally: $email")
                        _error.value = "Email o contraseña incorrectos"
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Login exception: ${e.message}", e)
                _error.value = "Error de conexión. Verifica tu internet e intenta nuevamente."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(name: String, email: String, password: String, role: String = "user") {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // PRIMERO: Intentar registro con Xano API
                val user = userRepository.signupWithXano(name, email, password, role)

                if (user != null) {
                    // Registro exitoso con Xano
                    _currentUser.value = user
                    _isLoggedIn.value = true

                    // Guardar sesión en DataStore
                    preferencesManager.saveUserSession(
                        userId = user.id,
                        name = user.name,
                        email = user.email
                    )
                } else {
                    // Si falla Xano, intentar registro local (fallback offline)
                    // Verificar si el usuario ya existe localmente
                    val existingUser = userRepository.getUserByEmail(email)
                    if (existingUser != null) {
                        _error.value = "El email ya está registrado"
                        _isLoading.value = false
                        return@launch
                    }

                    // Fallback offline no soportado para registro
                    // Solo se puede registrar en Xano
                    _error.value = "No se puede registrar sin conexión. Verifica tu internet."
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
                // Limpiar estado local INMEDIATAMENTE
                _currentUser.value = null
                _isLoggedIn.value = false
                _error.value = null
                
                // Limpiar sesión en DataStore
                preferencesManager.clearUserSession()

                // Cerrar sesión en Xano en segundo plano (no bloqueante)
                try {
                    userRepository.logout()
                } catch (e: Exception) {
                    // Ignorar errores de logout en Xano
                }
            } catch (e: Exception) {
                // Incluso si hay error, asegurar que se limpie la sesión
                _currentUser.value = null
                _isLoggedIn.value = false
                _error.value = null
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
