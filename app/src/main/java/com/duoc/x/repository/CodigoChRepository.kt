package com.duoc.x.repository

import android.content.Context
import com.duoc.x.data.AppDatabase
import com.duoc.x.data.ArticuloEstudioEntity
import com.duoc.x.data.UsuarioEntity
import com.duoc.x.model.ArticuloItem

class CodigoChRepository(context: Context) {

    private val db = try {
        AppDatabase.getDatabase(context)
    } catch (e: Exception) {
        null
    }

    private val prefs = context.getSharedPreferences("codigo_ch_local_store", Context.MODE_PRIVATE)

    // Guarda el usuario al iniciar sesión en el Login
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
        } catch (_: Exception) { }

        prefs.edit()
            .putString("user_nombre", entidad.nombre)
            .putString("user_correo", entidad.correo)
            .putInt("user_fuente", entidad.tamanoFuenteSp)
            .apply()
    }

    // Obtiene los datos del usuario logueado para mostrarlos en PerfilScreen
    fun obtenerUsuarioActual(): UsuarioEntity {
        try {
            db?.usuarioDao()?.obtenerUsuario()?.let { return it }
        } catch (_: Exception) { }

        val nombre = prefs.getString("user_nombre", "Camila Valdés Rojas") ?: "Camila Valdés Rojas"
        val correo = prefs.getString("user_correo", "estudiante.derecho@duocuc.cl") ?: "estudiante.derecho@duocuc.cl"
        val fuente = prefs.getInt("user_fuente", 16)
        return UsuarioEntity(id = 1, nombre = nombre, correo = correo, tamanoFuenteSp = fuente)
    }

    // Actualiza la preferencia de tamaño de letra (A- / A+)
    fun actualizarTamanoFuente(tamanoSp: Int) {
        val actual = obtenerUsuarioActual().copy(tamanoFuenteSp = tamanoSp)
        try {
            db?.usuarioDao()?.guardarUsuario(actual)
        } catch (_: Exception) { }
        prefs.edit().putInt("user_fuente", tamanoSp).apply()
    }

    // Limpia los datos de sesión al confirmar el Modal de "Cerrar sesión"
    fun cerrarSesionUsuario() {
        try {
            db?.usuarioDao()?.cerrarSesion()
        } catch (_: Exception) { }
        prefs.edit()
            .remove("user_nombre")
            .remove("user_correo")
            .apply()
    }

    // Guarda una nota o destacado en Room
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
        } catch (_: Exception) { }

        prefs.edit()
            .putBoolean("dest_${articulo.id}", articulo.esDestacado)
            .putString("nota_${articulo.id}", articulo.nota)
            .apply()
    }

    // Sincroniza la lista de artículos con lo guardado en la base de datos local
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
                val notaPref = prefs.getString("nota_${articulo.id}", articulo.nota) ?: articulo.nota
                articulo.copy(esDestacado = destPref, nota = notaPref)
            }
        }
    }
}