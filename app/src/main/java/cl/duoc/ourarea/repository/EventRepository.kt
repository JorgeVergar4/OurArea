package cl.duoc.ourarea.repository

import cl.duoc.ourarea.model.Event
import cl.duoc.ourarea.model.EventDao
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {
    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()
    suspend fun insertEvents(events: List<Event>) = eventDao.insertEvents(events)
    suspend fun insertEvent(event: Event) = eventDao.insertEvent(event)
    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)
    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)
    suspend fun deleteAllEvents() = eventDao.deleteAllEvents()

}
