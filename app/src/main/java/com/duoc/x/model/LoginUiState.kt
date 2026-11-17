package com.duoc.x.model

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val claveVisible: Boolean = false,
    val errores: LoginErrores = LoginErrores()
)

data class LoginErrores(
    val correo: String? = null,
    val clave: String? = null
)

data class LoginRequest(
    val correo: String = "",
    val clave: String = ""
)

data class LoginResponse(
    val mensaje: String? = null,
    val token: String? = null
)