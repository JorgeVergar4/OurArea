package cl.duoc.ourarea.api

import cl.duoc.ourarea.api.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Xano REST API Service Interface
 * Define all API endpoints for events, roles, and event logs
 *
 * IMPORTANT: This service uses RELATIVE paths. Configure base URLs in RetrofitClient:
 * - Events API: https://x8ki-letl-twmt.n7.xano.io/api:8B8nOhtv/
 * - Roles API: https://x8ki-letl-twmt.n7.xano.io/api:6dvTuZu9/
 * - Event Logs API: https://x8ki-letl-twmt.n7.xano.io/api:To8zQg-7/
 * - Misc API: https://x8ki-letl-twmt.n7.xano.io/api:p4Kx6qbK/
 */
interface XanoApiService {

    // ========== EVENT ENDPOINTS ==========
    // Base URL: https://x8ki-letl-twmt.n7.xano.io/api:8B8nOhtv/

    // ========== EVENT ENDPOINTS ==========
    // Base URL: https://x8ki-letl-twmt.n7.xano.io/api:8B8nOhtv/

    /**
     * Get all events (with optional filters)
     * GET /event
     * @param latitude User's current latitude for distance calculation
     * @param longitude User's current longitude for distance calculation
     * @param radius Search radius in kilometers (optional)
     * @param isFree Filter for free events only (optional)
     * @param isMusic Filter for music events (optional)
     * @param isFood Filter for food events (optional)
     * @param isArt Filter for art events (optional)
     * @param isSports Filter for sports events (optional)
     * @param isFamily Filter for family events (optional)
     * @param search Search query for title/description (optional)
     * @return List of EventResponse
     */
    @GET("event")
    suspend fun getEvents(
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("radius") radius: Int? = null,
        @Query("is_free") isFree: Boolean? = null,
        @Query("is_music") isMusic: Boolean? = null,
        @Query("is_food") isFood: Boolean? = null,
        @Query("is_art") isArt: Boolean? = null,
        @Query("is_sports") isSports: Boolean? = null,
        @Query("is_family") isFamily: Boolean? = null,
        @Query("search") search: String? = null
    ): Response<List<EventResponse>>

    /**
     * Get a single event by ID
     * GET /event/{event_id}
     * @param eventId Event ID
     * @return EventResponse
     */
    @GET("event/{event_id}")
    suspend fun getEventById(
        @Path("event_id") eventId: Int
    ): Response<EventResponse>

    /**
     * Create a new event
     * POST /event
     * @param event EventRequest with event data
     * @return Created EventResponse
     */
    @POST("event")
    suspend fun createEvent(
        @Body event: EventRequest
    ): Response<EventResponse>

    /**
     * Create a new event with image upload (multipart/form-data)
     * POST /event
     * @param title Event title
     * @param description Event description
     * @param latitude Event latitude
     * @param longitude Event longitude
     * @param timeInfo Event time information
     * @param isFree Is the event free
     * @param isFamily Is family-friendly
     * @param isMusic Is a music event
     * @param isFood Has food
     * @param isArt Is an art event
     * @param isSports Is a sports event
     * @param userId Creator user ID
     * @param image Image file (optional)
     * @return Created EventResponse
     */
    @Multipart
    @POST("event")
    suspend fun createEventWithImage(
        @Part("title") title: okhttp3.RequestBody,
        @Part("description") description: okhttp3.RequestBody,
        @Part("latitude") latitude: okhttp3.RequestBody,
        @Part("longitude") longitude: okhttp3.RequestBody,
        @Part("time_info") timeInfo: okhttp3.RequestBody,
        @Part("is_free") isFree: okhttp3.RequestBody,
        @Part("is_family") isFamily: okhttp3.RequestBody,
        @Part("is_music") isMusic: okhttp3.RequestBody,
        @Part("is_food") isFood: okhttp3.RequestBody,
        @Part("is_art") isArt: okhttp3.RequestBody,
        @Part("is_sports") isSports: okhttp3.RequestBody,
        @Part("user_id") userId: okhttp3.RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<EventResponse>

    /**
     * Update an existing event
     * PATCH /event/{event_id}
     * @param eventId Event ID to update
     * @param event EventRequest with updated data
     * @return Updated EventResponse
     */
    @PATCH("event/{event_id}")
    suspend fun updateEvent(
        @Path("event_id") eventId: Int,
        @Body event: EventRequest
    ): Response<EventResponse>

    /**
     * Delete an event (only if created by current user)
     * DELETE /event/{event_id}
     * @param eventId Event ID to delete
     */
    @DELETE("event/{event_id}")
    suspend fun deleteEvent(
        @Path("event_id") eventId: Int
    ): Response<Unit>

    /**
     * Get events created by a specific user
     * GET /event/user/{userId}
     * @param userId User ID
     * @return List of EventResponse
     */
    @GET("event/user/{userId}")
    suspend fun getEventsByUser(
        @Path("userId") userId: Int
    ): Response<List<EventResponse>>

    // ========== FILE UPLOAD ENDPOINTS ==========

    /**
     * Upload an image file to Xano storage
     * @param image MultipartBody.Part containing the image file
     * @return UploadResponse with file URL
     */
    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<UploadResponse>
}
