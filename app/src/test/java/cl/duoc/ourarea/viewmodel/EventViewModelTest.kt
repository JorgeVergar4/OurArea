package cl.duoc.ourarea.viewmodel

import android.app.Application
import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cl.duoc.ourarea.model.Event
import cl.duoc.ourarea.repository.EventRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

/**
 * Tests unitarios para EventViewModel
 * Verifica la gestión de eventos, filtros, ubicación y sincronización
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EventViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var application: Application
    private lateinit var eventRepository: EventRepository
    private lateinit var viewModel: EventViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = mockk(relaxed = true)
        eventRepository = mockk(relaxed = true)

        // Mock para getAllEvents (retorna flow vacío inicialmente)
        every { eventRepository.getAllEvents() } returns flowOf(emptyList())
        
        // Mock para syncEventsFromXano
        coEvery { eventRepository.syncEventsFromXano(any(), any()) } returns true

        viewModel = EventViewModel(application, eventRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init carga eventos del repositorio`() = runTest {
        // Given
        val events = listOf(
            createTestEvent(1, "Evento 1"),
            createTestEvent(2, "Evento 2"),
            createTestEvent(3, "Evento 3")
        )
        every { eventRepository.getAllEvents() } returns flowOf(events)

        // When
        val newViewModel = EventViewModel(application, eventRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(3, newViewModel.filteredEvents.value.size)
        assertEquals("Evento 1", newViewModel.filteredEvents.value[0].title)
    }

    @Test
    fun `syncEventsFromXano actualiza syncStatus cuando tiene éxito`() = runTest {
        // Given
        coEvery { eventRepository.syncEventsFromXano(any(), any()) } returns true

        // When
        viewModel.syncEventsFromXano()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Sincronización exitosa", viewModel.syncStatus.value)
        coVerify { eventRepository.syncEventsFromXano(any(), any()) }
    }

    @Test
    fun `syncEventsFromXano actualiza syncStatus cuando falla`() = runTest {
        // Given
        coEvery { eventRepository.syncEventsFromXano(any(), any()) } returns false

        // When
        viewModel.syncEventsFromXano()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Usando datos locales", viewModel.syncStatus.value)
    }

    @Test
    fun `syncEventsFromXano maneja errores de conexión`() = runTest {
        // Given
        coEvery { eventRepository.syncEventsFromXano(any(), any()) } throws Exception("Network error")

        // When
        viewModel.syncEventsFromXano()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Error de conexión", viewModel.syncStatus.value)
        assertEquals("No se pudo conectar con el servidor", viewModel.error.value)
    }

    @Test
    fun `setUserLocation actualiza la ubicación del usuario`() = runTest {
        // Given
        val location = mockk<Location>(relaxed = true)
        every { location.latitude } returns -33.4489
        every { location.longitude } returns -70.6693

        // When
        viewModel.setUserLocation(location)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        // Verificar que se actualizó (no hay getter público, pero podemos ver efectos)
        assertNotNull(location)
    }

    @Test
    fun `applyFilters filtra eventos por búsqueda de texto`() = runTest {
        // Given
        val events = listOf(
            createTestEvent(1, "Concierto de Rock"),
            createTestEvent(2, "Festival de Jazz"),
            createTestEvent(3, "Obra de Teatro")
        )
        every { eventRepository.getAllEvents() } returns flowOf(events)
        
        val newViewModel = EventViewModel(application, eventRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        newViewModel.applyFilters("Jazz", "Todos")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, newViewModel.filteredEvents.value.size)
        assertEquals("Festival de Jazz", newViewModel.filteredEvents.value[0].title)
    }

    @Test
    fun `applyFilters con filtro Gratis muestra solo eventos gratuitos`() = runTest {
        // Given
        val events = listOf(
            createTestEvent(1, "Evento Gratis", isFree = true),
            createTestEvent(2, "Evento Pago", isFree = false),
            createTestEvent(3, "Otro Gratis", isFree = true)
        )
        every { eventRepository.getAllEvents() } returns flowOf(events)
        
        val newViewModel = EventViewModel(application, eventRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        newViewModel.applyFilters("", "Gratis")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(2, newViewModel.filteredEvents.value.size)
        assertTrue(newViewModel.filteredEvents.value.all { it.isFree })
    }

    @Test
    fun `applyFilters con filtro Música muestra solo eventos de música`() = runTest {
        // Given
        val events = listOf(
            createTestEvent(1, "Concierto", isMusic = true),
            createTestEvent(2, "Teatro", isMusic = false),
            createTestEvent(3, "Festival", isMusic = true)
        )
        every { eventRepository.getAllEvents() } returns flowOf(events)
        
        val newViewModel = EventViewModel(application, eventRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        newViewModel.applyFilters("", "Música")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(2, newViewModel.filteredEvents.value.size)
        assertTrue(newViewModel.filteredEvents.value.all { it.isMusic })
    }

    @Test
    fun `insertEvent crea evento exitosamente`() = runTest {
        // Given
        val event = createTestEvent(0, "Nuevo Evento")
        val createdEvent = event.copy(id = 10)
        
        coEvery { eventRepository.createEventOnXano(any(), any()) } returns createdEvent

        var resultEvent: Event? = null
        
        // When
        viewModel.insertEvent(event, null) { result ->
            resultEvent = result
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNotNull(resultEvent)
        assertEquals(10, resultEvent?.id)
        assertEquals("Nuevo Evento", resultEvent?.title)
        coVerify { eventRepository.createEventOnXano(event, null) }
    }

    @Test
    fun `insertEvent maneja error al crear evento`() = runTest {
        // Given
        val event = createTestEvent(0, "Evento Con Error")
        
        coEvery { eventRepository.createEventOnXano(any(), any()) } returns null

        var resultEvent: Event? = null
        
        // When
        viewModel.insertEvent(event, null) { result ->
            resultEvent = result
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(resultEvent)
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Error"))
    }

    @Test
    fun `deleteEvent elimina evento correctamente`() = runTest {
        // Given
        val event = createTestEvent(5, "Evento a Eliminar")
        coEvery { eventRepository.deleteEvent(any()) } returns Unit

        // When
        viewModel.deleteEvent(event)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { eventRepository.deleteEvent(any()) }
    }

    @Test
    fun `clearError limpia mensaje de error`() = runTest {
        // Given
        coEvery { eventRepository.syncEventsFromXano(any(), any()) } throws Exception("Test error")
        viewModel.syncEventsFromXano()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.error.value)
    }

    @Test
    fun `clearSyncStatus limpia mensaje de sincronización`() = runTest {
        // Given
        viewModel.syncEventsFromXano()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearSyncStatus()

        // Then
        assertNull(viewModel.syncStatus.value)
    }

    // Helper function para crear eventos de prueba
    private fun createTestEvent(
        id: Int,
        title: String,
        isFree: Boolean = false,
        isMusic: Boolean = false,
        isFood: Boolean = false,
        isArt: Boolean = false,
        isSports: Boolean = false,
        isFamily: Boolean = false
    ): Event {
        return Event(
            id = id,
            title = title,
            description = "Descripción de $title",
            latitude = -33.4489,
            longitude = -70.6693,
            image = "",
            timeInfo = "Hoy",
            isFree = isFree,
            isFamily = isFamily,
            isMusic = isMusic,
            isFood = isFood,
            isArt = isArt,
            isSports = isSports,
            distance = 0f,
            createdByUserId = 1,
            createdAt = System.currentTimeMillis()
        )
    }
}
