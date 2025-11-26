package cl.duoc.ourarea.ui

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.ourarea.R
import cl.duoc.ourarea.model.Event
import cl.duoc.ourarea.ui.theme.AppColors
import cl.duoc.ourarea.viewmodel.AuthViewModel
import cl.duoc.ourarea.viewmodel.EventViewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

private val filters = listOf("Todos", "Hoy", "Este fin", "Gratis", "Familia", "Música", "Comida", "Arte", "Deportes")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    eventViewModel: EventViewModel,
    authViewModel: AuthViewModel,
    onEventDetail: (Int) -> Unit,
    onAddEvent: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = screenWidth > screenHeight
    
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    val events by eventViewModel.filteredEvents.collectAsState()
    val error by eventViewModel.error.collectAsState()
    val syncStatus by eventViewModel.syncStatus.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }

    // Ajustar altura del sheet basado en tamaño de pantalla
    val initialSheetHeight = when {
        isLandscape -> 200.dp
        screenHeight < 700.dp -> 280.dp  // Pantallas pequeñas
        screenHeight < 900.dp -> 350.dp  // Pantallas medianas
        else -> 400.dp  // Pantallas grandes
    }

    var sheetHeight by remember { mutableStateOf(initialSheetHeight) }
    val animatedSheetHeight by animateDpAsState(sheetHeight, label = "sheet")
    var showLogoutDialog by remember { mutableStateOf(false) }

    // NUEVO: Forzar recarga cuando se vuelve a la pantalla
    LaunchedEffect(Unit) {
        eventViewModel.syncEventsFromXano()
    }

    // Mostrar mensajes de error y estado
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            eventViewModel.clearError()
        }
    }

    LaunchedEffect(syncStatus) {
        syncStatus?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            eventViewModel.clearSyncStatus()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && userLocation == null) {
            userLocation = getUserLocation(context)
            userLocation?.let { eventViewModel.setUserLocation(it) }
        }
    }

    LaunchedEffect(searchQuery, selectedFilter) {
        eventViewModel.applyFilters(searchQuery, selectedFilter)
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
    Box(Modifier.fillMaxSize().background(AppColors.Background)) {
        when {
            !hasLocationPermission -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card(
                        modifier = Modifier
                            .padding(if (isLandscape) 32.dp else 24.dp)
                            .fillMaxWidth()
                            .widthIn(max = 600.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(AppColors.Surface),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            Modifier.padding(if (isLandscape) 24.dp else 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo OurArea",
                                modifier = Modifier
                                    .size(if (isLandscape) 70.dp else 100.dp)
                                    .padding(bottom = if (isLandscape) 4.dp else 8.dp)
                            )
                            Spacer(Modifier.height(if (isLandscape) 8.dp else 16.dp))
                            Text(
                                "Permiso de ubicación requerido",
                                fontSize = if (isLandscape) 18.sp else 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Primary
                            )
                            Spacer(Modifier.height(if (isLandscape) 6.dp else 10.dp))
                            Text(
                                "Para mostrar eventos cerca de ti, necesitamos acceder a tu ubicación.",
                                fontSize = if (isLandscape) 14.sp else 16.sp,
                                color = androidx.compose.ui.graphics.Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = if (isLandscape) 18.sp else 20.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(Modifier.height(if (isLandscape) 16.dp else 24.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ))
                                },
                                colors = ButtonDefaults.buttonColors(AppColors.Primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isLandscape) 45.dp else 50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Activar ubicación", fontWeight = FontWeight.Bold, color = AppColors.TextOnPrimary)
                            }
                        }
                    }
                }
            }
            userLocation == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            }
            else -> {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        LatLng(userLocation!!.latitude, userLocation!!.longitude), 14f
                    )
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    Marker(
                        state = rememberMarkerState(position = LatLng(userLocation!!.latitude, userLocation!!.longitude)),
                        title = "Tu ubicación"
                    )
                    events.forEach { event ->
                        Marker(
                            state = rememberMarkerState(position = LatLng(event.latitude, event.longitude)),
                            title = event.title
                        )
                    }
                }

                Column(Modifier.fillMaxSize()) {
                    TopBar(searchQuery, { searchQuery = it }, selectedFilter, { selectedFilter = it }, { showLogoutDialog = true })
                    Spacer(Modifier.weight(1f))
                    BottomSheet(
                        animatedSheetHeight,
                        { delta -> sheetHeight = (sheetHeight - delta.dp).coerceIn(200.dp, 600.dp) },
                        events,
                        cameraPositionState,
                        onEventDetail,
                        userLocation

                    )
                }

                // Mostrar FAB solo si el usuario puede crear eventos (Responsive)
                if (currentUser?.canCreateEvents() == true) {
                    FloatingActionButton(
                        onClick = onAddEvent,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                end = when {
                                    isLandscape -> 24.dp
                                    else -> 16.dp
                                },
                                bottom = when {
                                    isLandscape -> (sheetHeight + 16.dp)
                                    screenHeight < 700.dp -> (sheetHeight + 16.dp)
                                    else -> (sheetHeight + 24.dp)
                                }
                            ),
                        containerColor = AppColors.Primary
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = "Agregar evento",
                            tint = AppColors.TextOnPrimary,
                            modifier = Modifier.size(
                                when {
                                    isLandscape -> 22.dp
                                    screenHeight < 700.dp -> 22.dp
                                    else -> 24.dp
                                }
                            )
                        )
                    }
                }
            }
        }

        // Modal de logout
        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = {
                    showLogoutDialog = false
                    onLogout()
                },
                onDismiss = {
                    showLogoutDialog = false
                }
            )
        }
    }
    }
}

