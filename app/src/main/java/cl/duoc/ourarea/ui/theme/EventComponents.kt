package cl.duoc.ourarea.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import cl.duoc.ourarea.model.Event
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun EventHeaderImage(event: Event) {
    AsyncImage(
        model = event.image,
        contentDescription = event.title,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentScale = ContentScale.Crop,
        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
        error = painterResource(id = android.R.drawable.ic_menu_report_image)
    )
}

@Composable
fun EventTitleSection(event: Event) {
    Column {
        Text(
            text = event.title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            lineHeight = 32.sp
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EventChip(event.timeInfo, AppColors.Primary)
            if (event.isFree) EventChip("Gratis", AppColors.Success)
            if (event.isFood) EventChip("Comida", AppColors.Warning)
            if (event.isFamily) EventChip("Familiar", AppColors.Info)
            if (event.isMusic) EventChip("Música", AppColors.PrimaryVariant)
            if (event.isArt) EventChip("Arte", AppColors.Secondary)
            if (event.isSports) EventChip("Deportes", AppColors.MediumTeal)
        }
    }
}

@Composable
fun EventLocationInfo(event: Event) {
    val address = getAddressFromEvent(event.title)
    val schedule = getScheduleFromEvent(event.title)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.LightTeal),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${String.format("%.1f", event.distance / 1000)} km • $address",
                    fontSize = 14.sp,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = schedule,
                    fontSize = 14.sp,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EventDescriptionSection(event: Event) {
    Column {
        Text(
            text = "Descripción",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = event.description,
            fontSize = 15.sp,
            color = AppColors.TextSecondary,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun EventMapSection(
    event: Event,
    onDirections: () -> Unit,
    onOpenMap: () -> Unit
) {
    Column {
        Text(
            text = "Ubicación",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(12.dp))

        val eventLocation = LatLng(event.latitude, event.longitude)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(eventLocation, 15f)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                )
            ) {
                Marker(
                    state = rememberMarkerState(position = eventLocation),
                    title = event.title
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDirections,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.Primary
                )
            ) {
                Icon(
                    Icons.Default.Directions,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Cómo llegar", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            OutlinedButton(
                onClick = onOpenMap,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.Primary
                )
            ) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Abrir mapa", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun EventOrganizerSection(event: Event) {
    Column {
        Text(
            text = "Organiza",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AppColors.LightTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = AppColors.Primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = getOrganizerFromEvent(event.title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = "Responde en ~1 h",
                            fontSize = 13.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }
                FilledTonalButton(
                    onClick = { /* TODO */ },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AppColors.LightTeal,
                        contentColor = AppColors.Primary
                    )
                ) {
                    Text("Seguir", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun EventInfoSection(event: Event) {
    Column {
        Text(
            text = "Información útil",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (event.isFamily) InfoChip("Apto familias")
            InfoChip("Pet-friendly")
            InfoChip("Accesible")
        }
        Spacer(Modifier.height(8.dp))
        InfoChip("Est. disponible")
    }
}

@Composable
fun EventCommentsSection() {
    Column {
        Text(
            text = "Comentarios",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(12.dp))
        CommentItem("María", 4.5f, "Ambiente genial y comida deliciosa. ¡Muy recomendado!")
        Spacer(Modifier.height(14.dp))
        CommentItem("Jorge", 4.0f, "Buena música y variedad, aunque algo concurrido.")
    }
}

@Composable
fun EventActionButtons(
    isSaved: Boolean,
    isAttending: Boolean,
    onSaveClick: () -> Unit,
    onAttendClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onSaveClick,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isSaved) AppColors.LightTeal else AppColors.Transparent,
                contentColor = AppColors.Primary
            )
        ) {
            Icon(
                if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (isSaved) "Guardado" else "Guardar",
                fontWeight = FontWeight.SemiBold
            )
        }
        Button(
            onClick = onAttendClick,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isAttending) AppColors.PrimaryDark else AppColors.Primary
            )
        ) {
            Icon(
                if (isAttending) Icons.Default.CheckCircle else Icons.Default.AddCircleOutline,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (isAttending) "Confirmado" else "Asistir",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun EventChip(text: String, backgroundColor: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            color = AppColors.Surface,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun InfoChip(text: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = AppColors.LightTeal
    ) {
        Text(
            text = text,
            color = AppColors.PrimaryDark,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CommentItem(name: String, rating: Float, comment: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AppColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.first().toString(),
                    color = AppColors.Surface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AppColors.TextPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = AppColors.Warning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = rating.toString(),
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = comment,
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
