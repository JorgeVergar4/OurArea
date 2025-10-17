package cl.duoc.ourarea.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.ourarea.model.Event
import cl.duoc.ourarea.repository.EventRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EventViewModel(private val repository: EventRepository) : ViewModel() {
    private val _filteredEvents = MutableStateFlow<List<Event>>(emptyList())
    val filteredEvents: StateFlow<List<Event>> = _filteredEvents

    private var allEvents: List<Event> = emptyList()
    private var userLocation: Location? = null

    init {
        viewModelScope.launch {
            repository.getAllEvents().collect { events ->
                allEvents = events
                updateDistances()
                _filteredEvents.value = allEvents
            }
        }
    }

    fun setUserLocation(location: Location) {
        userLocation = location
        updateDistances()
    }

    fun applyFilters(query: String, filter: String) {
        var filtered = allEvents
        if (query.isNotBlank()) {
            filtered = filtered.filter { it.title.contains(query, ignoreCase = true) }
        }
        if (filter != "Todos") {
            filtered = when (filter) {
                "Hoy", "Este fin" -> filtered.filter { it.timeInfo == filter }
                "Gratis" -> filtered.filter { it.isFree }
                "Familia" -> filtered.filter { it.isFamily }
                "Música" -> filtered.filter { it.isMusic }
                "Comida" -> filtered.filter { it.isFood }
                "Arte" -> filtered.filter { it.isArt }
                "Deportes" -> filtered.filter { it.isSports }
                else -> filtered
            }
        }
        _filteredEvents.value = filtered

    }


    private fun updateDistances() {
        userLocation?.let { loc ->
            allEvents = allEvents.map { event ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    loc.latitude, loc.longitude,
                    event.latitude, event.longitude,
                    results
                )
                event.apply { distance = results[0] }
            }
            _filteredEvents.value = allEvents
        }
    }

    fun insertSampleEventsChile() {
        viewModelScope.launch {
            val existingEvents = repository.getAllEvents().firstOrNull() ?: emptyList()
            if (existingEvents.isEmpty()){
            val sampleEvents = listOf(
                Event(
                    title = "Feria Artesanal Santa Lucía",
                    description = "Artesanía chilena y souvenirs en pleno centro de Santiago.",
                    latitude = -33.4411,
                    longitude = -70.6458,
                    image = "https://www.santiagoturismo.cl/wp-content/uploads/2021/03/foto-interior-8-scaled.jpg",
                    timeInfo = "Hoy",
                    isFree = true,
                    isFamily = true
                ),
                Event(
                    title = "Noche de Museos",
                    description = "Museos de Santiago abren gratis en la noche con actividades especiales.",
                    latitude = -33.4372,
                    longitude = -70.6506,
                    image = "https://media.biobiochile.cl/wp-content/uploads/2024/09/noche-de-museos-750x400.png",
                    timeInfo = "Este fin",
                    isFree = true,
                    isFamily = true,
                    isArt = true
                ),
                Event(
                    title = "Festival de Cine UC",
                    description = "48° Festival de Cine UC con estrenos internacionales y nacionales.",
                    latitude = -33.4418,
                    longitude = -70.6465,
                    image = "https://extension.uc.cl/wp-content/uploads/2024/10/0B0A9047-2-scaled.jpg",
                    timeInfo = "Hoy",
                    isFree = true,
                    isFamily = true
                ),
                Event(
                    title = "Museo Nacional de Bellas Artes",
                    description = "Exposición permanente y actividades culturales en el MNBA.",
                    latitude = -33.4372,
                    longitude = -70.6410,
                    image = "https://www.mnba.gob.cl/sites/www.mnba.gob.cl/files/styles/16x9_grande/public/2024-05/IMG_20230321_090820.jpg?h=920929c4&itok=8SBDfQhh",
                    timeInfo = "Hoy",
                    isFree = true,
                    isArt = true
                ),
                Event(
                    title = "Mercado de la Vega Central",
                    description = "Feria de frutas, verduras y gastronomía chilena.",
                    latitude = -33.4275,
                    longitude = -70.6417,
                    image = "https://dynamic-media-cdn.tripadvisor.com/media/photo-o/1a/04/43/f0/wwwvisitsantiagoorg-ubicacion.jpg?w=900&h=500&s=1",
                    timeInfo = "Hoy",
                    isFood = true,
                    isFamily = true
                ),
                Event(
                    title = "Festival de los Patrimonios",
                    description = "Música, cultura y tradición en la Plaza de la Cultura.",
                    latitude = -33.4441,
                    longitude = -70.6536,
                    image = "https://cloudfront-us-east-1.images.arcpublishing.com/copesa/FA44XG7EJZFNVM7AXOFZDT6DCE.jpg",
                    timeInfo = "Este fin",
                    isFree = true,
                    isMusic = true,
                    isFamily = true
                ),
                Event(
                    title = "Cine Hoyts Parque Arauco",
                    description = "Estrenos de cine internacional y nacional.",
                    latitude = -33.4032,
                    longitude = -70.5676,
                    image = "https://offloadmedia.feverup.com/santiagosecreto.com/wp-content/uploads/2023/09/25102131/3-1-17.jpg",
                    timeInfo = "Hoy"
                ),
                Event(
                    title = "Torneo de Fútbol Infantil",
                    description = "Campeonato de fútbol para niños en Estadio Bicentenario.",
                    latitude = -33.5000,
                    longitude = -70.6111,
                    image = "https://www.portalpuentealto.cl/wp-content/uploads/2024/12/DSC02915-1536x864-1.jpg",
                    timeInfo = "Este fin",
                    isSports = true,
                    isFamily = true
                ),
                Event(
                    title = "Festival de Comida Vegana",
                    description = "Food trucks y stands de comida vegana en Providencia.",
                    latitude = -33.4263,
                    longitude = -70.6092,
                    image = "https://mesadetemporada.com/wp-content/uploads/2020/05/vegana-1.jpg",
                    timeInfo = "Hoy",
                    isFood = true
                ),
                Event(
                    title = "Exposición de Arte Contemporáneo",
                    description = "Galería de arte con artistas emergentes chilenos.",
                    latitude = -33.4375,
                    longitude = -70.6500,
                    image = "https://www.escueladesarts.com/wp-content/uploads/galerias-de-arte.jpg",
                    timeInfo = "Este fin",
                    isArt = true
                )
            )
            repository.insertEvents(sampleEvents)

            }


        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }
    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    fun deleteAllEvents() {
        viewModelScope.launch {
            repository.deleteAllEvents()
        }
    }

}