@Composable
fun TopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    filter: String,
    onFilterSelect: (String) -> Unit,
    onLogout: () -> Unit
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val isSmallScreen = screenHeight < 700.dp

    Card(
        Modifier
            .fillMaxWidth()
            .padding(when {
                isLandscape -> 12.dp
                isSmallScreen -> 12.dp
                else -> 16.dp
            }),
        shape = RoundedCornerShape(if (isSmallScreen) 12.dp else 16.dp),
        colors = CardDefaults.cardColors(AppColors.MapCardBackground)
    ) {
        Column(Modifier.padding(when {
            isLandscape -> 12.dp
            isSmallScreen -> 12.dp
            else -> 16.dp
        })) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Explorar cerca",
                        fontSize = when {
                            isLandscape -> 18.sp
                            isSmallScreen -> 18.sp
                            else -> 22.sp
                        },
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        "Descubre eventos",
                        fontSize = when {
                            isLandscape -> 12.sp
                            isSmallScreen -> 12.sp
                            else -> 14.sp
                        },
                        color = AppColors.TextSecondary
                    )
                }
                IconButton(onClick = onLogout) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(when {
                            isLandscape -> 20.dp
                            isSmallScreen -> 20.dp
                            else -> 24.dp
                        })
                    )
                }
            }
            Spacer(Modifier.height(when {
                isLandscape -> 8.dp
                isSmallScreen -> 8.dp
                else -> 12.dp
            }))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Buscar eventos...", fontSize = when {
                    isLandscape -> 12.sp
                    isSmallScreen -> 13.sp
                    else -> 14.sp
                }) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(when {
                            isLandscape -> 18.dp
                            isSmallScreen -> 18.dp
                            else -> 20.dp
                        })
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = when {
                    isLandscape -> 12.sp
                    isSmallScreen -> 13.sp
                    else -> 14.sp
                })
            )
            Spacer(Modifier.height(when {
                isLandscape -> 6.dp
                isSmallScreen -> 8.dp
                else -> 12.dp
            }))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(when {
                isLandscape -> 6.dp
                isSmallScreen -> 6.dp
                else -> 8.dp
            })) {
                items(filters) { f ->
                    FilterChip(
                        selected = f == filter,
                        onClick = { onFilterSelect(f) },
                        label = { Text(f, fontSize = when {
                            isLandscape -> 10.sp
                            isSmallScreen -> 10.sp
                            else -> 12.sp
                        }) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (f == filter) AppColors.Primary else AppColors.BackgroundGray,
                            labelColor = if (f == filter) AppColors.TextOnPrimary else AppColors.TextPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun BottomSheet(
    height: Dp,
    onHeightChange: (Float) -> Unit,
    events: List<Event>,
    cameraPositionState: CameraPositionState,
    onEventClick: (Int) -> Unit,
    userLocation: Location?
) {
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, drag ->
                            onHeightChange(drag)
                        }
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(AppColors.TextGray.copy(0.3f), RoundedCornerShape(2.dp))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Eventos cerca de ti",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        "${events.size} eventos encontrados",
                        fontSize = 11.sp,
                        color = AppColors.TextSecondary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ViewList,
                        contentDescription = null,
                        modifier = Modifier
                            .background(AppColors.Primary, CircleShape)
                            .padding(6.dp)
                            .size(18.dp),
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Mi ubicación",
                        modifier = Modifier
                            .background(AppColors.Primary, CircleShape)
                            .padding(6.dp)
                            .size(18.dp)
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    coroutineScope.launch {
                                        userLocation?.let { location ->
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(location.latitude, location.longitude),
                                                    15f
                                                ),
                                                durationMs = 1000
                                            )
                                        }
                                    }
                                }
                            },
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                }
            }

            HorizontalDivider(color = AppColors.BackgroundGray, thickness = 1.dp)

            if (events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = AppColors.TextGray
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No hay eventos cerca",
                            fontSize = 15.sp,
                            color = AppColors.TextGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(events, key = { it.id }) { event ->
                        CompactEventCard(
                            event = event,
                            onCardClick = {
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(event.latitude, event.longitude), 16f
                                        ),
                                        durationMs = 1000
                                    )
                                }
                            },
                            onDetailsClick = {
                                onEventClick(event.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactEventCard(
    event: Event,
    onCardClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val isSmallScreen = screenHeight < 700.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(if (isSmallScreen) 12.dp else 16.dp)),
        shape = RoundedCornerShape(if (isSmallScreen) 12.dp else 16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        onClick = onCardClick
    ) {
        Column {
            Box {
                AsyncImage(
                    model = event.image.ifEmpty { "https://via.placeholder.com/400x140" },
                    contentDescription = event.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(when {
                            isLandscape -> 100.dp
                            isSmallScreen -> 120.dp
                            else -> 140.dp
                        }),
                    contentScale = ContentScale.Crop,
                    error = painterResource(android.R.drawable.ic_menu_gallery)
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(when {
                            isLandscape -> 6.dp
                            isSmallScreen -> 8.dp
                            else -> 10.dp
                        }),
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.Primary
                ) {
                    Text(
                        event.timeInfo,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = when {
                            isLandscape -> 9.sp
                            isSmallScreen -> 9.sp
                            else -> 10.sp
                        },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            horizontal = if (isSmallScreen) 6.dp else 8.dp,
                            vertical = if (isSmallScreen) 3.dp else 4.dp
                        )
                    )
                }
            }

            Column(modifier = Modifier.padding(when {
                isLandscape -> 10.dp
                isSmallScreen -> 10.dp
                else -> 12.dp
            })) {
                Text(
                    event.title,
                    fontSize = when {
                        isLandscape -> 14.sp
                        isSmallScreen -> 14.sp
                        else -> 16.sp
                    },
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                    maxLines = if (isSmallScreen) 1 else 2
                )
                Spacer(Modifier.height(if (isSmallScreen) 3.dp else 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(if (isSmallScreen) 12.dp else 14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        String.format(
                            Locale.getDefault(),
                            "%.1f km • %s",
                            event.distance / 1000,
                            event.description.take(if (isSmallScreen) 20 else 25) +
                            if (event.description.length > (if (isSmallScreen) 20 else 25)) "..." else ""
                        ),
                        fontSize = if (isSmallScreen) 11.sp else 12.sp,
                        color = AppColors.TextSecondary,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(if (isSmallScreen) 6.dp else 10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDetailsClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(when {
                                isLandscape -> 34.dp
                                isSmallScreen -> 36.dp
                                else -> 38.dp
                            }),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "Ver detalles",
                            fontSize = when {
                                isLandscape -> 11.sp
                                isSmallScreen -> 11.sp
                                else -> 12.sp
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = { /* TODO: Favoritos */ },
                        modifier = Modifier
                            .background(
                                AppColors.Primary.copy(alpha = 0.1f),
                                RoundedCornerShape(10.dp)
                            )
                            .size(if (isSmallScreen) 36.dp else 38.dp)
                    ) {
                        Icon(
                            Icons.Default.BookmarkBorder,
                            contentDescription = "Guardar",
                            tint = AppColors.Primary,
                            modifier = Modifier.size(if (isSmallScreen) 18.dp else 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectTapGestures {
                    onDismiss()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures { /* Evitar que el click pase al fondo */ }
                },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono con efecto de fondo
                Box(
                    modifier = Modifier.size(88.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(88.dp),
                        shape = CircleShape,
                        color = AppColors.Primary.copy(alpha = 0.08f)
                    ) {}
                    Surface(
                        modifier = Modifier.size(68.dp),
                        shape = CircleShape,
                        color = AppColors.Primary.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Título
                Text(
                    text = "¿Cerrar Sesión?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                Spacer(Modifier.height(12.dp))

                // Descripción
                Text(
                    text = "¿Estás seguro que deseas cerrar sesión?",
                    fontSize = 16.sp,
                    color = AppColors.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Podrás volver a iniciar sesión cuando quieras.",
                    fontSize = 14.sp,
                    color = AppColors.TextGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = FontWeight.Light
                )

                Spacer(Modifier.height(32.dp))

                // Botones
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Primary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Sí, cerrar sesión",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextOnPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            AppColors.Primary.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "Cancelar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Primary
                        )
                    }
                }
            }
        }
    }
}

@Suppress("MissingPermission")
suspend fun getUserLocation(context: Context): Location? {
    val client = LocationServices.getFusedLocationProviderClient(context)
    return suspendCancellableCoroutine { cont ->
        client.lastLocation.addOnSuccessListener { location ->
            cont.resume(location ?: Location("").apply {
                latitude = -33.4978
                longitude = -70.6165
            })
        }.addOnFailureListener {
            cont.resume(Location("").apply {
                latitude = -33.4978
                longitude = -70.6165
            })
        }
    }
}
