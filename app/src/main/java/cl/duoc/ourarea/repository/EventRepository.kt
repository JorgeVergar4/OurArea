package cl.duoc.ourarea.repository

import android.content.Context
import cl.duoc.ourarea.model.AppDatabase
import cl.duoc.ourarea.model.Event
import kotlinx.coroutines.flow.Flow

class EventRepository(context: Context) {
    private val eventDao = AppDatabase.getInstance(context).eventDao()

    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()

    suspend fun insertEvents(events: List<Event>) = eventDao.insertEvents(events)
}

