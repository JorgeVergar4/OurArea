package cl.duoc.ourarea.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<Event>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
}
