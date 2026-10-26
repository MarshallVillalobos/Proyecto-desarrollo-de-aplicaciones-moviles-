package com.duoc.x.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.duoc.x.ui.screens.*
import com.duoc.x.viewmodel.EstudioViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val estudioViewModel: EstudioViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // 1. Pantalla Login
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Normas.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Pantalla Normas
        composable(route = Screen.Normas.route) {
            NormasScreen(
                viewModel = estudioViewModel,
                onNormaClick = { normaNombre ->
                    navController.navigate(Screen.Articulos.createRoute(normaNombre))
                },
                onNavigateToMisEstudios = {
                    navController.navigate(Screen.MisEstudios.route) {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate(Screen.Perfil.route)
                }
            )
        }

        // 3. Pantalla Artículos
        composable(
            route = Screen.Articulos.route,
            arguments = listOf(navArgument("normaNombre") { type = NavType.StringType })
        ) { backStackEntry ->
            val normaNombre = backStackEntry.arguments?.getString("normaNombre") ?: "Código del Trabajo"
            ArticulosScreen(
                normaNombre = normaNombre,
                viewModel = estudioViewModel,
                onArticuloClick = { articuloNumero ->
                    navController.navigate(Screen.Lectura.createRoute(normaNombre, articuloNumero))
                },
                onBackClick = { navController.popBackStack() },
                onNavigateToNormas = {
                    navController.navigate(Screen.Normas.route) {
                        popUpTo(Screen.Normas.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToMisEstudios = {
                    navController.navigate(Screen.MisEstudios.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Perfil.route)
                }
            )
        }

        // 4. Pantalla Lectura
        composable(
            route = Screen.Lectura.route,
            arguments = listOf(
                navArgument("normaNombre") { type = NavType.StringType },
                navArgument("articuloNumero") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val normaNombre = backStackEntry.arguments?.getString("normaNombre") ?: "Código del Trabajo"
            val articuloNumero = backStackEntry.arguments?.getString("articuloNumero") ?: "Art. 1"
            LecturaScreen(
                normaNombre = normaNombre,
                articuloNumero = articuloNumero,
                viewModel = estudioViewModel,
                onAgregarNotaClick = {
                    navController.navigate(Screen.NuevaNota.createRoute(normaNombre, articuloNumero))
                },
                onBackClick = { navController.popBackStack() },
                onNavigateToNormas = {
                    navController.navigate(Screen.Normas.route) {
                        popUpTo(Screen.Normas.route) { inclusive = false }
                    }
                },
                onNavigateToMisEstudios = {
                    navController.navigate(Screen.MisEstudios.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Perfil.route)
                }
            )
        }
        // 5. Pantalla Nueva Nota
        composable(
            route = Screen.NuevaNota.route,
            arguments = listOf(
                navArgument("normaNombre") { type = NavType.StringType },
                navArgument("articuloNumero") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val normaNombre = backStackEntry.arguments?.getString("normaNombre") ?: "Código del Trabajo"
            val articuloNumero = backStackEntry.arguments?.getString("articuloNumero") ?: "Art. 1"
            NuevaNotaScreen(
                normaNombre = normaNombre,
                articuloNumero = articuloNumero,
                viewModel = estudioViewModel,
                onGuardarNotaSuccess = {
                    navController.navigate(Screen.MisEstudios.route) {
                        popUpTo(Screen.Normas.route)
                    }
                },
                onBackClick = { navController.popBackStack() },
                onNavigateToNormas = {
                    navController.navigate(Screen.Normas.route)
                },
                onNavigateToMisEstudios = {
                    navController.navigate(Screen.MisEstudios.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Perfil.route)
                }
            )
        }

        // 6. Pantalla Mis Estudios
        composable(route = Screen.MisEstudios.route) {
            MisEstudiosScreen(
                viewModel = estudioViewModel,
                onArticuloClick = { normaNombre, articuloNumero ->
                    navController.navigate(Screen.Lectura.createRoute(normaNombre, articuloNumero))
                },
                onNavigateToNormas = {
                    navController.navigate(Screen.Normas.route) {
                        popUpTo(Screen.Normas.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate(Screen.Perfil.route)
                }
            )
        }

        // 7. Pantalla Perfil
        composable(route = Screen.Perfil.route) {
            PerfilScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToNormas = {
                    navController.navigate(Screen.Normas.route)
                },
                onNavigateToMisEstudios = {
                    navController.navigate(Screen.MisEstudios.route)
                },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}