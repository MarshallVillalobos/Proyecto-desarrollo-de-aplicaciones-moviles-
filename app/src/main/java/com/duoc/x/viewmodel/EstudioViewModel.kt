package com.duoc.x.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.duoc.x.model.ArticuloItem
import com.duoc.x.model.EstudioUiState
import com.duoc.x.model.NormaItem
import com.duoc.x.repository.CodigoChRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EstudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CodigoChRepository(application.applicationContext)

    private val _estado = MutableStateFlow(EstudioUiState())
    val estado: StateFlow<EstudioUiState> = _estado.asStateFlow()

    init {
        cargarDatosPersistentes()
    }

    private fun cargarDatosPersistentes() {
        val usuario = repository.obtenerUsuarioActual()
        val articulosSincronizados = repository.sincronizarArticulosConBaseDeDatos(obtenerArticulosIniciales())

        _estado.update {
            it.copy(
                normas = obtenerNormasIniciales(),
                articulos = articulosSincronizados,
                fontSizeSp = usuario.tamanoFuenteSp,
                usuarioNombre = usuario.nombre,
                usuarioCorreo = usuario.correo
            )
        }
    }

    // Guarda en Room el usuario que acaba de iniciar sesión en el Login
    fun registrarSesionActiva(correo: String) {
        val correoFinal = correo.ifBlank { "estudiante.derecho@duocuc.cl" }
        repository.guardarSesionUsuario(correoFinal, _estado.value.fontSizeSp)
        val usuario = repository.obtenerUsuarioActual()
        _estado.update {
            it.copy(
                usuarioNombre = usuario.nombre,
                usuarioCorreo = usuario.correo
            )
        }
    }

    fun cerrarSesion() {
        repository.cerrarSesionUsuario()
    }

    // --- Acciones para NormasScreen ---
    fun onSearchQueryChange(nuevaBusqueda: String) {
        _estado.update { it.copy(searchQuery = nuevaBusqueda) }
    }

    fun onCategoriaSelected(categoria: String) {
        _estado.update { it.copy(selectedCategoria = categoria) }
    }

    // --- Acciones para LecturaScreen y PerfilScreen (Persisten en Room) ---
    fun aumentarFuente() {
        val actual = _estado.value.fontSizeSp
        if (actual < 28) {
            cambiarFuenteDirecto(actual + 2)
        }
    }

    fun disminuirFuente() {
        val actual = _estado.value.fontSizeSp
        if (actual > 12) {
            cambiarFuenteDirecto(actual - 2)
        }
    }

    fun cambiarFuenteDirecto(nuevoTamano: Int) {
        val tamanoAjustado = nuevoTamano.coerceIn(12, 28)
        repository.actualizarTamanoFuente(tamanoAjustado)
        _estado.update { it.copy(fontSizeSp = tamanoAjustado) }
    }

    fun alternarDestacado(normaTitulo: String, articuloNumero: String) {
        _estado.update { estadoActual ->
            val listaActualizada = estadoActual.articulos.map { art ->
                if (art.normaTitulo.equals(normaTitulo, ignoreCase = true) &&
                    art.numero.equals(articuloNumero, ignoreCase = true)
                ) {
                    val actualizado = art.copy(esDestacado = !art.esDestacado)
                    repository.guardarEstadoArticulo(actualizado)
                    actualizado
                } else {
                    art
                }
            }
            estadoActual.copy(articulos = listaActualizada)
        }
    }

    // --- Acciones para NuevaNotaScreen y MisEstudiosScreen (Persisten en Room) ---
    fun prepararBorradorNota(normaTitulo: String, articuloNumero: String) {
        val notaExistente = obtenerArticulo(normaTitulo, articuloNumero)?.nota ?: ""
        _estado.update { it.copy(borradorNota = notaExistente, errorNota = null) }
    }

    fun onBorradorNotaChange(nuevoTexto: String) {
        _estado.update { it.copy(borradorNota = nuevoTexto, errorNota = null) }
    }

    fun validarYGuardarNota(normaTitulo: String, articuloNumero: String): Boolean {
        val textoLimpio = _estado.value.borradorNota.trim()
        if (textoLimpio.isEmpty()) {
            _estado.update {
                it.copy(errorNota = "La nota no puede estar vacía. Escribe un apunte antes de guardar.")
            }
            return false
        }

        _estado.update { estadoActual ->
            val listaActualizada = estadoActual.articulos.map { art ->
                if (art.normaTitulo.equals(normaTitulo, ignoreCase = true) &&
                    art.numero.equals(articuloNumero, ignoreCase = true)
                ) {
                    val actualizado = art.copy(nota = textoLimpio)
                    repository.guardarEstadoArticulo(actualizado)
                    actualizado
                } else {
                    art
                }
            }
            estadoActual.copy(articulos = listaActualizada, errorNota = null)
        }
        return true
    }

    fun eliminarNota(articuloId: String) {
        _estado.update { estadoActual ->
            val listaActualizada = estadoActual.articulos.map { art ->
                if (art.id == articuloId) {
                    val actualizado = art.copy(nota = "")
                    repository.guardarEstadoArticulo(actualizado)
                    actualizado
                } else {
                    art
                }
            }
            estadoActual.copy(articulos = listaActualizada)
        }
    }

    fun obtenerArticulo(normaTitulo: String, articuloNumero: String): ArticuloItem? {
        return _estado.value.articulos.find {
            it.normaTitulo.equals(normaTitulo, ignoreCase = true) &&
                    it.numero.equals(articuloNumero, ignoreCase = true)
        }
    }

    private fun obtenerNormasIniciales(): List<NormaItem> = listOf(

    )

    private fun obtenerArticulosIniciales(): List<ArticuloItem> = listOf(
    )
}