package com.duoc.x.repository

import android.content.Context
import android.util.Patterns
import androidx.core.content.edit
import com.duoc.x.data.AppDatabase
import com.duoc.x.data.ArticuloEstudioEntity
import com.duoc.x.data.UsuarioEntity
import com.duoc.x.model.ArticuloItem
import com.duoc.x.model.DevUiState
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


    // 1. LOGIN Y AUTENTICACIÓN

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

    suspend fun validarFormulario(
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
        return Pair(estadoActual.copy(errores = nuevosErrores), !hayErrores)
    }

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
            val respuesta = RetrofitInstance.api.obtenerUsuarios()

            if (respuesta.isSuccessful) {
                val listaUsuarios: List<LoginRequest> = respuesta.body().orEmpty()
                val usuarioEncontrado = listaUsuarios.any { usuario ->
                    usuario.correo.equals(correo.trim(), ignoreCase = true) &&
                            usuario.clave == clave
                }

                if (usuarioEncontrado) {
                    Pair(estadoValidado, true)
                } else {
                    val estadoConError = estadoValidado.copy(
                        errores = estadoValidado.errores.copy(
                            clave = "Correo o contraseña incorrectos"
                        )
                    )
                    Pair(estadoConError, false)
                }
            } else {
                val estadoConError = estadoValidado.copy(
                    errores = estadoValidado.errores.copy(
                        clave = "Error del servidor (${respuesta.code()})"
                    )
                )
                Pair(estadoConError, false)
            }
        } catch (e: Exception) {
            val estadoConError = estadoValidado.copy(
                errores = estadoValidado.errores.copy(
                    clave = "No se pudo conectar con Xano"
                )
            )
            Pair(estadoConError, false)
        }
    }

    suspend fun registrarUsuario(
        estadoActual: LoginUiState,
        correo: String,
        clave: String
    ): Pair<LoginUiState, Boolean> {
        val (estadoValidado, esValido) = validarFormulario(estadoActual, correo, clave)
        if (!esValido) {
            return Pair(estadoValidado, false)
        }

        return try {
            val respExistentes = RetrofitInstance.api.obtenerUsuarios()
            if (respExistentes.isSuccessful) {
                val correoYaExiste = respExistentes.body().orEmpty().any { usuario ->
                    usuario.correo.equals(correo.trim(), ignoreCase = true)
                }
                if (correoYaExiste) {
                    val estadoDuplicado = estadoValidado.copy(
                        errores = estadoValidado.errores.copy(
                            correo = "Este correo ya se encuentra registrado"
                        )
                    )
                    return Pair(estadoDuplicado, false)
                }
            }

            val nuevoUsuario = LoginRequest(
                correo = correo.trim(),
                clave = clave
            )
            val respuesta = RetrofitInstance.api.registrarUsuario(nuevoUsuario)

            if (respuesta.isSuccessful) {
                Pair(estadoValidado, true)
            } else {
                val estadoConError = estadoValidado.copy(
                    errores = estadoValidado.errores.copy(
                        clave = "No se pudo crear la cuenta (Error ${respuesta.code()})"
                    )
                )
                Pair(estadoConError, false)
            }
        } catch (e: Exception) {
            val estadoConError = estadoValidado.copy(
                errores = estadoValidado.errores.copy(
                    clave = "No se pudo conectar con Xano"
                )
            )
            Pair(estadoConError, false)
        }
    }

    // =========================================================================
    // 2. GESTIÓN DE ESTUDIO, LECTURA Y NOTAS (EstudioViewModel)
    // =========================================================================

    suspend fun cargarDatosPersistentes(estadoActual: EstudioUiState): EstudioUiState {
        val usuario = obtenerUsuarioActual()
        val normasDesdeXano = obtenerNormas()
        val articulosDesdeXano = obtenerArticulos()
        val articulosSincronizados = sincronizarArticulosConBaseDeDatos(articulosDesdeXano)

        return estadoActual.copy(
            normas = normasDesdeXano,
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

    // =========================================================================
    // 3. PERSISTENCIA LOCAL (Room Database + SharedPreferences)
    // =========================================================================

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

    // =========================================================================
    // 4. CATÁLOGO DE DATOS
    // =========================================================================

    private suspend fun obtenerNormas(): List<NormaItem> {
        return try {
            val respuesta = RetrofitInstance.api.obtenerNormas()
            if (respuesta.isSuccessful) {
                respuesta.body().orEmpty()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun obtenerArticulos(): List<ArticuloItem> {
        return try {
            val respuesta = RetrofitInstance.api.obtenerArticulos()
            if (respuesta.isSuccessful) {
                respuesta.body().orEmpty()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

// =========================================================================
// 5. DevScreen
// =========================================================================

    suspend fun cargarDatosDev(estadoActual: DevUiState): DevUiState {
        return try {
            val respUsuarios = RetrofitInstance.api.obtenerUsuarios()
            val respArticulos = RetrofitInstance.api.obtenerArticulos()

            estadoActual.copy(
                usuarios = if (respUsuarios.isSuccessful) respUsuarios.body().orEmpty() else estadoActual.usuarios,
                articulos = if (respArticulos.isSuccessful) respArticulos.body().orEmpty() else estadoActual.articulos,
                cargando = false
            )
        } catch (e: Exception) {
            estadoActual.copy(
                cargando = false,
                mensajeFeedback = "Error al conectar con Xano: ${e.localizedMessage}",
                esError = true
            )
        }
    }

    suspend fun eliminarUsuarioXano(estadoActual: DevUiState, usuarioId: Int): DevUiState {
        return try {
            val respuesta = RetrofitInstance.api.eliminarUsuario(usuarioId)
            if (respuesta.isSuccessful) {
                val listaActualizada = estadoActual.usuarios.filterNot { it.id == usuarioId }
                estadoActual.copy(
                    usuarios = listaActualizada,
                    mensajeFeedback = "Usuario #$usuarioId eliminado de Xano",
                    esError = false
                )
            } else {
                estadoActual.copy(
                    mensajeFeedback = "Error al eliminar usuario (${respuesta.code()})",
                    esError = true
                )
            }
        } catch (e: Exception) {
            estadoActual.copy(
                mensajeFeedback = "Error de red: ${e.localizedMessage}",
                esError = true
            )
        }
    }

    fun actualizarFormularioNuevoArticulo(
        estadoActual: DevUiState,
        codigoId: String = estadoActual.nuevoCodigoId,
        normaTitulo: String = estadoActual.nuevaNormaTitulo,
        numero: String = estadoActual.nuevoNumero,
        extracto: String = estadoActual.nuevoExtracto,
        contenido: String = estadoActual.nuevoContenido
    ): DevUiState {
        return estadoActual.copy(
            nuevoCodigoId = codigoId,
            nuevaNormaTitulo = normaTitulo,
            nuevoNumero = numero,
            nuevoExtracto = extracto,
            nuevoContenido = contenido,
            mensajeFeedback = null
        )
    }

    suspend fun crearArticuloXano(estadoActual: DevUiState): DevUiState {
        if (estadoActual.nuevoNumero.isBlank() || estadoActual.nuevoContenido.isBlank()) {
            return estadoActual.copy(
                mensajeFeedback = "Completa al menos el número y contenido del artículo",
                esError = true
            )
        }

        val nuevoArticulo = ArticuloItem(
            id = estadoActual.nuevoCodigoId.ifBlank { "art_${System.currentTimeMillis()}" },
            normaTitulo = estadoActual.nuevaNormaTitulo.trim(),
            numero = estadoActual.nuevoNumero.trim(),
            extracto = estadoActual.nuevoExtracto.ifBlank { estadoActual.nuevoContenido.take(90) }.trim(),
            contenido = estadoActual.nuevoContenido.trim(),
            esDestacado = false,
            nota = ""
        )

        return try {
            val respuesta = RetrofitInstance.api.crearArticulo(nuevoArticulo)
            if (respuesta.isSuccessful) {
                val creado = respuesta.body()
                val nuevaLista = if (creado != null) estadoActual.articulos + creado else estadoActual.articulos
                estadoActual.copy(
                    articulos = nuevaLista,
                    nuevoCodigoId = "",
                    nuevoNumero = "",
                    nuevoExtracto = "",
                    nuevoContenido = "",
                    mensajeFeedback = "Artículo ${nuevoArticulo.numero} creado en Xano",
                    esError = false
                )
            } else {
                estadoActual.copy(
                    mensajeFeedback = "Error al crear artículo (${respuesta.code()})",
                    esError = true
                )
            }
        } catch (e: Exception) {
            estadoActual.copy(
                mensajeFeedback = "Error de red: ${e.localizedMessage}",
                esError = true
            )
        }
    }

    fun seleccionarArticuloParaEditar(estadoActual: DevUiState, articulo: ArticuloItem): DevUiState {
        return estadoActual.copy(
            articuloEditandoXanoId = articulo.xanoId,
            editNormaTitulo = articulo.normaTitulo,
            editNumero = articulo.numero,
            editExtracto = articulo.extracto,
            editContenido = articulo.contenido,
            mensajeFeedback = null
        )
    }

    fun actualizarFormularioEdicionArticulo(
        estadoActual: DevUiState,
        normaTitulo: String = estadoActual.editNormaTitulo,
        numero: String = estadoActual.editNumero,
        extracto: String = estadoActual.editExtracto,
        contenido: String = estadoActual.editContenido
    ): DevUiState {
        return estadoActual.copy(
            editNormaTitulo = normaTitulo,
            editNumero = numero,
            editExtracto = extracto,
            editContenido = contenido,
            mensajeFeedback = null
        )
    }

    suspend fun modificarArticuloXano(estadoActual: DevUiState): DevUiState {
        val xanoId = estadoActual.articuloEditandoXanoId ?: return estadoActual.copy(
            mensajeFeedback = "Selecciona un artículo de la lista para modificarlo",
            esError = true
        )

        val original = estadoActual.articulos.find { it.xanoId == xanoId } ?: return estadoActual

        val actualizado = original.copy(
            normaTitulo = estadoActual.editNormaTitulo.trim(),
            numero = estadoActual.editNumero.trim(),
            extracto = estadoActual.editExtracto.trim(),
            contenido = estadoActual.editContenido.trim()
        )

        return try {
            val respuesta = RetrofitInstance.api.actualizarArticulo(xanoId, actualizado)
            if (respuesta.isSuccessful) {
                val itemServidor = respuesta.body() ?: actualizado
                val listaActualizada = estadoActual.articulos.map {
                    if (it.xanoId == xanoId) itemServidor else it
                }
                estadoActual.copy(
                    articulos = listaActualizada,
                    articuloEditandoXanoId = null,
                    editNormaTitulo = "",
                    editNumero = "",
                    editExtracto = "",
                    editContenido = "",
                    mensajeFeedback = "Artículo ${itemServidor.numero} actualizado en Xano",
                    esError = false
                )
            } else {
                estadoActual.copy(
                    mensajeFeedback = "Error al modificar artículo (${respuesta.code()})",
                    esError = true
                )
            }
        } catch (e: Exception) {
            estadoActual.copy(
                mensajeFeedback = "Error de red: ${e.localizedMessage}",
                esError = true
            )
        }
    }

    suspend fun consultarArticulosXano(estadoActual: DevUiState): DevUiState {
        return try {
            val respuesta = RetrofitInstance.api.obtenerArticulos()
            if (respuesta.isSuccessful) {
                val lista = respuesta.body().orEmpty()
                estadoActual.copy(
                    articulos = lista,
                    mensajeFeedback = "GET articulo: ${lista.size} registros cargados desde Xano",
                    esError = false
                )
            } else {
                estadoActual.copy(
                    mensajeFeedback = "Error al consultar artículos (${respuesta.code()})",
                    esError = true
                )
            }
        } catch (e: Exception) {
            estadoActual.copy(
                mensajeFeedback = "Error de red: ${e.localizedMessage}",
                esError = true
            )
        }
    }

    suspend fun eliminarArticuloXano(estadoActual: DevUiState, articuloXanoId: Int): DevUiState {
        return try {
            val respuesta = RetrofitInstance.api.eliminarArticulo(articuloXanoId)
            if (respuesta.isSuccessful) {
                val listaActualizada = estadoActual.articulos.filterNot { it.xanoId == articuloXanoId }
                estadoActual.copy(
                    articulos = listaActualizada,
                    mensajeFeedback = "Artículo #$articuloXanoId eliminado de Xano",
                    esError = false
                )
            } else {
                estadoActual.copy(
                    mensajeFeedback = "Error al eliminar artículo (${respuesta.code()})",
                    esError = true
                )
            }
        } catch (e: Exception) {
            estadoActual.copy(
                mensajeFeedback = "Error de red: ${e.localizedMessage}",
                esError = true
            )
        }
    }

}