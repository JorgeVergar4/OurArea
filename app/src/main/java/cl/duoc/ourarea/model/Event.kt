package cl.duoc.ourarea.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val image: String,
    val timeInfo: String,
    val isFree: Boolean,
    val isFamily: Boolean,
    val isMusic: Boolean,
    val isFood: Boolean,
    val isArt: Boolean,
    val isSports: Boolean,
    val distance: Float = 0f,
    val createdByUserId: Int = 0,  // NUEVO: ID del usuario que creó el evento
    val createdAt: Long = System.currentTimeMillis()  // NUEVO: Timestamp de creación
)
