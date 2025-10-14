package cl.duoc.ourarea.ui

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.ChipDefaults
import coil.compose.AsyncImage
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.suspendCancellableCoroutine
import cl.duoc.ourarea.viewmodel.EventViewModel
import cl.duoc.ourarea.model.Event
import kotlin.coroutines.resume

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    eventViewModel: EventViewModel = viewModel(),
    onEventDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<Location?>(null) }
    val events by eventViewModel.events.collectAsState()
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Solicita permiso y obtiene ubicación
    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted && userLocation == null) {
            userLocation = getUserLocation(context)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FEFD) // Color de fondo general
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Estado: Permiso denegado
                !permissionState.status.isGranted -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                    ) {
                        Text("Para mostrar eventos cerca de ti, activa el permiso de ubicación.", fontSize = 18.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { permissionState.launchPermissionRequest() }) {
                            Text("Activar ubicación")
                        }
                    }
                }
                // Estado: Obteniendo ubicación
                userLocation == null -> {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Obteniendo tu ubicación...", fontSize = 16.sp)
                    }
                }
                // Estado: Permiso concedido y ubicación obtenida
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
                        uiSettings = MapUiSettings(zoomControlsEnabled = false)
                    ) {
                        Marker(
                            state = MarkerState(position = LatLng(userLocation!!.latitude, userLocation!!.longitude)),
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

                    // Contenido superpuesto (búsqueda y lista)
                    Column(Modifier.fillMaxSize()) {
                        TopSection()
                        Spacer(Modifier.weight(1f))
                        EventListSection(events = events, onEventClick = onEventDetail)
                    }
                }
            }
        }
    }
}

@Composable
fun TopSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Explorar cerca",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A2524)
            )
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_sort_by_size),
                contentDescription = "Opciones",
                tint = Color(0xFF008080)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Buscar eventos, ferias...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { /* Acción de filtro */ },
                modifier = Modifier
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filtros",
                    tint = Color(0xFF008080)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val filters = listOf("Hoy", "Este fin", "Gratis", "Familia", "Música")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                FilterChip(
                    selected = false, // Cambia a tu lógica de selección si lo necesitas
                    onClick = { /* Acción para el filtro */ },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        labelColor = Color(0xFF1A2524)
                    ),
                    border = null,
                    modifier = Modifier.height(36.dp)
                )
            }
        }
    }
}

@Composable
fun EventListSection(events: List<Event>, onEventClick: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(Modifier.heightIn(max = 450.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Eventos cerca de ti",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Vista de lista",
                        modifier = Modifier
                            .background(Color(0xFFE0F5F5), CircleShape)
                            .padding(6.dp),
                        tint = Color(0xFF008080)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Vista de mapa",
                        tint = Color.Gray
                    )
                }
            }

            if (events.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(32.dp).fillMaxWidth()) {
                    Text("No hay eventos cerca de ti.", fontSize = 16.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(events) { event ->
                        EventCard(event = event, onDetailsClick = { onEventClick(event.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Event, onDetailsClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = event.image,
                contentDescription = event.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery), // Placeholder
                error = painterResource(id = android.R.drawable.ic_menu_report_image) // Error image
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = event.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Ubicación", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f km • %s", event.distance / 1000, event.description.take(20)),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE0F5F5)
                    ) {
                        Text(
                            text = event.timeInfo,
                            color = Color(0xFF008080),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDetailsClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFFFFF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Detalles", color = Color(0xFF008080))
                    }
                    Button(
                        onClick = { /* TODO: Lógica para guardar evento */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFFFFF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar", color = Color(0xFF008080))
                    }
                }
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
                    .addOnSuccessListener { freshLocation -> cont.resume(freshLocation) }
                    .addOnFailureListener { cont.resume(null) }
            }
        }.addOnFailureListener { cont.resume(null) }
    }
}