package com.duoc.x.repository

import android.content.Context
import android.util.Patterns
import androidx.core.content.edit
import com.duoc.x.data.AppDatabase
import com.duoc.x.data.ArticuloEstudioEntity
import com.duoc.x.data.UsuarioEntity
import com.duoc.x.model.ArticuloItem
import com.duoc.x.model.EstudioUiState
import com.duoc.x.model.LoginErrores
import com.duoc.x.model.LoginRequest
import com.duoc.x.model.LoginUiState
import com.duoc.x.model.NormaItem
import com.duoc.x.remote.RetrofitInstance

class CodigoChRepository(context: Context) {

    private val db = try {
        AppDatabase.getDatabase(context)
    } catch (e: Exception) {
        null
    }

    private val prefs = context.getSharedPreferences("codigo_ch_local_store", Context.MODE_PRIVATE)

    //Datos guardados con room

    fun guardarSesionUsuario(correo: String, tamanoFuenteSp: Int = 16) {
        val nombreCalculado = if (correo.contains("camila", ignoreCase = true) ||
            correo.contains("estudiante", ignoreCase = true)
        ) {
            "Camila Valdés Rojas"
        } else {
            correo.substringBefore("@")
                .replace(".", " ")
                .replaceFirstChar { it.uppercase() }
        }

        val entidad = UsuarioEntity(
            id = 1,
            nombre = nombreCalculado,
            correo = correo,
            tamanoFuenteSp = tamanoFuenteSp
        )

        try {
            db?.usuarioDao()?.guardarUsuario(entidad)
        } catch (_: Exception) {
        }

        prefs.edit {
            putString("user_nombre", entidad.nombre)
            putString("user_correo", entidad.correo)
            putInt("user_fuente", entidad.tamanoFuenteSp)
        }
    }

    fun obtenerUsuarioActual(): UsuarioEntity {
        try {
            db?.usuarioDao()?.obtenerUsuario()?.let { return it }
        } catch (_: Exception) {
        }

        val nombre = prefs.getString("user_nombre", "Camila Valdés Rojas") ?: "Camila Valdés Rojas"
        val correo = prefs.getString("user_correo", "estudiante.derecho@duocuc.cl")
            ?: "estudiante.derecho@duocuc.cl"
        val fuente = prefs.getInt("user_fuente", 16)
        return UsuarioEntity(id = 1, nombre = nombre, correo = correo, tamanoFuenteSp = fuente)
    }

    fun actualizarTamanoFuente(tamanoSp: Int) {
        val actual = obtenerUsuarioActual().copy(tamanoFuenteSp = tamanoSp)
        try {
            db?.usuarioDao()?.guardarUsuario(actual)
        } catch (_: Exception) {
        }
        prefs.edit { putInt("user_fuente", tamanoSp) }
    }

    fun cerrarSesionUsuario() {
        try {
            db?.usuarioDao()?.cerrarSesion()
        } catch (_: Exception) {
        }
        prefs.edit {
            remove("user_nombre")
            remove("user_correo")
        }
    }

    fun guardarEstadoArticulo(articulo: ArticuloItem) {
        val entidad = ArticuloEstudioEntity(
            articuloId = articulo.id,
            normaTitulo = articulo.normaTitulo,
            numero = articulo.numero,
            esDestacado = articulo.esDestacado,
            nota = articulo.nota
        )
        try {
            db?.articuloEstudioDao()?.guardarEstadoArticulo(entidad)
        } catch (_: Exception) {
        }

        prefs.edit {
            putBoolean("dest_${articulo.id}", articulo.esDestacado)
            putString("nota_${articulo.id}", articulo.nota)
        }
    }

    fun sincronizarArticulosConBaseDeDatos(articulosBase: List<ArticuloItem>): List<ArticuloItem> {
        val guardadosRoom = try {
            db?.articuloEstudioDao()?.obtenerTodos()?.associateBy { it.articuloId } ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }

        return articulosBase.map { articulo ->
            val desdeRoom = guardadosRoom[articulo.id]
            if (desdeRoom != null) {
                articulo.copy(
                    esDestacado = desdeRoom.esDestacado,
                    nota = desdeRoom.nota
                )
            } else {
                val destPref = prefs.getBoolean("dest_${articulo.id}", articulo.esDestacado)
                val notaPref =
                    prefs.getString("nota_${articulo.id}", articulo.nota) ?: articulo.nota
                articulo.copy(esDestacado = destPref, nota = notaPref)
            }
        }
    }


    // Funciones para EstudioViewModel


    fun actualizarCorreo(estadoActual: LoginUiState, nuevoCorreo: String): LoginUiState {
        return estadoActual.copy(
            correo = nuevoCorreo,
            errores = estadoActual.errores.copy(correo = null)
        )
    }

