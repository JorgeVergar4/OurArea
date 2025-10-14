package cl.duoc.ourarea.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.ourarea.model.Event
import cl.duoc.ourarea.repository.EventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = EventRepository(app)
    val events: StateFlow<List<Event>> = repository.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (events.value.isEmpty()) {
                repository.insertEvents(sampleEvents)
            }
        }
    }
}

private val sampleEvents = listOf(
    Event(
        title = "Feria de Emprendedores",
        latitude = -33.45,
        longitude = -70.66,
        image = "https://images.unsplash.com/photo-1506744038136-46273834b3fb",
        description = "Plaza Central, productos locales",
        distance = 1200.0,
        timeInfo = "Hoy 16:00"
    ),
    Event(
        title = "Concierto al aire libre",
        latitude = -33.46,
        longitude = -70.65,
        image = "https://images.unsplash.com/photo-1465101046530-73398c7f28ca",
        description = "Parque Metropolitano, música en vivo",
        distance = 2500.0,
        timeInfo = "Sábado 19:00"
    ),
    Event(
        title = "Taller de arte para niños",
        latitude = -33.44,
        longitude = -70.67,
        image = "https://images.unsplash.com/photo-1519125323398-675f0ddb6308",
        description = "Centro cultural, actividades familiares",
        distance = 800.0,
        timeInfo = "Domingo 11:00"
    )
)

