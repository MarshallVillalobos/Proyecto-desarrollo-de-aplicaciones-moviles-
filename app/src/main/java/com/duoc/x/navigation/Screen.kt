package com.duoc.x.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Normas : Screen("normas")
    data object MisEstudios : Screen("mis_estudios")
    data object Perfil : Screen("perfil")

    // Rutas que reciben parámetros (ID de la norma o ID del artículo)
    data object Articulos : Screen("articulos/{normaNombre}") {
        fun createRoute(normaNombre: String) = "articulos/$normaNombre"
    }

    data object Lectura : Screen("lectura/{normaNombre}/{articuloNumero}") {
        fun createRoute(normaNombre: String, articuloNumero: String) =
            "lectura/$normaNombre/$articuloNumero"
    }

    data object NuevaNota : Screen("nueva_nota/{normaNombre}/{articuloNumero}") {
        fun createRoute(normaNombre: String, articuloNumero: String) =
            "nueva_nota/$normaNombre/$articuloNumero"
    }
}