    fun actualizarClave(estadoActual: LoginUiState, nuevaClave: String): LoginUiState {
        return estadoActual.copy(
            clave = nuevaClave,
            errores = estadoActual.errores.copy(clave = null)
        )
    }

    fun alternarVisibilidadClave(estadoActual: LoginUiState): LoginUiState {
        return estadoActual.copy(
            claveVisible = !estadoActual.claveVisible
        )
    }

    fun validarFormulario(
        estadoActual: LoginUiState,
        correo: String,
        clave: String
    ): Pair<LoginUiState, Boolean> {
        val errorCorreo = when {
            correo.isBlank() -> "El correo electrónico es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() ->
                "Ingresa un correo válido (ej. estudiante.derecho@duocuc.cl)"
            else -> null
        }

        val errorClave = when {
            clave.isBlank() -> "La contraseña es obligatoria"
            clave.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }

        val nuevosErrores = LoginErrores(
            correo = errorCorreo,
            clave = errorClave
        )

        val hayErrores = listOfNotNull(nuevosErrores.correo, nuevosErrores.clave).isNotEmpty()
        val nuevoEstado = estadoActual.copy(errores = nuevosErrores)

        return Pair(nuevoEstado, !hayErrores)
    }

    // Esta funcion necesita la funcion hecha con el retrofit dentro de ApiService

    /*

    suspend fun autenticarUsuario(
        estadoActual: LoginUiState,
        correo: String,
        clave: String
    ): Pair<LoginUiState, Boolean> {
        val (estadoValidado, esValido) = validarFormulario(estadoActual, correo, clave)
        if (!esValido) {
            return Pair(estadoValidado, false)
        }

        return try {
            val respuesta = RetrofitInstance.api.login(LoginRequest(correo, clave))

            if (respuesta.isSuccessful) {
                Pair(estadoValidado, true)
            } else {
                val estadoConError = estadoValidado.copy(
                    errores = estadoValidado.errores.copy(
                        clave = "Credenciales incorrectas (Error ${respuesta.code()})"
                    )
                )
                Pair(estadoConError, false)
            }
        } catch (e: Exception) {
            val estadoConError = estadoValidado.copy(
                errores = estadoValidado.errores.copy(
                    clave = "No se pudo conectar con el servidor"
                )
            )
            Pair(estadoConError, false)
        }
    }


     */

    // Funciones de  EstudioViewModel


    fun obtenerArticulo(
        estadoActual: EstudioUiState,
        normaTitulo: String,
        articuloNumero: String
    ): ArticuloItem? {
        return estadoActual.articulos.find {
            it.normaTitulo.equals(normaTitulo, ignoreCase = true) &&
                    it.numero.equals(articuloNumero, ignoreCase = true)
        }
    }

    fun cargarDatosPersistentes(estadoActual: EstudioUiState): EstudioUiState {
        val usuario = obtenerUsuarioActual()
        val articulosSincronizados = sincronizarArticulosConBaseDeDatos(obtenerArticulosIniciales())

        return estadoActual.copy(
            normas = obtenerNormasIniciales(),
            articulos = articulosSincronizados,
            fontSizeSp = usuario.tamanoFuenteSp,
            usuarioNombre = usuario.nombre,
            usuarioCorreo = usuario.correo
        )
    }

    fun registrarSesionActiva(estadoActual: EstudioUiState, correo: String): EstudioUiState {
        val correoFinal = correo.ifBlank { "estudiante.derecho@duocuc.cl" }
        guardarSesionUsuario(correoFinal, estadoActual.fontSizeSp)
        val usuario = obtenerUsuarioActual()

        return estadoActual.copy(
            usuarioNombre = usuario.nombre,
            usuarioCorreo = usuario.correo
        )
    }

    fun actualizarBusqueda(estadoActual: EstudioUiState, nuevaBusqueda: String): EstudioUiState {
        return estadoActual.copy(searchQuery = nuevaBusqueda)
    }

    fun seleccionarCategoria(estadoActual: EstudioUiState, categoria: String): EstudioUiState {
        return estadoActual.copy(selectedCategoria = categoria)
    }

    fun aumentarFuente(estadoActual: EstudioUiState): EstudioUiState {
        return if (estadoActual.fontSizeSp < 28) {
            cambiarFuenteDirecto(estadoActual, estadoActual.fontSizeSp + 2)
        } else {
            estadoActual
        }
    }

    fun disminuirFuente(estadoActual: EstudioUiState): EstudioUiState {
        return if (estadoActual.fontSizeSp > 12) {
            cambiarFuenteDirecto(estadoActual, estadoActual.fontSizeSp - 2)
        } else {
            estadoActual
        }
    }

