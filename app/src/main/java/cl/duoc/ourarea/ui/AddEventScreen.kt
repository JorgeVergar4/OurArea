package cl.duoc.ourarea.ui

import android.content.Context
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import cl.duoc.ourarea.model.Event
import cl.duoc.ourarea.ui.theme.AppColors
import cl.duoc.ourarea.viewmodel.AuthViewModel
import cl.duoc.ourarea.viewmodel.EventViewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddEventScreen(
    eventViewModel: EventViewModel,
    authViewModel: AuthViewModel,  // AGREGADO: Necesitas pasar esto
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = screenWidth > screenHeight
    
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    // Permiso de galería (varía según versión de Android)
    val storagePermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val storagePermissionState = rememberPermissionState(storagePermission)

    val currentUser by authViewModel.currentUser.collectAsState()  // AGREGADO
    val error by eventViewModel.error.collectAsState()
    val syncStatus by eventViewModel.syncStatus.collectAsState()
    val isLoading by eventViewModel.isLoading.collectAsState()

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

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var timeInfo by remember { mutableStateOf("Hoy") }
    var isFree by remember { mutableStateOf(false) }
    var isFamily by remember { mutableStateOf(false) }
    var isMusic by remember { mutableStateOf(false) }
    var isFood by remember { mutableStateOf(false) }
    var isArt by remember { mutableStateOf(false) }
    var isSports by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showMapPicker by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var showError by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Obtener ubicación actual
    LaunchedEffect(Unit) {
        val location = getCurrentUserLocation(context)
        if (location != null) {
            selectedLocation = LatLng(location.latitude, location.longitude)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> imageUri = uri }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            imageUri = cameraImageUri
        }
    }

    if (showMapPicker && selectedLocation != null) {
        MapLocationPicker(
            initialLocation = selectedLocation!!,
            onLocationSelected = { location ->
                selectedLocation = location
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

    Box(Modifier.fillMaxSize().background(AppColors.Background)) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Agregar Evento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background
                )
            )

            Card(
                Modifier.fillMaxSize().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(AppColors.MapCardBackground)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título del evento") },
                        leadingIcon = { Icon(Icons.Default.Event, null, tint = AppColors.Primary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        leadingIcon = { Icon(Icons.Default.Description, null, tint = AppColors.Primary) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )

                    Column {
                        Text("Imagen del evento", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))

                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    when {
                                        storagePermissionState.status.isGranted -> {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                        else -> {
                                            showPermissionDialog = true
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(AppColors.Primary)
                            ) {
                                Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Galería")
                            }

                            OutlinedButton(
                                onClick = {
                                    when {
                                        cameraPermissionState.status.isGranted -> {
                                            try {
                                                cameraImageUri = context.getImageUri()
                                                cameraImageUri?.let { uri ->
                                                    cameraLauncher.launch(uri)
                                                } ?: run {
                                                    showError = true
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                showError = true
                                            }
                                        }
                                        else -> {
                                            cameraPermissionState.launchPermissionRequest()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Camera, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Cámara")
                            }
                        }
                    }

                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = timeInfo,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cuándo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("Hoy", "Este fin", "Próxima semana").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        timeInfo = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Selector de Ubicación
                    Column {
                        Text("Ubicación del evento", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clickable { showMapPicker = true },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Box {
                                if (selectedLocation != null) {
                                    val cameraPositionState = rememberCameraPositionState {
                                        position = CameraPosition.fromLatLngZoom(selectedLocation!!, 15f)
                                    }
                                    GoogleMap(
                                        modifier = Modifier.fillMaxSize(),
                                        cameraPositionState = cameraPositionState,
                                        uiSettings = MapUiSettings(
                                            scrollGesturesEnabled = false,
                                            zoomGesturesEnabled = false
                                        )
                                    ) {
                                        Marker(state = rememberMarkerState(position = selectedLocation!!))
                                    }
                                }
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = AppColors.Primary.copy(alpha = 0.9f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.MyLocation,
                                            null,
                                            tint = androidx.compose.ui.graphics.Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Click para seleccionar",
                                            color = androidx.compose.ui.graphics.Color.White,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text("Categorías", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Text("Selecciona las categorías que aplican", fontSize = 13.sp, color = AppColors.TextSecondary, modifier = Modifier.padding(bottom = 8.dp))

                    // Categorías como chips modernos
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CategoryChip(
                                label = "Gratis",
                                icon = Icons.Default.Money,
                                selected = isFree,
                                onSelectedChange = { isFree = it },
                                modifier = Modifier.weight(1f)
                            )
                            CategoryChip(
                                label = "Familia",
                                icon = Icons.Default.People,
                                selected = isFamily,
                                onSelectedChange = { isFamily = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CategoryChip(
                                label = "Música",
                                icon = Icons.Default.MusicNote,
                                selected = isMusic,
                                onSelectedChange = { isMusic = it },
                                modifier = Modifier.weight(1f)
                            )
                            CategoryChip(
                                label = "Comida",
                                icon = Icons.Default.Restaurant,
                                selected = isFood,
                                onSelectedChange = { isFood = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CategoryChip(
                                label = "Arte",
                                icon = Icons.Default.Palette,
                                selected = isArt,
                                onSelectedChange = { isArt = it },
                                modifier = Modifier.weight(1f)
                            )
                            CategoryChip(
                                label = "Deportes",
                                icon = Icons.Default.SportsBaseball,
                                selected = isSports,
                                onSelectedChange = { isSports = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Botón de guardar mejorado
                    Button(
                        onClick = {
                            selectedLocation?.let { location ->
                                // Convertir URI a archivo temporal si existe
                                val imageFile = imageUri?.let { uri ->
                                    context.uriToFile(uri)
                                }

                                // Obtener ID del usuario actual
                                val currentUserId = currentUser?.id ?: 0

                                val newEvent = Event(
                                    title = title,
                                    description = description,
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                    image = imageFile?.absolutePath ?: "",
                                    timeInfo = timeInfo,
                                    isFree = isFree,
                                    isFamily = isFamily,
                                    isMusic = isMusic,
                                    isFood = isFood,
                                    isArt = isArt,
                                    isSports = isSports,
                                    createdByUserId = currentUserId
                                )
                                
                                // Pasar el archivo de imagen al ViewModel
                                eventViewModel.insertEvent(newEvent, imageFile) { createdEvent ->
                                    // Navegar de vuelta siempre (el evento se guarda localmente aunque falle Xano)
                                    onNavigateBack()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        shape = RoundedCornerShape(14.dp),
                                enabled = title.isNotBlank() && description.isNotBlank() && selectedLocation != null && !isLoading,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 8.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = AppColors.TextOnPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            if (isLoading) "Publicando..." else "Publicar Evento",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextOnPrimary
                        )
                    }
                }
            }
        }

        if (showPermissionDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            showPermissionDialog = false
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
                                        imageVector = Icons.Default.Image,
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
                            text = "Permiso de Galería",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )

                        Spacer(Modifier.height(12.dp))

                        // Descripción
                        Text(
                            text = "Para seleccionar imágenes de tu galería, necesitamos acceso a tus fotos.",
                            fontSize = 16.sp,
                            color = AppColors.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 24.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Esto te permitirá agregar imágenes a tus eventos.",
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
                                onClick = {
                                    showPermissionDialog = false
                                    storagePermissionState.launchPermissionRequest()
                                },
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
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Permitir acceso",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextOnPrimary
                                )
                            }

                            OutlinedButton(
                                onClick = { showPermissionDialog = false },
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
                                    "Ahora no",
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

        if (showError) {
            AlertDialog(
                onDismissRequest = { showError = false },
                title = { Text("Error") },
                text = { Text("No se pudo abrir la cámara. Verifica los permisos.") },
                confirmButton = {
                    TextButton(onClick = { showError = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapLocationPicker(
    initialLocation: LatLng,
    onLocationSelected: (LatLng) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPosition by remember { mutableStateOf(initialLocation) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 15f)
    }
    val markerState = rememberMarkerState(position = initialLocation)

    LaunchedEffect(selectedPosition) {
        markerState.position = selectedPosition
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLng(selectedPosition),
            durationMs = 500
        )
    }

    Box(Modifier.fillMaxSize().background(AppColors.Background)) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Seleccionar Ubicación", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = AppColors.Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Surface)
            )

            Box(Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        scrollGesturesEnabled = true,
                        zoomGesturesEnabled = true,
                        rotationGesturesEnabled = true,
                        tiltGesturesEnabled = true
                    ),
                    onMapClick = { latLng ->
                        selectedPosition = latLng
                    }
                ) {
                    Marker(
                        state = markerState,
                        title = "Ubicación del evento",
                        draggable = true
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(AppColors.Surface)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Ubicación seleccionada",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Lat: ${String.format("%.4f", selectedPosition.latitude)}, Lng: ${String.format("%.4f", selectedPosition.longitude)}",
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onLocationSelected(selectedPosition) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(AppColors.Primary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Confirmar Ubicación", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Suppress("MissingPermission")
suspend fun getCurrentUserLocation(context: Context): Location? {
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

fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = externalCacheDir ?: cacheDir
    return File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    )
}

fun Context.getImageUri(): Uri? {
    return try {
        val file = createImageFile()
        FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            file
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun Context.saveImageToInternalStorage(uri: Uri): String? {
    return try {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_${timeStamp}.jpg"

        val imagesDir = File(filesDir, "event_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        val imageFile = File(imagesDir, fileName)
        val outputStream = FileOutputStream(imageFile)

        inputStream.copyTo(outputStream)

        inputStream.close()
        outputStream.close()

        imageFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun Context.uriToFile(uri: Uri): File? {
    return try {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_${timeStamp}.jpg"

        val imagesDir = File(cacheDir, "event_images_temp")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        val imageFile = File(imagesDir, fileName)
        val outputStream = FileOutputStream(imageFile)

        inputStream.copyTo(outputStream)

        inputStream.close()
        outputStream.close()

        imageFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun CategoryChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { onSelectedChange(!selected) },
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) AppColors.Primary else AppColors.BackgroundGray,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, AppColors.TextGray.copy(alpha = 0.2f)),
        tonalElevation = if (selected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) AppColors.TextOnPrimary else AppColors.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) AppColors.TextOnPrimary else AppColors.TextPrimary
            )
            if (selected) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AppColors.TextOnPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
