import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.ourarea.R
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionScreen(onGranted: () -> Unit) {
    val permissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    if (permissionState.status.isGranted) {
        onGranted()
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4FBF8)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    // Logo OurArea
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo OurArea",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Permiso de ubicación requerido",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF19B6B6)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Para mostrar eventos cerca de ti, necesitamos acceder a tu ubicación.",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { permissionState.launchPermissionRequest() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF19B6B6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Activar ubicación", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
