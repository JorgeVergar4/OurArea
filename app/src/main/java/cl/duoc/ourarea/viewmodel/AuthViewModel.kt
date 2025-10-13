package cl.duoc.ourarea.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.ourarea.model.AppDatabase
import cl.duoc.ourarea.model.User
import cl.duoc.ourarea.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val repository = UserRepository(userDao)

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess

    fun register(
        name: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            if (!repository.userExists(email)) {
                repository.register(User(name = name, email = email, password = password))
                _registrationSuccess.value = true
            } else {
                _registrationSuccess.value = false
            }
        }
    }
}
