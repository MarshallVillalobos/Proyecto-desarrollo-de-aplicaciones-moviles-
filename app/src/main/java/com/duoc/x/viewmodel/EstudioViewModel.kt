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
        NormaItem(
            id = "c_civil",
            titulo = "Código Civil",
            descripcion = "Materia: Derecho Civil, Contratos y Propiedad",
            categoria = "Civil"
        ),
        NormaItem(
            id = "c_trabajo",
            titulo = "Código del Trabajo",
            descripcion = "Materia: Derecho Laboral, Protección y Contratos",
            categoria = "Laboral"
        ),
        NormaItem(
            id = "c_constitucion",
            titulo = "Constitución Política",
            descripcion = "Materia: Derecho Político, Bases de la Institucionalidad",
            categoria = "Constitución"
        )
    )

    private fun obtenerArticulosIniciales(): List<ArticuloItem> = listOf(
        ArticuloItem(
            id = "ct_art1",
            normaTitulo = "Código del Trabajo",
            numero = "Art. 1",
            extracto = "Las relaciones laborales entre empleadores y trabajadores se regularán por este Código y por sus leyes complementarias.",
            contenido = "Las relaciones laborales entre empleadores y trabajadores se regularán por este Código y por sus leyes complementarias.\n\nEstas normas no se aplicarán, sin embargo, a los funcionarios de la Administración del Estado, centralizada y descentralizada, del Congreso Nacional y del Poder Judicial, ni a los del Ministerio Público.",
            esDestacado = false,
            nota = "Regla general: aplica a toda relación laboral."
        ),
        ArticuloItem(
            id = "ct_art2",
            normaTitulo = "Código del Trabajo",
            numero = "Art. 2",
            extracto = "Las funciones o servicios prestados en una empresa deben considerar la igual dignidad de los trabajadores y la protección social.",
            contenido = "Las funciones o servicios prestados en una empresa deben considerar la igual dignidad de los trabajadores y la protección social.\n\nSon contrarios a los principios de las leyes laborales los actos de discriminación. Se consideran actos de discriminación las distinciones, exclusiones o preferencias basadas en motivos de raza, color, sexo, edad, estado civil, sindicación o religión.",
            esDestacado = false,
            nota = ""
        ),
        ArticuloItem(
            id = "ct_art3",
            normaTitulo = "Código del Trabajo",
            numero = "Art. 3",
            extracto = "Para todos los efectos legales se entiende por empleador la persona natural o jurídica que utiliza los servicios de uno o más trabajadores...",
            contenido = "Para todos los efectos legales se entiende por empleador la persona natural o jurídica que utiliza los servicios de uno o más trabajadores en virtud de un contrato de trabajo.\n\nTrabajador es toda persona natural que presta servicios personales intelectuales o materiales bajo dependencia o subordinación, en virtud de un contrato de trabajo.",
            esDestacado = true,
            nota = "Distinguir bien empleador de empresa. Repasar antes de la prueba."
        ),
        ArticuloItem(
            id = "ct_art4",
            normaTitulo = "Código del Trabajo",
            numero = "Art. 4",
            extracto = "Se presume de derecho que representa al empleador y que en tal carácter obliga a este con los trabajadores el gerente, el director...",
            contenido = "Se presume de derecho que representa al empleador y que en tal carácter obliga a este con los trabajadores el gerente, el administrador, el capitán de barco y, en general, la persona que ejerce habitualmente funciones de dirección o administración por cuenta o representación de una persona natural o jurídica.",
            esDestacado = false,
            nota = ""
        ),
        ArticuloItem(
            id = "cc_art1",
            normaTitulo = "Código Civil",
            numero = "Art. 1",
            extracto = "La ley es una declaración de la voluntad soberana que, manifestada en la forma prescrita por la Constitución, manda, prohíbe o permite.",
            contenido = "La ley es una declaración de la voluntad soberana que, manifestada en la forma prescrita por la Constitución, manda, prohíbe o permite.",
            esDestacado = false,
            nota = ""
        ),
        ArticuloItem(
            id = "cc_art2",
            normaTitulo = "Código Civil",
            numero = "Art. 2",
            extracto = "La costumbre no constituye derecho sino en los casos en que la ley se remite a ella.",
            contenido = "La costumbre no constituye derecho sino en los casos en que la ley se remite a ella.",
            esDestacado = true,
            nota = "Revisar diferencia con costumbre mercantil."
        ),
        ArticuloItem(
            id = "cc_art1545",
            normaTitulo = "Código Civil",
            numero = "Art. 1545",
            extracto = "Todo contrato legalmente celebrado es una ley para los contratantes, y no puede ser invalidado sino por su consentimiento mutuo...",
            contenido = "Todo contrato legalmente celebrado es una ley para los contratantes, y no puede ser invalidado sino por su consentimiento mutuo o por causas legales.",
            esDestacado = false,
            nota = "Ejemplo para el ensayo de contratos."
        ),
        ArticuloItem(
            id = "cp_art1",
            normaTitulo = "Constitución Política",
            numero = "Art. 1",
            extracto = "Las personas nacen libres e iguales en dignidad y derechos. La familia es el núcleo fundamental de la sociedad.",
            contenido = "Las personas nacen libres e iguales en dignidad y derechos.\n\nLa familia es el núcleo fundamental de la sociedad.\n\nEl Estado está al servicio de la persona humana y su finalidad es promover el bien común.",
            esDestacado = true,
            nota = "Base de la institucionalidad."
        ),
        ArticuloItem(
            id = "cp_art2",
            normaTitulo = "Constitución Política",
            numero = "Art. 2",
            extracto = "Es deber del Estado resguardar la seguridad nacional, dar protección a la población y a la familia.",
            contenido = "Son emblemas nacionales la bandera nacional, el escudo de armas de la República y el himno nacional.",
            esDestacado = false,
            nota = ""
        )
    )
}