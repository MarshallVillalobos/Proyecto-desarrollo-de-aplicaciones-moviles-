package com.duoc.x.model

data class DevUiState(
    val usuarios: List<LoginRequest> = emptyList(),
    val articulos: List<ArticuloItem> = emptyList(),
    val cargando: Boolean = false,
    val mensajeFeedback: String? = null,
    val esError: Boolean = false,

    // Campos para Añadir Artículo (POST)
    val nuevoCodigoId: String = "",
    val nuevaNormaTitulo: String = "Código Civil",
    val nuevoNumero: String = "",
    val nuevoExtracto: String = "",
    val nuevoContenido: String = "",

    // Campos para Modificar Artículo (PATCH)
    val articuloEditandoXanoId: Int? = null,
    val editNormaTitulo: String = "",
    val editNumero: String = "",
    val editExtracto: String = "",
    val editContenido: String = ""
)