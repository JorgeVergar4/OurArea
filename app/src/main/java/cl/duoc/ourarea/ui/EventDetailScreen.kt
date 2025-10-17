package cl.duoc.ourarea.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.ourarea.ui.theme.*
import cl.duoc.ourarea.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Int,
    eventViewModel: EventViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val events by eventViewModel.filteredEvents.collectAsState()
    val event = events.find { it.id == eventId }
    var isSaved by remember { mutableStateOf(false) }
    var isAttending by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Detalles del evento",
                        color = AppColors.Surface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = AppColors.Surface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppColors.Primary
                )
            )
        },
        containerColor = AppColors.Background
    ) { paddingValues ->
        if (event == null) {
            EmptyEventState(paddingValues)
        } else {
            EventDetailContent(
                event = event,
                paddingValues = paddingValues,
                context = context,
                isSaved = isSaved,
                isAttending = isAttending,
                onSaveClick = { isSaved = !isSaved },
                onAttendClick = { isAttending = !isAttending }
            )
        }
    }
}

@Composable
private fun EmptyEventState(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = AppColors.TextSecondary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Evento no encontrado",
                fontSize = 18.sp,
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EventDetailContent(
    event: cl.duoc.ourarea.model.Event,
    paddingValues: PaddingValues,
    context: android.content.Context,
    isSaved: Boolean,
    isAttending: Boolean,
    onSaveClick: () -> Unit,
    onAttendClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        EventHeaderImage(event)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            EventTitleSection(event)
            Spacer(Modifier.height(16.dp))
            EventLocationInfo(event)
            Spacer(Modifier.height(20.dp))

            SectionDivider()

            EventDescriptionSection(event)
            Spacer(Modifier.height(20.dp))

            SectionDivider()

            EventMapSection(
                event = event,
                onDirections = {
                    launchGoogleMapsNavigation(context, event)
                },
                onOpenMap = {
                    launchGoogleMapsLocation(context, event)
                }
            )
            Spacer(Modifier.height(20.dp))

            SectionDivider()

            EventOrganizerSection(event)
            Spacer(Modifier.height(20.dp))

            SectionDivider()

            EventInfoSection(event)
            Spacer(Modifier.height(20.dp))

            SectionDivider()

            EventCommentsSection()
            Spacer(Modifier.height(28.dp))

            EventActionButtons(
                isSaved = isSaved,
                isAttending = isAttending,
                onSaveClick = onSaveClick,
                onAttendClick = onAttendClick
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 12.dp),
        thickness = 1.dp,
        color = AppColors.Divider
    )
}

// Helper functions
private fun launchGoogleMapsNavigation(context: android.content.Context, event: cl.duoc.ourarea.model.Event) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("google.navigation:q=${event.latitude},${event.longitude}")
    ).apply {
        setPackage("com.google.android.apps.maps")
    }
    context.startActivity(intent)
}

private fun launchGoogleMapsLocation(context: android.content.Context, event: cl.duoc.ourarea.model.Event) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:${event.latitude},${event.longitude}?q=${event.latitude},${event.longitude}(${event.title})")
    ).apply {
        setPackage("com.google.android.apps.maps")
    }
    context.startActivity(intent)
}