    fun cambiarFuenteDirecto(estadoActual: EstudioUiState, nuevoTamano: Int): EstudioUiState {
        val tamanoAjustado = nuevoTamano.coerceIn(12, 28)
        actualizarTamanoFuente(tamanoAjustado)
        return estadoActual.copy(fontSizeSp = tamanoAjustado)
    }

    fun alternarDestacado(
        estadoActual: EstudioUiState,
        normaTitulo: String,
        articuloNumero: String
    ): EstudioUiState {
        val listaActualizada = estadoActual.articulos.map { art ->
            if (art.normaTitulo.equals(normaTitulo, ignoreCase = true) &&
                art.numero.equals(articuloNumero, ignoreCase = true)
            ) {
                val actualizado = art.copy(esDestacado = !art.esDestacado)
                guardarEstadoArticulo(actualizado)
                actualizado
            } else {
                art
            }
        }
        return estadoActual.copy(articulos = listaActualizada)
    }

    fun prepararBorradorNota(
        estadoActual: EstudioUiState,
        normaTitulo: String,
        articuloNumero: String
    ): EstudioUiState {
        val notaExistente = obtenerArticulo(estadoActual, normaTitulo, articuloNumero)?.nota ?: ""
        return estadoActual.copy(borradorNota = notaExistente, errorNota = null)
    }

    fun actualizarBorradorNota(estadoActual: EstudioUiState, nuevoTexto: String): EstudioUiState {
        return estadoActual.copy(borradorNota = nuevoTexto, errorNota = null)
    }

    fun validarYGuardarNota(
        estadoActual: EstudioUiState,
        normaTitulo: String,
        articuloNumero: String
    ): Pair<EstudioUiState, Boolean> {
        val textoLimpio = estadoActual.borradorNota.trim()
        if (textoLimpio.isEmpty()) {
            val estadoError = estadoActual.copy(
                errorNota = "La nota no puede estar vacía. Escribe un apunte antes de guardar."
            )
            return Pair(estadoError, false)
        }

        val listaActualizada = estadoActual.articulos.map { art ->
            if (art.normaTitulo.equals(normaTitulo, ignoreCase = true) &&
                art.numero.equals(articuloNumero, ignoreCase = true)
            ) {
                val actualizado = art.copy(nota = textoLimpio)
                guardarEstadoArticulo(actualizado)
                actualizado
            } else {
                art
            }
        }
        return Pair(estadoActual.copy(articulos = listaActualizada, errorNota = null), true)
    }

    fun eliminarNota(estadoActual: EstudioUiState, articuloId: String): EstudioUiState {
        val listaActualizada = estadoActual.articulos.map { art ->
            if (art.id == articuloId) {
                val actualizado = art.copy(nota = "")
                guardarEstadoArticulo(actualizado)
                actualizado
            } else {
                art
            }
        }
        return estadoActual.copy(articulos = listaActualizada)
    }


    // Reemplazar listas con la db de xano usando el retrofit porque osino no tengo como sacar los datos


    private fun obtenerNormasIniciales(): List<NormaItem> {
        return listOf(
            NormaItem(
                id = "norma_1",
                titulo = "Constitución Política de la República",
                descripcion = "Bases de la institucionalidad, derechos y deberes constitucionales.",
                categoria = "Constitucional"
            ),
            NormaItem(
                id = "norma_2",
                titulo = "Código Civil",
                descripcion = "Regula las personas, los bienes, la propiedad, obligaciones y contratos.",
                categoria = "Civil"
            ),
            NormaItem(
                id = "norma_3",
                titulo = "Código Penal",
                descripcion = "Tipificación de delitos, penas y responsabilidad penal en Chile.",
                categoria = "Penal"
            )
        )
    }

    private fun obtenerArticulosIniciales(): List<ArticuloItem> {
        return listOf(
            ArticuloItem(
                id = "art_cpr_1",
                normaTitulo = "Constitución Política de la República",
                numero = "Artículo 1",
                extracto = "Las personas nacen libres e iguales en dignidad y derechos.",
                contenido = "Las personas nacen libres e iguales en dignidad y derechos. La familia es el núcleo fundamental de la sociedad. El Estado reconoce y ampara a los grupos intermedios a través de los cuales se organiza y estructura la sociedad."
            ),
            ArticuloItem(
                id = "art_cc_1",
                normaTitulo = "Código Civil",
                numero = "Artículo 1",
                extracto = "La ley es una declaración de la voluntad soberana...",
                contenido = "La ley es una declaración de la voluntad soberana que, manifestada en la forma prescrita por la Constitución, manda, prohíbe o permite."
            )
        )
    }
}