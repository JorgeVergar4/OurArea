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
import androidx.core.net.toUri
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
fun EditEventScreen(
    eventId: Int,
    eventViewModel: EventViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = screenWidth > screenHeight
    
    val events by eventViewModel.filteredEvents.collectAsState()
    val existingEvent = events.find { it.id == eventId }
    val currentUser by authViewModel.currentUser.collectAsState()
    val error by eventViewModel.error.collectAsState()
    val syncStatus by eventViewModel.syncStatus.collectAsState()
    val isLoading by eventViewModel.isLoading.collectAsState()

    // Verificar permisos
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val storagePermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val storagePermissionState = rememberPermissionState(storagePermission)

    // Estados del formulario
    var title by remember { mutableStateOf(existingEvent?.title ?: "") }
    var description by remember { mutableStateOf(existingEvent?.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var currentImagePath by remember { mutableStateOf(existingEvent?.image ?: "") }
    var timeInfo by remember { mutableStateOf(existingEvent?.timeInfo ?: "Hoy") }
    var isFree by remember { mutableStateOf(existingEvent?.isFree ?: false) }
    var isFamily by remember { mutableStateOf(existingEvent?.isFamily ?: false) }
    var isMusic by remember { mutableStateOf(existingEvent?.isMusic ?: false) }
    var isFood by remember { mutableStateOf(existingEvent?.isFood ?: false) }
    var isArt by remember { mutableStateOf(existingEvent?.isArt ?: false) }
    var isSports by remember { mutableStateOf(existingEvent?.isSports ?: false) }
    var selectedLocation by remember { 
        mutableStateOf<LatLng?>(
            existingEvent?.let { LatLng(it.latitude, it.longitude) }
        ) 
    }
    var showMapPicker by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var showError by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

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

    if (existingEvent == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Evento no encontrado", color = AppColors.TextSecondary)
        }
        return
    }

    // Verificar que el usuario actual sea el creador
    val canEdit = currentUser?.id == existingEvent.createdByUserId
    if (!canEdit) {
        Box(
            Modifier.fillMaxSize().background(AppColors.Background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = AppColors.TextSecondary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No tienes permiso para editar este evento",
                    color = AppColors.TextSecondary,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onNavigateBack) {
                    Text("Volver")
                }
            }
        }
        return
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
                    title = { Text("Editar Evento", fontWeight = FontWeight.Bold) },
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

                            // Mostrar la imagen actual o nueva
                            val displayImageModel = when {
                                imageUri != null -> imageUri
                                currentImagePath.startsWith("/") -> File(currentImagePath)
                                currentImagePath.isNotEmpty() -> currentImagePath
                                else -> "https://via.placeholder.com/400x200"
                            }

                            AsyncImage(
                                model = displayImageModel,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(8.dp))

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
                                                "Click para cambiar",
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

                        // Botón de guardar cambios
                        Button(
                            onClick = {
                                selectedLocation?.let { location ->
                                    // Convertir URI a archivo temporal si se cambió la imagen
                                    val imageFile = imageUri?.let { uri ->
                                        context.uriToFile(uri)
                                    }

                                    // Determinar la ruta de imagen final
                                    val finalImagePath = imageFile?.absolutePath ?: currentImagePath

                                    val updatedEvent = existingEvent.copy(
                                        title = title,
                                        description = description,
                                        latitude = location.latitude,
                                        longitude = location.longitude,
                                        image = finalImagePath,
                                        timeInfo = timeInfo,
                                        isFree = isFree,
                                        isFamily = isFamily,
                                        isMusic = isMusic,
                                        isFood = isFood,
                                        isArt = isArt,
                                        isSports = isSports
                                    )
                                    
                                    eventViewModel.updateEvent(updatedEvent, imageFile) { result ->
                                        if (result != null) {
                                            onNavigateBack()
                                        }
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
                                if (isLoading) "Guardando..." else "Guardar Cambios",
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

                            Text(
                                text = "Permiso de Galería",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "Para seleccionar imágenes de tu galería, necesitamos acceso a tus fotos.",
                                fontSize = 16.sp,
                                color = AppColors.TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 24.sp
                            )

                            Spacer(Modifier.height(32.dp))

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
