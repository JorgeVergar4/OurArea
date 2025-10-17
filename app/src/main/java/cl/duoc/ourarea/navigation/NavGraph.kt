package cl.duoc.ourarea.navigation

import cl.duoc.ourarea.ui.LocationPermissionScreen
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.ourarea.ui.EventDetailScreen
import cl.duoc.ourarea.ui.HomeScreen
import cl.duoc.ourarea.ui.LoginScreen
import cl.duoc.ourarea.ui.RegisterScreen
import cl.duoc.ourarea.viewmodel.EventViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PERMISSION = "permission"
    const val HOME = "home"
    const val EVENT_DETAIL = "event_detail/{eventId}"
}

@Composable
fun AppNavGraph(eventViewModel: EventViewModel) {
    val navController = rememberNavController()

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
        composable(Routes.PERMISSION) {
            LocationPermissionScreen(onGranted = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.PERMISSION) { inclusive = true }
                }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                eventViewModel = eventViewModel,
                onEventDetail = { eventId ->
                    navController.navigate(Routes.EVENT_DETAIL.replace("{eventId}", eventId.toString()))
                }
            )
        }
        composable(
            route = Routes.EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")
            if (eventId != null) {
                EventDetailScreen(
                    eventId = eventId,
                    eventViewModel = eventViewModel,
                    onBack = { navController.popBackStack() }
                )
            } else {
                Text("Evento no encontrado")
            }
        }
    }
}
