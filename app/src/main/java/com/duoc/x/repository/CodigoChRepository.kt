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

    // =========================================================================
    // 2. GESTIÓN DE ESTUDIO, LECTURA Y NOTAS (EstudioViewModel)
    // =========================================================================

    fun cargarDatosPersistentes(estadoActual: EstudioUiState): EstudioUiState {
        val usuario = obtenerUsuarioActual()
        val articulosSincronizados = sincronizarArticulosConBaseDeDatos(obtenerArticulos())

        return estadoActual.copy(
            normas = obtenerNormas(),
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
    // 4. CATÁLOGO DE DATOS (Reemplazar por Retrofit / Xano al crear endpoints)
    // =========================================================================

    private fun obtenerNormas(): List<NormaItem> {
        return listOf(
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
    }

    private fun obtenerArticulos(): List<ArticuloItem> {
        return listOf(
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
}