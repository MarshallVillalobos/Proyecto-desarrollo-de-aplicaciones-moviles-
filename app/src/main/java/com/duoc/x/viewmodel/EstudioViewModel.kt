package com.duoc.x.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.x.model.ArticuloItem
import com.duoc.x.model.EstudioUiState
import com.duoc.x.repository.CodigoChRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EstudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CodigoChRepository(application.applicationContext)

    private val _estado = MutableStateFlow(EstudioUiState())
    val estado: StateFlow<EstudioUiState> = _estado.asStateFlow()

    init {
        cargarDatosPersistentes()
    }

    fun cargarDatosPersistentes() {
        viewModelScope.launch {
            val estadoActualizado = repository.cargarDatosPersistentes(_estado.value)
            _estado.update { estadoActualizado }
        }
    }

    fun registrarSesionActiva(correo: String) {
        _estado.update { repository.registrarSesionActiva(it, correo) }
    }

    fun cerrarSesion() {
        repository.cerrarSesionUsuario()
    }

    // --- Acciones para NormasScreen ---
    fun onSearchQueryChange(nuevaBusqueda: String) {
        _estado.update { repository.actualizarBusqueda(it, nuevaBusqueda) }
    }

    fun onCategoriaSelected(categoria: String) {
        _estado.update { repository.seleccionarCategoria(it, categoria) }
    }

    // --- Acciones para LecturaScreen y PerfilScreen ---
    fun aumentarFuente() {
        _estado.update { repository.aumentarFuente(it) }
    }

    fun disminuirFuente() {
        _estado.update { repository.disminuirFuente(it) }
    }

    fun cambiarFuenteDirecto(nuevoTamano: Int) {
        _estado.update { repository.cambiarFuenteDirecto(it, nuevoTamano) }
    }

    fun alternarDestacado(normaTitulo: String, articuloNumero: String) {
        _estado.update { repository.alternarDestacado(it, normaTitulo, articuloNumero) }
    }

    fun prepararBorradorNota(normaTitulo: String, articuloNumero: String) {
        _estado.update { repository.prepararBorradorNota(it, normaTitulo, articuloNumero) }
    }

    fun onBorradorNotaChange(nuevoTexto: String) {
        _estado.update { repository.actualizarBorradorNota(it, nuevoTexto) }
    }

    fun validarYGuardarNota(normaTitulo: String, articuloNumero: String): Boolean {
        val (nuevoEstado, guardado) = repository.validarYGuardarNota(
            estadoActual = _estado.value,
            normaTitulo = normaTitulo,
            articuloNumero = articuloNumero
        )
        _estado.update { nuevoEstado }
        return guardado
    }

    fun eliminarNota(articuloId: String) {
        _estado.update { repository.eliminarNota(it, articuloId) }
    }

    fun obtenerArticulos(normaTitulo: String, articuloNumero: String): ArticuloItem? {
        return repository.obtenerArticulo(_estado.value, normaTitulo, articuloNumero)
    }
}