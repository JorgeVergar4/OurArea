package cl.duoc.ourarea.api.models

import com.google.gson.annotations.SerializedName

// ========== AUTH MODELS ==========

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "user"
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("user_id")
    val userId: Int
)

data class UserResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("role")
    val role: String = "user",
    @SerializedName("created_at")
    val createdAt: Long? = null
)

// ========== EVENT MODELS ==========

data class EventRequest(
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("image_url")
    val imageUrl: String = "",
    @SerializedName("time_info")
    val timeInfo: String,
    @SerializedName("is_free")
    val isFree: Boolean,
    @SerializedName("is_family")
    val isFamily: Boolean,
    @SerializedName("is_music")
    val isMusic: Boolean,
    @SerializedName("is_food")
    val isFood: Boolean,
    @SerializedName("is_art")
    val isArt: Boolean,
    @SerializedName("is_sports")
    val isSports: Boolean,
    @SerializedName("user_id")
    val userId: Int
)

data class XanoImage(
    @SerializedName("path")
    val path: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("type")
    val type: String = "",
    @SerializedName("url")
    val url: String = ""
)

data class EventResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("image")
    val image: XanoImage? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("time_info")
    val timeInfo: String,
    @SerializedName("is_free")
    val isFree: Boolean,
    @SerializedName("is_family")
    val isFamily: Boolean,
    @SerializedName("is_music")
    val isMusic: Boolean,
    @SerializedName("is_food")
    val isFood: Boolean,
    @SerializedName("is_art")
    val isArt: Boolean,
    @SerializedName("is_sports")
    val isSports: Boolean,
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("created_by_user_id")
    val createdByUserId: Int? = null,
    @SerializedName("created_at")
    val createdAt: Long
)

data class EventsResponse(
    @SerializedName("events")
    val events: List<EventResponse>
)

// ========== FILE UPLOAD MODELS ==========

data class UploadResponse(
    @SerializedName("path")
    val path: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("size")
    val size: Int,
    @SerializedName("url")
    val url: String
)

// ========== ERROR MODELS ==========

data class ErrorResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("code")
    val code: String? = null
)

// ========== SYNC MODELS ==========

data class SyncStatus(
    val isSynced: Boolean = false,
    val lastSyncTime: Long = 0L,
    val pendingChanges: Int = 0
)
