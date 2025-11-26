package cl.duoc.ourarea.api.models

import cl.duoc.ourarea.model.Event
import cl.duoc.ourarea.model.User

// ========== USER MAPPERS ==========

fun UserResponse.toUser(): User {
    return User(
        id = this.id,
        email = this.email,
        password = "", // Password shouldn't come from API
        name = this.name,
        role = this.role
    )
}

fun User.toUserResponse(): UserResponse {
    return UserResponse(
        id = this.id,
        email = this.email,
        name = this.name,
        role = this.role,
        createdAt = System.currentTimeMillis()
    )
}

// ========== EVENT MAPPERS ==========

fun EventResponse.toEvent(): Event {
    // Handle image URL - prefer imageUrl if available, otherwise use image object's URL
    val eventImageUrl = when {
        !imageUrl.isNullOrEmpty() -> imageUrl
        image?.url?.isNotEmpty() == true -> image.url
        else -> ""
    }

    // Handle user ID - Xano uses user_id, but we support both
    val eventUserId = createdByUserId ?: userId ?: 0

    return Event(
        id = this.id,
        title = this.title,
        description = this.description,
        latitude = this.latitude,
        longitude = this.longitude,
        image = eventImageUrl,
        timeInfo = this.timeInfo,
        isFree = this.isFree,
        isFamily = this.isFamily,
        isMusic = this.isMusic,
        isFood = this.isFood,
        isArt = this.isArt,
        isSports = this.isSports,
        distance = 0f, // Will be calculated locally
        createdByUserId = eventUserId,
        createdAt = this.createdAt
    )
}

fun Event.toEventRequest(): EventRequest {
    return EventRequest(
        title = this.title,
        description = this.description,
        latitude = this.latitude,
        longitude = this.longitude,
        imageUrl = this.image,
        timeInfo = this.timeInfo,
        isFree = this.isFree,
        isFamily = this.isFamily,
        isMusic = this.isMusic,
        isFood = this.isFood,
        isArt = this.isArt,
        isSports = this.isSports,
        userId = this.createdByUserId
    )
}

fun Event.toEventResponse(): EventResponse {
    return EventResponse(
        id = this.id,
        title = this.title,
        description = this.description,
        latitude = this.latitude,
        longitude = this.longitude,
        imageUrl = this.image,
        timeInfo = this.timeInfo,
        isFree = this.isFree,
        isFamily = this.isFamily,
        isMusic = this.isMusic,
        isFood = this.isFood,
        isArt = this.isArt,
        isSports = this.isSports,
        createdByUserId = this.createdByUserId,
        createdAt = this.createdAt
    )
}
