package com.duoc.x.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.x.model.LoginUiState
import com.duoc.x.repository.CodigoChRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
        repository.alternarVisibilidadClave()
    }

    fun validarFormulario(correo: String, clave: String, resultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            val (nuevoEstado, esValido) = repository.autenticarUsuario(
                estadoActual = estado_mutable.value,
                correo = correo,
                clave = clave
            )
            estado_mutable.value = nuevoEstado
            resultado(esValido)
        }
    }


}

