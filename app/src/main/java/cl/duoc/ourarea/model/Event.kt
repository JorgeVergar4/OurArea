package cl.duoc.ourarea.model


data class Event(
    val id: Int,
    val title: String,
    val description: String,
    val imageRes: Int,
    val distanceText: String,
    val infoText: String
)