package cl.duoc.ourarea.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import cl.duoc.ourarea.ui.theme.AppColors
import cl.duoc.ourarea.viewmodel.AuthViewModel
import cl.duoc.ourarea.viewmodel.EventViewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.io.File
import java.util.Locale

// Función para abrir Google Maps con navegación
fun openGoogleMapsNavigation(context: Context, latitude: Double, longitude: Double) {
    try {
        // Crear URI para navegación con coordenadas
        val uri = "google.navigation:q=$latitude,$longitude".toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")
        context.startActivity(mapIntent)
    } catch (_: ActivityNotFoundException) {
        // Si Google Maps no está instalado, abrir en el navegador
        try {
            val uri = "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude".toUri()
            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Int,
    eventViewModel: EventViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val events by eventViewModel.filteredEvents.collectAsState()
    val event = events.find { it.id == eventId }
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (event == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Evento no encontrado", color = AppColors.TextSecondary)
        }
        return
    }

    // Verificar si el usuario actual puede eliminar este evento
    val canDelete = currentUser?.id == event.createdByUserId

    Box(Modifier.fillMaxSize().background(AppColors.Background)) {
        Column(Modifier.fillMaxSize()) {
            // Top Bar con botón de eliminar
            TopAppBar(
                title = { Text("Detalles del Evento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = AppColors.Primary
                        )
                    }
                },
                actions = {
                    // Botón de eliminar solo visible si el usuario es el creador
                    if (canDelete) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar evento",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Surface
                )
            )

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Imagen del evento (soporte para rutas locales y URLs)
                AsyncImage(
                    model = if (event.image.startsWith("/")) {
                        // Es una ruta de archivo local
                        File(event.image)
                    } else {
                        // Es una URL
                        event.image.ifEmpty { "https://via.placeholder.com/400x200" }
                    },
                    contentDescription = event.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop,
                    error = painterResource(android.R.drawable.ic_menu_gallery),
                    placeholder = painterResource(android.R.drawable.ic_menu_gallery)
                )

                // Contenido del evento
                Column(Modifier.padding(20.dp)) {
                    // Título
                    Text(
                        text = event.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )

                    Spacer(Modifier.height(12.dp))

                    // Categorías
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (event.isFree) CategoryChip("Gratis", AppColors.Secondary)
                        if (event.isFamily) CategoryChip("Familia", AppColors.GoogleBlue)
                        if (event.isMusic) CategoryChip("Música", AppColors.GoogleYellow)
                        if (event.isFood) CategoryChip("Comida", AppColors.GoogleRed)
                        if (event.isArt) CategoryChip("Arte", AppColors.Primary)
                        if (event.isSports) CategoryChip("Deportes", AppColors.Secondary)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Información básica
                    InfoRow(Icons.Default.CalendarToday, event.timeInfo)
                    Spacer(Modifier.height(8.dp))
                    InfoRow(
                        Icons.Default.LocationOn,
                        String.format(
                            Locale.getDefault(),
                            "%.1f km de distancia",
                            event.distance / 1000
                        )
                    )

                    Spacer(Modifier.height(20.dp))

                    // Descripción
                    Text(
                        text = "Descripción",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = event.description,
                        fontSize = 16.sp,
                        color = AppColors.TextSecondary,
                        lineHeight = 24.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    // Ubicación
                    Text(
                        text = "Ubicación",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Spacer(Modifier.height(12.dp))

                    // Mapa de ubicación
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(
                            LatLng(event.latitude, event.longitude), 15f
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = false,
                                scrollGesturesEnabled = false,
                                zoomGesturesEnabled = false
                            )
                        ) {
                            Marker(
                                state = rememberMarkerState(
                                    position = LatLng(event.latitude, event.longitude)
                                ),
                                title = event.title
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Botón "Cómo llegar"
                    Button(
                        onClick = {
                            openGoogleMapsNavigation(
                                context = context,
                                latitude = event.latitude,
                                longitude = event.longitude
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(AppColors.Primary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.Default.Directions,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Cómo llegar", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }

        // Diálogo de confirmación para eliminar
        if (showDeleteDialog) {
            EventDeleteDialog(
                eventTitle = event.title,
                onConfirm = {
                    eventViewModel.deleteEvent(event) {
                        showDeleteDialog = false
                        onNavigateBack()
                    }
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}


@Composable
fun EventDeleteDialog(  // CAMBIADO AQUÍ
    eventTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Eliminar Evento",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("¿Estás seguro de que deseas eliminar \"$eventTitle\"? Esta acción no se puede deshacer.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


@Composable
fun CategoryChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = AppColors.Primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            color = AppColors.TextSecondary
        )
    }
}
