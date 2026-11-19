package com.duoc.x.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.x.model.ArticuloItem
import com.duoc.x.model.DevUiState
import com.duoc.x.repository.CodigoChRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DevViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CodigoChRepository(application.applicationContext)

    private val _estado = MutableStateFlow(DevUiState(cargando = true))
    val estado: StateFlow<DevUiState> = _estado.asStateFlow()

    init {
        recargarDatos()
    }

    fun recargarDatos() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            val nuevoEstado = repository.cargarDatosDev(_estado.value)
            _estado.update { nuevoEstado }
        }
    }

    // --- 1. Borrar Usuario (DELETE) ---
    fun eliminarUsuario(usuarioId: Int) {
        viewModelScope.launch {
            val nuevoEstado = repository.eliminarUsuarioXano(_estado.value, usuarioId)
            _estado.update { nuevoEstado }
        }
    }

    // --- 2. Añadir Artículo (POST) ---
    fun onNuevoCodigoIdChange(valor: String) {
        _estado.update { repository.actualizarFormularioNuevoArticulo(it, codigoId = valor) }
    }

    fun onNuevaNormaChange(valor: String) {
        _estado.update { repository.actualizarFormularioNuevoArticulo(it, normaTitulo = valor) }
    }

    fun onNuevoNumeroChange(valor: String) {
        _estado.update { repository.actualizarFormularioNuevoArticulo(it, numero = valor) }
    }

    fun onNuevoExtractoChange(valor: String) {
        _estado.update { repository.actualizarFormularioNuevoArticulo(it, extracto = valor) }
    }

    fun onNuevoContenidoChange(valor: String) {
        _estado.update { repository.actualizarFormularioNuevoArticulo(it, contenido = valor) }
    }

    fun crearArticulo() {
        viewModelScope.launch {
            val nuevoEstado = repository.crearArticuloXano(_estado.value)
            _estado.update { nuevoEstado }
        }
    }

    // --- 3. Modificar Artículo (PATCH) ---
    fun seleccionarArticuloParaEditar(articulo: ArticuloItem) {
        _estado.update { repository.seleccionarArticuloParaEditar(it, articulo) }
    }

    fun onEditNormaChange(valor: String) {
        _estado.update { repository.actualizarFormularioEdicionArticulo(it, normaTitulo = valor) }
    }

    fun onEditNumeroChange(valor: String) {
        _estado.update { repository.actualizarFormularioEdicionArticulo(it, numero = valor) }
    }

    fun onEditExtractoChange(valor: String) {
        _estado.update { repository.actualizarFormularioEdicionArticulo(it, extracto = valor) }
    }

    fun onEditContenidoChange(valor: String) {
        _estado.update { repository.actualizarFormularioEdicionArticulo(it, contenido = valor) }
    }

    fun guardarCambiosArticulo() {
        viewModelScope.launch {
            val nuevoEstado = repository.modificarArticuloXano(_estado.value)
            _estado.update { nuevoEstado }
        }
    }
    fun consultarArticulos() {
        viewModelScope.launch {
            val nuevoEstado = repository.consultarArticulosXano(_estado.value)
            _estado.update { nuevoEstado }
        }
    }

    fun eliminarArticulo(articuloXanoId: Int) {
        viewModelScope.launch {
            val nuevoEstado = repository.eliminarArticuloXano(_estado.value, articuloXanoId)
            _estado.update { nuevoEstado }
        }
    }
}