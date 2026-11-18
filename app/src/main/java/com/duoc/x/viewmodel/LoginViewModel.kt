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

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun onCorreoChange(nuevoCorreo: String) {
        _estado.update { repository.actualizarCorreo(it, nuevoCorreo) }
    }

    fun onClaveChange(nuevaClave: String) {
        _estado.update { repository.actualizarClave(it, nuevaClave) }
    }

    fun alternarVisibilidadClave() {
        _estado.update { repository.alternarVisibilidadClave(it) }
    }

    fun validarFormulario(correo: String, clave: String, resultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            val (nuevoEstado, esValido) = repository.autenticarUsuario(
                estadoActual = _estado.value,
                correo = correo,
                clave = clave
            )
            _estado.value = nuevoEstado
            resultado(esValido)
        }
    }

    fun registrarUsuario(
        correo: String,
        clave: String,
        onResultado: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val (nuevoEstado, creado) = repository.registrarUsuario(
                estadoActual = _estado.value,
                correo = correo,
                clave = clave
            )
            _estado.value = nuevoEstado
            onResultado(creado)
        }
    }


}

