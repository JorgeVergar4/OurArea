package cl.duoc.ourarea.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Ignore

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val image: String,
    val timeInfo: String,
    val isFree: Boolean = false,
    val isFamily: Boolean = false,
    val isMusic: Boolean = false,
    val isFood: Boolean = false,
    val isArt: Boolean = false,
    val isSports: Boolean = false
) {
    @Ignore
    var distance: Float = 0f
}

