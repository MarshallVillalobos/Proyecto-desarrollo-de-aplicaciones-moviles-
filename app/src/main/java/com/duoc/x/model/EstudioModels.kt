package com.duoc.x.model

import com.google.gson.annotations.SerializedName

// Modelo para cada ley o código en la pantalla de Normas
data class NormaItem(
    @SerializedName("codigo_id")
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = ""
)

data class ArticuloItem(
    @SerializedName("codigo_id")
    val id: String = "",
    val normaTitulo: String = "",
    val numero: String = "",
    val extracto: String = "",
    val contenido: String = "",
    val esDestacado: Boolean = false,
    val nota: String = ""
) {
    val tieneNota: Boolean
        get() = nota.isNotBlank()
}

data class EstudioUiState(
    val searchQuery: String = "",
    val selectedCategoria: String = "Todas",
    val fontSizeSp: Int = 16,
    val normas: List<NormaItem> = emptyList(),
    val articulos: List<ArticuloItem> = emptyList(),
    val borradorNota: String = "",
    val errorNota: String? = null,
    val usuarioNombre: String = "Camila Valdés Rojas",
    val usuarioCorreo: String = "estudiante.derecho@duocuc.cl"
)