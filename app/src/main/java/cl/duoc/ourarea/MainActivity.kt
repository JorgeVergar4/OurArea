package cl.duoc.ourarea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.ourarea.data.UserPreferencesManager
import cl.duoc.ourarea.model.AppDatabase
import cl.duoc.ourarea.repository.EventRepository
import cl.duoc.ourarea.repository.UserRepository
import cl.duoc.ourarea.ui.AppNavGraph
import cl.duoc.ourarea.ui.theme.OurAreaTheme
import cl.duoc.ourarea.viewmodel.AuthViewModel
import cl.duoc.ourarea.viewmodel.AuthViewModelFactory
import cl.duoc.ourarea.viewmodel.EventViewModel
import cl.duoc.ourarea.viewmodel.EventViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar base de datos
        val database = AppDatabase.getInstance(applicationContext)

        // Crear repositorios
        val eventRepository = EventRepository(database.eventDao())
        val userRepository = UserRepository(database.userDao())

        // Crear UserPreferencesManager
        val preferencesManager = UserPreferencesManager(applicationContext)

        setContent {
            OurAreaTheme {
                // Crear ViewModels con Application context
                val eventViewModel: EventViewModel = viewModel(
                    factory = EventViewModelFactory(application, eventRepository)
                )
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(userRepository, preferencesManager)
                )

                // Cargar eventos de ejemplo al iniciar
                LaunchedEffect(Unit) {
                    eventViewModel.insertSampleEventsChile()
                }

                // Iniciar navegación
                AppNavGraph(
                    eventViewModel = eventViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
