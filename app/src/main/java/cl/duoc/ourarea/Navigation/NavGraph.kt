package cl.duoc.ourarea.navigation

import LocationPermissionScreen
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.ourarea.ui.HomeScreen
import cl.duoc.ourarea.ui.LoginScreen
import cl.duoc.ourarea.ui.RegisterScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PERMISSION = "permission"
    const val HOME = "home"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.PERMISSION) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // Nueva pantalla de permisos de ubicación
        composable(Routes.PERMISSION) {
            LocationPermissionScreen(onGranted = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.PERMISSION) { inclusive = true }
                }
            })
        }

        composable(Routes.HOME) {
            HomeScreen(
                onEventDetail = { eventId ->
                    // Aquí podrías agregar navegación a detalles.
                    // navController.navigate("event_detail/$eventId")
                }
            )
        }
    }
}
