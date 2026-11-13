package com.duoc.x.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.duoc.x.model.LoginUiState
import com.duoc.x.repository.CodigoChRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(application: Application) : AndroidViewModel(application) {

        private val repository = CodigoChRepository(application)

    private val estado_mutable = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = estado_mutable.asStateFlow()

    fun onCorreoChange(nuevoCorreo: String) {
        estado_mutable.update {
            it.copy(
                correo = nuevoCorreo,
                errores = it.errores.copy(correo = null)
            )
        }
    }

    fun onClaveChange(nuevaClave: String) {
        estado_mutable.update {
            it.copy(
                clave = nuevaClave,
                errores = it.errores.copy(clave = null)
            )
        }
    }

    fun alternarVisibilidadClave() {
        estado_mutable.update {
            it.copy(claveVisible = !it.claveVisible)
        }
    }

    fun validarFormulario(correo: String, clave: String): Boolean {
        val (nuevoEstado, esValido) = repository.validarFormulario(
            estadoActual = estado_mutable.value,
            correo = correo,
            clave = clave
        )
        estado_mutable.value = nuevoEstado
        return esValido
    }


}

