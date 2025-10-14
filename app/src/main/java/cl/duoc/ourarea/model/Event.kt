package cl.duoc.ourarea.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val image: String,
    val description: String,
    val distance: Double,
    val timeInfo: String
)
