package cl.duoc.ourarea.repository

import android.util.Log
import cl.duoc.ourarea.api.RetrofitClient
import cl.duoc.ourarea.api.models.toEvent
import cl.duoc.ourarea.api.models.toEventRequest
import cl.duoc.ourarea.model.Event
import cl.duoc.ourarea.model.EventDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * EventRepository - Manages event data from both local (Room) and remote (Xano) sources
 * Implements offline-first architecture:
 * - Read from local database (fast, works offline)
 * - Write to local database first, then sync to Xano in background
 * - Periodic sync to keep data up-to-date
 *
 * Uses Xano Events API: https://x8ki-letl-twmt.n7.xano.io/api:dBEiwsrR/
 */
class EventRepository(
    private val eventDao: EventDao,
    private val apiService: cl.duoc.ourarea.api.XanoApiService = RetrofitClient.eventsApiService
) {

    companion object {
        private const val TAG = "EventRepository"
    }

    // ========== LOCAL OPERATIONS (Room Database) ==========

    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()

    suspend fun getEventById(id: Int): Event? = eventDao.getEventById(id)

    suspend fun insertEvents(events: List<Event>) = eventDao.insertEvents(events)

    suspend fun insertEvent(event: Event) = eventDao.insertEvent(event)

    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)

    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)

    suspend fun deleteEventById(eventId: Int) = eventDao.deleteEventById(eventId)

    suspend fun deleteAllEvents() = eventDao.deleteAllEvents()

    // ========== XANO API OPERATIONS ==========

    /**
     * Sync events from Xano API to local database
     * OFFLINE-FIRST: Merge remote events with local-only events (negative IDs)
     * @param latitude User's current latitude (optional)
     * @param longitude User's current longitude (optional)
     * @param radius Search radius in km (optional)
     * @return true if sync successful, false otherwise
     */
    suspend fun syncEventsFromXano(
        latitude: Double? = null,
        longitude: Double? = null,
        radius: Int? = null
    ): Boolean {
        return try {
            val response = apiService.getEvents(
                latitude = latitude,
                longitude = longitude,
                radius = radius
            )

            if (response.isSuccessful && response.body() != null) {
                val eventResponses = response.body()!!
                val remoteEvents = eventResponses.map { it.toEvent() }

                // Get current local events (get first emission from Flow)
                val localEvents = try {
                    eventDao.getAllEvents().first()
                } catch (e: Exception) {
                    emptyList()
                }

                // Keep local-only events (negative IDs = not yet synced to Xano)
                val localOnlyEvents = localEvents.filter { it.id < 0 }

                // Delete all events with positive IDs (they'll be replaced by remote)
                localEvents.filter { it.id > 0 }.forEach { event ->
                    deleteEventById(event.id)
                }

                // Insert remote events
                insertEvents(remoteEvents)

                // Local-only events are preserved automatically since we didn't delete them

                Log.d(TAG, "✅ Synced ${remoteEvents.size} remote events, kept ${localOnlyEvents.size} local-only events")
                true
            } else {
                Log.e(TAG, "Sync events failed: ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync events error: ${e.message}", e)
            false
        }
    }

    /**
     * Get events with filters from Xano API
     * @return List of events or null if failed
     */
    suspend fun getEventsFromXano(
        latitude: Double? = null,
        longitude: Double? = null,
        radius: Int? = null,
        isFree: Boolean? = null,
        isMusic: Boolean? = null,
        isFood: Boolean? = null,
        isArt: Boolean? = null,
        isSports: Boolean? = null,
        isFamily: Boolean? = null,
        search: String? = null
    ): List<Event>? {
        return try {
            val response = apiService.getEvents(
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                isFree = isFree,
                isMusic = isMusic,
                isFood = isFood,
                isArt = isArt,
                isSports = isSports,
                isFamily = isFamily,
                search = search
            )

            if (response.isSuccessful && response.body() != null) {
                val events = response.body()!!.map { it.toEvent() }
                Log.d(TAG, "Got ${events.size} events from Xano")
                events
            } else {
                Log.e(TAG, "Get events failed: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get events error: ${e.message}", e)
            null
        }
    }

    /**
     * Create event on Xano and save to local database
     * OFFLINE-FIRST: Guarda SIEMPRE localmente primero, luego intenta Xano
     * @param event Event to create
     * @param imageFile Optional image file to upload (if image is a local path)
     * @return Created event (always returns an event, even if Xano fails)
     */
    suspend fun createEventOnXano(event: Event, imageFile: File? = null): Event? {
        // PASO 1: GUARDAR LOCALMENTE PRIMERO (offline-first)
        return try {
            Log.d(TAG, "Creating event: ${event.title}")
            
            // Guardar en base de datos local SIEMPRE
            val localId = insertEvent(event)
            Log.d(TAG, "Event saved locally with ID: $localId")
            
            // PASO 2: INTENTAR SINCRONIZAR CON XANO
            try {
                Log.d(TAG, "Attempting to sync with Xano: ${event.title}")

                val response = if (imageFile != null && imageFile.exists()) {
                    // Create event with image using multipart/form-data
                    Log.d(TAG, "Creating event with image: ${imageFile.name}")

                    val titlePart = event.title.toRequestBody("text/plain".toMediaTypeOrNull())
                    val descriptionPart = event.description.toRequestBody("text/plain".toMediaTypeOrNull())
                    val latitudePart = event.latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val longitudePart = event.longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val timeInfoPart = event.timeInfo.toRequestBody("text/plain".toMediaTypeOrNull())
                    val isFreePart = event.isFree.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val isFamilyPart = event.isFamily.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val isMusicPart = event.isMusic.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val isFoodPart = event.isFood.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val isArtPart = event.isArt.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val isSportsPart = event.isSports.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val userIdPart = event.createdByUserId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                    val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                    val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)

                    apiService.createEventWithImage(
                        title = titlePart,
                        description = descriptionPart,
                        latitude = latitudePart,
                        longitude = longitudePart,
                        timeInfo = timeInfoPart,
                        isFree = isFreePart,
                        isFamily = isFamilyPart,
                        isMusic = isMusicPart,
                        isFood = isFoodPart,
                        isArt = isArtPart,
                        isSports = isSportsPart,
                        userId = userIdPart,
                        image = imagePart
                    )
                } else {
                    // Create event without image using JSON
                    Log.d(TAG, "Creating event without image")
                    val request = event.toEventRequest()
                    apiService.createEvent(request)
                }

                if (response.isSuccessful && response.body() != null) {
                    val xanoEvent = response.body()!!.toEvent()

                    // Actualizar evento local con el ID de Xano
                    updateEvent(xanoEvent)

                    Log.d(TAG, "✅ Event created on Xano successfully: ${xanoEvent.title} (ID: ${xanoEvent.id})")
                    xanoEvent
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.w(TAG, "⚠️ Xano failed, event saved locally only. Error: $errorBody")

                    // Retornar el evento local guardado
                    event.copy(id = localId.toInt())
                }
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Xano error (no internet?): ${e.message}, event saved locally")
                
                // Retornar el evento local guardado
                event.copy(id = localId.toInt())
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save event: ${e.message}", e)
            null
        }
    }

    /**
     * Create event with offline-first approach
     * Saves to local DB immediately, syncs to Xano in background
     * @param event Event to create
     * @param imageFile Optional image file to upload
     * @return true if saved locally, false otherwise
     */
    suspend fun createEventOfflineFirst(event: Event, imageFile: File? = null): Boolean {
        return try {
            // Save to local database first (works offline)
            insertEvent(event)
            Log.d(TAG, "Event saved locally: ${event.title}")

            // Try to sync to Xano in background (non-blocking)
            try {
                createEventOnXano(event, imageFile)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync event to Xano (will retry later): ${e.message}")
                // Event is already saved locally, so we don't fail
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Create event offline-first error: ${e.message}", e)
            false
        }
    }

    /**
     * Update event on Xano
     * @param event Event to update
     * @param imageFile Optional new image file to upload
     * @return Updated event or null if failed
     */
    suspend fun updateEventOnXano(event: Event, imageFile: File? = null): Event? {
        return try {
            // Step 1: Upload new image if provided
            var imageUrl = event.image
            if (imageFile != null && imageFile.exists()) {
                Log.d(TAG, "Uploading new image to Xano: ${imageFile.name}")
                val uploadedUrl = uploadImageToXano(imageFile)
                if (uploadedUrl != null) {
                    imageUrl = uploadedUrl
                    Log.d(TAG, "New image uploaded successfully: $imageUrl")
                } else {
                    Log.w(TAG, "Image upload failed, keeping existing image")
                }
            }

            // Step 2: Update event with new image URL if changed (NO JWT required)
            val eventWithImageUrl = if (imageUrl != event.image) {
                event.copy(image = imageUrl)
            } else {
                event
            }

            val request = eventWithImageUrl.toEventRequest()
            val response = apiService.updateEvent(event.id, request)

            if (response.isSuccessful && response.body() != null) {
                val updatedEvent = response.body()!!.toEvent()

                // Update local database
                updateEvent(updatedEvent)

                Log.d(TAG, "Updated event on Xano: ${updatedEvent.title}")
                updatedEvent
            } else {
                Log.e(TAG, "Update event failed: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Update event error: ${e.message}", e)
            null
        }
    }

    /**
     * Delete event from Xano and local database
     * @param eventId Event ID to delete
     * @return true if deleted successfully, false otherwise
     */
    suspend fun deleteEventFromXano(eventId: Int): Boolean {
        return try {
            val response = apiService.deleteEvent(eventId)

            if (response.isSuccessful) {
                // Delete from local database
                deleteEventById(eventId)

                Log.d(TAG, "Deleted event from Xano: $eventId")
                true
            } else {
                Log.e(TAG, "Delete event failed: ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Delete event error: ${e.message}", e)
            false
        }
    }

    /**
     * Upload image to Xano storage
     * @param imageFile Image file to upload
     * @return Image URL or null if failed
     */
    suspend fun uploadImageToXano(imageFile: File): String? {
        return try {
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                requestBody
            )

            val response = apiService.uploadImage(multipartBody)

            if (response.isSuccessful && response.body() != null) {
                val uploadResponse = response.body()!!
                Log.d(TAG, "Image uploaded: ${uploadResponse.url}")
                uploadResponse.url
            } else {
                Log.e(TAG, "Upload image failed: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload image error: ${e.message}", e)
            null
        }
    }

    /**
     * Get a single event by ID from Xano
     * @param eventId Event ID
     * @return Event or null if failed
     */
    suspend fun getEventByIdFromXano(eventId: Int): Event? {
        return try {
            val response = apiService.getEventById(eventId)

            if (response.isSuccessful && response.body() != null) {
                val event = response.body()!!.toEvent()

                // Actualizar en la base de datos local
                insertEvent(event)

                Log.d(TAG, "Got event from Xano: ${event.title}")
                event
            } else {
                Log.e(TAG, "Get event by ID failed: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get event by ID error: ${e.message}", e)
            null
        }
    }

    /**
     * Get events created by a specific user
     * @param userId User ID
     * @return List of events or null if failed
     */
    suspend fun getEventsByUserFromXano(userId: Int): List<Event>? {
        return try {
            val response = apiService.getEventsByUser(userId)

            if (response.isSuccessful && response.body() != null) {
                val events = response.body()!!.map { it.toEvent() }
                Log.d(TAG, "Got ${events.size} events for user $userId")
                events
            } else {
                Log.e(TAG, "Get user events failed: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get user events error: ${e.message}", e)
            null
        }
    }
}
