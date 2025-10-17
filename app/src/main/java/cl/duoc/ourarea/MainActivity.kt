package cl.duoc.ourarea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import cl.duoc.ourarea.model.AppDatabase
import cl.duoc.ourarea.repository.EventRepository
import cl.duoc.ourarea.viewmodel.EventViewModel
import cl.duoc.ourarea.viewmodel.EventViewModelFactory
import cl.duoc.ourarea.navigation.AppNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Instancia para eventos
            val eventDao = AppDatabase.getInstance(applicationContext).eventDao()
            val eventRepository = EventRepository(eventDao)
            val eventViewModelFactory = EventViewModelFactory(eventRepository)
            val eventViewModel: EventViewModel = viewModel(factory = eventViewModelFactory)

            // Población automática de eventos reales chilenos al iniciar
            LaunchedEffect(Unit) {
                eventViewModel.insertSampleEventsChile()
            }

            AppNavGraph(
                eventViewModel = eventViewModel
            )
        }
    }
}
