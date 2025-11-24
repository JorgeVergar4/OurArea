package cl.duoc.ourarea.repository

import cl.duoc.ourarea.model.Event
import cl.duoc.ourarea.model.EventDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para EventRepository
 * Verifica la correcta delegación de operaciones al DAO
 */
class EventRepositoryTest {

    private lateinit var eventDao: EventDao
    private lateinit var eventRepository: EventRepository

    @Before
    fun setup() {
        eventDao = mockk()
        eventRepository = EventRepository(eventDao)
    }

    @Test
    fun `getAllEvents delega al DAO y retorna Flow`() = runTest {
        // Given
        val eventsList = listOf(
            createTestEvent(1, "Evento 1"),
            createTestEvent(2, "Evento 2"),
            createTestEvent(3, "Evento 3")
        )
        val eventsFlow = flowOf(eventsList)
        coEvery { eventDao.getAllEvents() } returns eventsFlow

        // When
        val result = eventRepository.getAllEvents()

        // Then
        result.collect { events ->
            assertEquals(eventsList, events)
            assertEquals(3, events.size)
        }
        coVerify(exactly = 1) { eventDao.getAllEvents() }
    }

    @Test
    fun `getEventById delega al DAO correctamente`() = runTest {
        // Given
        val eventId = 1
        val expectedEvent = createTestEvent(eventId, "Test Event")
        coEvery { eventDao.getEventById(eventId) } returns expectedEvent

        // When
        val result = eventRepository.getEventById(eventId)

        // Then
        assertEquals(expectedEvent, result)
        coVerify(exactly = 1) { eventDao.getEventById(eventId) }
    }

    @Test
    fun `getEventById retorna null cuando no existe`() = runTest {
        // Given
        val eventId = 999
        coEvery { eventDao.getEventById(eventId) } returns null

        // When
        val result = eventRepository.getEventById(eventId)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { eventDao.getEventById(eventId) }
    }

    @Test
    fun `insertEvent delega al DAO y retorna ID`() = runTest {
        // Given
        val event = createTestEvent(0, "Nuevo Evento")
        val expectedId = 10L
        coEvery { eventDao.insertEvent(event) } returns expectedId

        // When
        val result = eventRepository.insertEvent(event)

        // Then
        assertEquals(expectedId, result)
        coVerify(exactly = 1) { eventDao.insertEvent(event) }
    }

    @Test
    fun `insertEvents delega al DAO para múltiples eventos`() = runTest {
        // Given
        val events = listOf(
            createTestEvent(0, "Evento 1"),
            createTestEvent(0, "Evento 2"),
            createTestEvent(0, "Evento 3")
        )
        coEvery { eventDao.insertEvents(events) } returns Unit

        // When
        eventRepository.insertEvents(events)

        // Then
        coVerify(exactly = 1) { eventDao.insertEvents(events) }
    }

    @Test
    fun `updateEvent delega al DAO correctamente`() = runTest {
        // Given
        val event = createTestEvent(1, "Evento Actualizado")
        coEvery { eventDao.updateEvent(event) } returns Unit

        // When
        eventRepository.updateEvent(event)

        // Then
        coVerify(exactly = 1) { eventDao.updateEvent(event) }
    }

    @Test
    fun `deleteEvent delega al DAO correctamente`() = runTest {
        // Given
        val event = createTestEvent(1, "Evento a Eliminar")
        coEvery { eventDao.deleteEvent(event) } returns Unit

        // When
        eventRepository.deleteEvent(event)

        // Then
        coVerify(exactly = 1) { eventDao.deleteEvent(event) }
    }

    @Test
    fun `deleteEventById delega al DAO con ID correcto`() = runTest {
        // Given
        val eventId = 5
        coEvery { eventDao.deleteEventById(eventId) } returns Unit

        // When
        eventRepository.deleteEventById(eventId)

        // Then
        coVerify(exactly = 1) { eventDao.deleteEventById(eventId) }
    }

    @Test
    fun `deleteAllEvents delega al DAO correctamente`() = runTest {
        // Given
        coEvery { eventDao.deleteAllEvents() } returns Unit

        // When
        eventRepository.deleteAllEvents()

        // Then
        coVerify(exactly = 1) { eventDao.deleteAllEvents() }
    }

    // Helper para crear eventos de prueba
    private fun createTestEvent(id: Int, title: String): Event {
        return Event(
            id = id,
            title = title,
            description = "Descripción de $title",
            latitude = -33.4489,
            longitude = -70.6693,
            image = "/path/to/image.jpg",
            timeInfo = "Hoy",
            isFree = true,
            isFamily = false,
            isMusic = true,
            isFood = false,
            isArt = false,
            isSports = false,
            distance = 1500f,
            createdByUserId = 1,
            createdAt = System.currentTimeMillis()
        )
    }
}
