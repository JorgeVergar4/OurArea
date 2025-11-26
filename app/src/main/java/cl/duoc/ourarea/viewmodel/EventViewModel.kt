package cl.duoc.ourarea.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.ourarea.model.Event
import cl.duoc.ourarea.repository.EventRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class EventViewModel(
    application: Application,
    private val repository: EventRepository
) : AndroidViewModel(application) {

    private val _filteredEvents = MutableStateFlow<List<Event>>(emptyList())
    val filteredEvents: StateFlow<List<Event>> = _filteredEvents

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus

    private var allEvents: List<Event> = emptyList()
    private var userLocation: Location? = null

    init {
        loadEvents()
        syncEventsFromXano()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllEvents().collect { events ->
                allEvents = events
                updateDistances()
                _filteredEvents.value = allEvents
                _isLoading.value = false
            }
        }
    }

    /**
     * Sincroniza eventos desde Xano API
     */
    fun syncEventsFromXano() {
        viewModelScope.launch {
            try {
                _syncStatus.value = "Sincronizando con servidor..."
                val success = repository.syncEventsFromXano(
                    latitude = userLocation?.latitude,
                    longitude = userLocation?.longitude
                )
                _syncStatus.value = if (success) {
                    "Sincronización exitosa"
                } else {
                    "Usando datos locales"
                }
            } catch (e: Exception) {
                _syncStatus.value = "Error de conexión"
                _error.value = "No se pudo conectar con el servidor"
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
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
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
                event.copy(distance = results[0])
            }
            _filteredEvents.value = allEvents
        }
    }

    fun insertSampleEventsChile() {
        viewModelScope.launch {
            val existingEvents = repository.getAllEvents().firstOrNull() ?: emptyList()
            if (existingEvents.isEmpty()) {
                val sampleEvents = getSampleEvents()
                repository.insertEvents(sampleEvents)
            }
        }
    }

    private fun getSampleEvents(): List<Event> {
        return listOf(
            Event(
                title = "Feria Artesanal en Espacio Vespucio",
                description = "Artesanía local y productos chilenos cerca del mall.",
                latitude = -33.5128,
                longitude = -70.6089,
                image = "https://www.santiagoturismo.cl/wp-content/uploads/2021/03/foto-interior-8-scaled.jpg",
                timeInfo = "Hoy",
                isFree = true,
                isFamily = true,
                isMusic = false,
                isFood = false,
                isArt = false,
                isSports = false
            ),
            Event(
                title = "Exposición Cultural DUOC",
                description = "Muestra de proyectos estudiantiles y actividades culturales.",
                latitude = -33.4985,
                longitude = -70.6170,
                image = "https://media.biobiochile.cl/wp-content/uploads/2024/09/noche-de-museos-750x400.png",
                timeInfo = "Este fin",
                isFree = true,
                isFamily = true,
                isArt = true,
                isMusic = false,
                isFood = false,
                isSports = false
            ),
            Event(
                title = "Feria Persas del Bio Bio",
                description = "Feria de ropa, antigüedades y artículos varios cerca de Vicuña Mackenna.",
                latitude = -33.4850,
                longitude = -70.6250,
                image = "https://extension.uc.cl/wp-content/uploads/2024/10/0B0A9047-2-scaled.jpg",
                timeInfo = "Hoy",
                isFree = true,
                isFamily = true,
                isMusic = false,
                isFood = false,
                isArt = false,
                isSports = false
            ),
            Event(
                title = "Parque O'Higgins - Actividades",
                description = "Deportes, caminatas y actividades familiares en el parque.",
                latitude = -33.4678,
                longitude = -70.6590,
                image = "https://www.mnba.gob.cl/sites/www.mnba.gob.cl/files/styles/16x9_grande/public/2024-05/IMG_20230321_090820.jpg?h=920929c4&itok=8SBDfQhh",
                timeInfo = "Hoy",
                isFree = true,
                isSports = true,
                isFamily = true,
                isMusic = false,
                isFood = false,
                isArt = false
            ),
            Event(
                title = "Mercado Lo Valledor",
                description = "Mercado mayorista con frutas, verduras y gastronomía local.",
                latitude = -33.4701,
                longitude = -70.6868,
                image = "https://dynamic-media-cdn.tripadvisor.com/media/photo-o/1a/04/43/f0/wwwvisitsantiagoorg-ubicacion.jpg?w=900&h=500&s=1",
                timeInfo = "Hoy",
                isFood = true,
                isFamily = true,
                isFree = false,
                isMusic = false,
                isArt = false,
                isSports = false
            ),
            Event(
                title = "Concierto en Plaza San Joaquín",
                description = "Música en vivo y actividades culturales en la plaza.",
                latitude = -33.4965,
                longitude = -70.6180,
                image = "https://cloudfront-us-east-1.images.arcpublishing.com/copesa/FA44XG7EJZFNVM7AXOFZDT6DCE.jpg",
                timeInfo = "Este fin",
                isFree = true,
                isMusic = true,
                isFamily = true,
                isFood = false,
                isArt = false,
                isSports = false
            ),
            Event(
                title = "Cine Hoyts Parque Arauco",
                description = "Estrenos de cine internacional y nacional.",
                latitude = -33.4032,
                longitude = -70.5676,
                image = "https://offloadmedia.feverup.com/santiagosecreto.com/wp-content/uploads/2023/09/25102131/3-1-17.jpg",
                timeInfo = "Hoy",
                isFree = false,
                isFamily = true,
                isMusic = false,
                isFood = false,
                isArt = false,
                isSports = false
            ),
            Event(
                title = "Torneo de Fútbol Infantil",
                description = "Campeonato de fútbol para niños en Estadio Bicentenario.",
                latitude = -33.5000,
                longitude = -70.6111,
                image = "https://www.portalpuentealto.cl/wp-content/uploads/2024/12/DSC02915-1536x864-1.jpg",
                timeInfo = "Este fin",
                isSports = true,
                isFamily = true,
                isFree = false,
                isMusic = false,
                isFood = false,
                isArt = false
            ),
            Event(
                title = "Festival de Comida Vegana",
                description = "Food trucks y stands de comida vegana en Providencia.",
                latitude = -33.4263,
                longitude = -70.6092,
                image = "https://mesadetemporada.com/wp-content/uploads/2020/05/vegana-1.jpg",
                timeInfo = "Hoy",
                isFood = true,
                isFree = false,
                isFamily = false,
                isMusic = false,
                isArt = false,
                isSports = false
            ),
            Event(
                title = "Exposición de Arte Contemporáneo",
                description = "Galería de arte con artistas emergentes chilenos.",
                latitude = -33.4375,
                longitude = -70.6500,
                image = "https://www.escueladesarts.com/wp-content/uploads/galerias-de-arte.jpg",
                timeInfo = "Este fin",
                isArt = true,
                isFree = false,
                isFamily = false,
                isMusic = false,
                isFood = false,
                isSports = false
            )
        )
    }


    /**
     * Crea un evento en Xano y guarda localmente
     * @param event Evento a crear
     * @param imageFile Archivo de imagen opcional para subir a Xano
     * @param onSuccess Callback con el evento creado (o null si falla)
     */
    fun insertEvent(event: Event, imageFile: File? = null, onSuccess: (Event?) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Crear evento (intenta Xano primero, si falla guarda localmente)
                val createdEvent = repository.createEventOnXano(event, imageFile)

                if (createdEvent != null) {
                    // Verificar si el evento se sincronizó a Xano o es solo local
                    if (createdEvent.id > 0 && createdEvent.id == event.id) {
                        // ID autogenerado por Room (solo local)
                        _syncStatus.value = "⚠️ Evento guardado localmente (sin conexión)"
                    } else if (createdEvent.id > 0) {
                        // ID de Xano (sincronizado exitosamente)
                        _syncStatus.value = "✅ Evento publicado correctamente"
                        // FORZAR RECARGA desde Xano para obtener el evento actualizado
                        syncEventsFromXano()
                    } else {
                        // ID 0 (autogenerate de Room)
                        _syncStatus.value = "✅ Evento guardado"
                    }
                    onSuccess(createdEvent)
                } else {
                    // Error al guardar (ni local ni Xano)
                    _error.value = "❌ Error al crear evento. Intenta de nuevo."
                    onSuccess(null)
                }
            } catch (e: Exception) {
                _error.value = "❌ Error: ${e.message}"
                onSuccess(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina un evento de Xano y localmente
     */
    fun deleteEvent(event: Event, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // PRIMERO: Eliminar de Xano
                val success = repository.deleteEventFromXano(event.id)

                if (success) {
                    // Éxito: evento eliminado de Xano y local
                    _syncStatus.value = "Evento eliminado"

                    // Eliminar archivo de imagen si existe y es una ruta local
                    if (event.image.isNotEmpty() && event.image.startsWith("/")) {
                        deleteEventImage(event.image)
                    }

                    onSuccess()
                } else {
                    // Fallback: Eliminar solo localmente
                    if (event.image.isNotEmpty() && event.image.startsWith("/")) {
                        deleteEventImage(event.image)
                    }
                    repository.deleteEvent(event)
                    _error.value = "Evento eliminado solo localmente (sin conexión)"
                    onSuccess()
                }
            } catch (e: Exception) {
                _error.value = "Error al eliminar evento: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza un evento en Xano y localmente
     * @param event Evento a actualizar
     * @param imageFile Archivo de imagen opcional (solo si cambió la imagen)
     * @param onSuccess Callback con el evento actualizado (o null si falla)
     */
    fun updateEvent(event: Event, imageFile: File? = null, onSuccess: (Event?) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Actualizar en Xano (con nueva imagen si es necesaria)
                val updatedEvent = repository.updateEventOnXano(event, imageFile)

                if (updatedEvent != null) {
                    _syncStatus.value = "Evento actualizado exitosamente"
                    onSuccess(updatedEvent)
                } else {
                    // Fallback: Actualizar solo localmente
                    repository.updateEvent(event)
                    _error.value = "Evento actualizado solo localmente (sin conexión)"
                    onSuccess(event)
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar evento: ${e.message}"
                onSuccess(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina el archivo de imagen del almacenamiento interno
     */
    private fun deleteEventImage(imagePath: String) {
        try {
            val file = File(imagePath)
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    println("Imagen eliminada: $imagePath")
                } else {
                    println("No se pudo eliminar la imagen: $imagePath")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Verifica si el usuario actual puede eliminar el evento
     */
    fun canDeleteEvent(event: Event, currentUserId: Int): Boolean {
        return event.createdByUserId == currentUserId
    }

    /**
     * Limpia mensajes de error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Limpia mensajes de sincronización
     */
    fun clearSyncStatus() {
        _syncStatus.value = null
    }
}

/**
 * Factory para crear instancias de EventViewModel con Application y Repository
 */
class EventViewModelFactory(
    private val application: Application,
    private val repository: EventRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
