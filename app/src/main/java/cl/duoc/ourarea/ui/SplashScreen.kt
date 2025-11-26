package cl.duoc.ourarea.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cl.duoc.ourarea.R
import cl.duoc.ourarea.ui.theme.AppColors

@Composable
fun SplashScreen() {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = screenWidth > screenHeight
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo OurArea
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo OurArea",
                modifier = Modifier.size(if (isLandscape) 90.dp else 120.dp)
            )

            Spacer(modifier = Modifier.height(if (isLandscape) 20.dp else 32.dp))

            // Indicador de carga
            CircularProgressIndicator(
                color = AppColors.Primary,
                modifier = Modifier.size(if (isLandscape) 32.dp else 40.dp)
            )
        }
    }
}
