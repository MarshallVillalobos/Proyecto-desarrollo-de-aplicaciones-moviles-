package com.duoc.x.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.duoc.x.model.ArticuloItem
import com.duoc.x.model.EstudioUiState
import com.duoc.x.repository.CodigoChRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EstudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CodigoChRepository(application.applicationContext)

    private val _estado = MutableStateFlow(EstudioUiState())
    val estado: StateFlow<EstudioUiState> = _estado.asStateFlow()

    init {
        _estado.value = repository.cargarDatosPersistentes(_estado.value)
    }

    fun registrarSesionActiva(correo: String) {
        _estado.value = repository.registrarSesionActiva(_estado.value, correo)
    }

    fun cerrarSesion() {
        repository.cerrarSesionUsuario()
    }

    // --- Acciones para NormasScreen ---
    fun onSearchQueryChange(nuevaBusqueda: String) {
        _estado.value = repository.actualizarBusqueda(_estado.value, nuevaBusqueda)
    }

    fun onCategoriaSelected(categoria: String) {
        _estado.value = repository.seleccionarCategoria(_estado.value, categoria)
    }

    // --- Acciones para LecturaScreen y PerfilScreen ---
    fun aumentarFuente() {
        _estado.value = repository.aumentarFuente(_estado.value)
    }

    fun disminuirFuente() {
        _estado.value = repository.disminuirFuente(_estado.value)
    }

    fun cambiarFuenteDirecto(nuevoTamano: Int) {
        _estado.value = repository.cambiarFuenteDirecto(_estado.value, nuevoTamano)
    }

    fun alternarDestacado(normaTitulo: String, articuloNumero: String) {
        _estado.value = repository.alternarDestacado(_estado.value, normaTitulo, articuloNumero)
    }

    // --- Acciones para NuevaNotaScreen y MisEstudiosScreen ---
    fun prepararBorradorNota(normaTitulo: String, articuloNumero: String) {
        _estado.value = repository.prepararBorradorNota(_estado.value, normaTitulo, articuloNumero)
    }

    fun onBorradorNotaChange(nuevoTexto: String) {
        _estado.value = repository.actualizarBorradorNota(_estado.value, nuevoTexto)
    }

    fun validarYGuardarNota(normaTitulo: String, articuloNumero: String): Boolean {
        val (nuevoEstado, guardado) = repository.validarYGuardarNota(
            estadoActual = _estado.value,
            normaTitulo = normaTitulo,
            articuloNumero = articuloNumero
        )
        _estado.value = nuevoEstado
        return guardado
    }

    fun eliminarNota(articuloId: String) {
        _estado.value = repository.eliminarNota(_estado.value, articuloId)
    }

    fun obtenerArticulo(normaTitulo: String, articuloNumero: String): ArticuloItem? {
        return repository.obtenerArticulo(_estado.value, normaTitulo, articuloNumero)
    }
}