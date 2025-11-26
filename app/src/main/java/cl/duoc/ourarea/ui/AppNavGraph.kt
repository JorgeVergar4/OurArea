package cl.duoc.ourarea.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.ourarea.viewmodel.AuthViewModel
import cl.duoc.ourarea.viewmodel.EventViewModel

@Composable
fun AppNavGraph(
    eventViewModel: EventViewModel,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isCheckingSession by authViewModel.isCheckingSession.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen()

            // Navegar cuando termine de verificar la sesión
            LaunchedEffect(isCheckingSession) {
                if (!isCheckingSession) {
                    val destination = if (isLoggedIn) "home" else "login"
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
        }
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                eventViewModel = eventViewModel,
                authViewModel = authViewModel,
                onEventDetail = { eventId -> navController.navigate("detail/$eventId") },
                onAddEvent = { navController.navigate("add") },
                onLogout = {
                    // Navegar inmediatamente, logout se ejecuta en segundo plano
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                    authViewModel.logout()
                }
            )
        }

        composable("add") {
            AddEventScreen(
                eventViewModel = eventViewModel,
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0
            EditEventScreen(
                eventId = eventId,
                eventViewModel = eventViewModel,
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "detail/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0
            EventDetailScreen(
                eventId = eventId,
                eventViewModel = eventViewModel,
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate("edit/$id") }
            )
        }
    }
}
