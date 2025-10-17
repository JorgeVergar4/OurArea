package cl.duoc.ourarea.ui

import android.content.Context
import android.location.Location
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.Locale
import kotlinx.coroutines.suspendCancellableCoroutine
import cl.duoc.ourarea.viewmodel.EventViewModel
import cl.duoc.ourarea.model.Event
import cl.duoc.ourarea.ui.theme.AppColors
import kotlin.coroutines.resume
import kotlin.math.max
import kotlin.math.min

// Paleta de colores
private val PrimaryTeal = Color(0xFF19B6B6)
private val DarkTeal = Color(0xFF00796B)
private val LightTeal = Color(0xFFE0F7FA)
private val BackgroundGray = Color(0xFFF5F7FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF1A2524)
private val TextGray = Color(0xFF666666)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    eventViewModel: EventViewModel = viewModel(),
    onEventDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    val events by eventViewModel.filteredEvents.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Hoy", "Este fin", "Gratis", "Familia", "Música", "Comida", "Arte")

    // Estado del BottomSheet (altura mínima 200dp, máxima 600dp)
    var sheetHeight by remember { mutableStateOf(350.dp) }
    val animatedSheetHeight by animateDpAsState(targetValue = sheetHeight, label = "sheet_height")

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && userLocation == null) {
            userLocation = getUserLocation(context)
            userLocation?.let { eventViewModel.setUserLocation(it) }
        }
    }

    LaunchedEffect(searchQuery, selectedFilter) {
        eventViewModel.applyFilters(searchQuery, selectedFilter)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !hasLocationPermission -> {
                LocationPermissionScreen(onGranted = { hasLocationPermission = true })
            }
            userLocation == null -> {
                LoadingLocationSection()
            }
            else -> {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        LatLng(userLocation!!.latitude, userLocation!!.longitude), 14f
                    )
                }

                // Mapa de fondo
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false
                    )
                ) {
                    Marker(
                        state = remember {MarkerState(position = LatLng(userLocation!!.latitude, userLocation!!.longitude))},
                        title = "Tu ubicación"
                    )
                    events.forEach { event ->
                        Marker(
                            state = MarkerState(position = LatLng(event.latitude, event.longitude)),
                            title = event.title,
                            snippet = event.description,
                            onInfoWindowClick = { onEventDetail(event.id) }
                        )
                    }
                }

                // Layout principal con BottomSheet deslizable
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header flotante
                    TopSectionCompact(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        filters = filters,
                        selectedFilter = selectedFilter,
                        onFilterSelect = { selectedFilter = it }
                    )

                    Spacer(Modifier.weight(1f))

                    // BottomSheet deslizable
                    DraggableBottomSheet(
                        height = animatedSheetHeight,
                        onHeightChange = { delta ->
                            val newHeight = sheetHeight - delta.dp
                            sheetHeight = max(200.dp, min(600.dp, newHeight))
                        },
                        events = events,
                        onEventClick = onEventDetail
                    )
                }
            }
        }
    }
}

@Composable
fun DraggableBottomSheet(
    height: androidx.compose.ui.unit.Dp,
    onHeightChange: (Float) -> Unit,
    events: List<Event>,
    onEventClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column {
            // Drag handle con detección de gestos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            onHeightChange(dragAmount)
                        }
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(TextGray.copy(0.3f), RoundedCornerShape(2.dp))
                )
            }

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Eventos cerca de ti", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("${events.size} eventos encontrados", fontSize = 11.sp, color = TextGray)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.ViewList,
                        contentDescription = null,
                        modifier = Modifier
                            .background(PrimaryTeal, CircleShape)
                            .padding(6.dp)
                            .size(18.dp),
                        tint = Color.White
                    )
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier
                            .background(BackgroundGray, CircleShape)
                            .padding(6.dp)
                            .size(18.dp),
                        tint = TextGray
                    )
                }
            }

            HorizontalDivider(color = BackgroundGray, thickness = 1.dp)

            // Lista de eventos
            if (events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(56.dp), tint = TextGray)
                        Spacer(Modifier.height(12.dp))
                        Text("No hay eventos cerca", fontSize = 15.sp, color = TextGray, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(events, key = { it.id }) { event ->
                        CompactEventCard(event = event, onDetailsClick = { onEventClick(event.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun TopSectionCompact(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    filters: List<String>,
    selectedFilter: String,
    onFilterSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Explorar cerca", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("Descubre eventos increíbles", fontSize = 12.sp, color = TextGray)
                }
                IconButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .background(LightTeal, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Perfil", tint = DarkTeal)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Buscar eventos...", fontSize = 13.sp, color = TextGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryTeal) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BackgroundGray,
                        unfocusedContainerColor = BackgroundGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
                IconButton(
                    onClick = { /* Filtros */ },
                    modifier = Modifier
                        .background(Brush.horizontalGradient(listOf(PrimaryTeal, DarkTeal)), CircleShape)
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Filtros", tint = Color.White)
                }
            }

            Spacer(Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { filter ->
                    FilterChip(
                        selected = filter == selectedFilter,
                        onClick = { onFilterSelect(filter) },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (filter == selectedFilter) PrimaryTeal else BackgroundGray,
                            labelColor = if (filter == selectedFilter) Color.White else TextDark,
                            selectedContainerColor = PrimaryTeal,
                            selectedLabelColor = Color.White
                        ),
                        border = null
                    )
                }
            }
        }
    }
}

@Composable
fun CompactEventCard(event: Event, onDetailsClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = event.image,
                    contentDescription = event.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                    error = painterResource(android.R.drawable.ic_menu_report_image)
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryTeal
                ) {
                    Text(
                        event.timeInfo,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(event.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        String.format(Locale.getDefault(), "%.1f km • %s", event.distance / 1000, event.description.take(18)),
                        fontSize = 12.sp,
                        color = TextGray,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDetailsClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Ver detalles", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .background(LightTeal, RoundedCornerShape(10.dp))
                            .size(38.dp)
                    ) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Guardar", tint = DarkTeal)
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingLocationSection() {
    Box(modifier = Modifier.fillMaxSize().background(BackgroundGray), contentAlignment = Alignment.Center) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = PrimaryTeal)
                Spacer(Modifier.height(16.dp))
                Text("Obteniendo tu ubicación...", fontSize = 15.sp, color = TextDark, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Suppress("MissingPermission")
suspend fun getUserLocation(context: Context): Location? {
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    return suspendCancellableCoroutine { cont ->
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                cont.resume(location)
            } else {
                fusedLocationProviderClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null
                )
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(null) }
            }
        }.addOnFailureListener { cont.resume(null) }
    }
}
