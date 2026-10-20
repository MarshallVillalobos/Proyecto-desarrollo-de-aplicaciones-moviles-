package com.duoc.x.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.duoc.x.model.LoginErrores
import com.duoc.x.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    // Actualiza el correo y limpia el error visual mientras el usuario escribe
    fun onCorreoChange(nuevoCorreo: String) {
        _estado.update {
            it.copy(
                correo = nuevoCorreo,
                errores = it.errores.copy(correo = null)
            )
        }
    }

    // Actualiza la contraseña y limpia el error visual mientras el usuario escribe
    fun onClaveChange(nuevaClave: String) {
        _estado.update {
            it.copy(
                clave = nuevaClave,
                errores = it.errores.copy(clave = null)
            )
        }
    }

    // Alterna la visibilidad de la contraseña
    fun alternarVisibilidadClave() {
        _estado.update {
            it.copy(claveVisible = !it.claveVisible)
        }
    }

    // Validación  del formulario antes de permitir el acceso
    fun validarFormulario(): Boolean {
        val estadoActual = _estado.value

        val errorCorreo = when {
            estadoActual.correo.isBlank() -> "El correo electrónico es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(estadoActual.correo).matches() ->
                "Ingresa un correo válido (ej. estudiante.derecho@duocuc.cl)"
            else -> null
        }

        val errorClave = when {
            estadoActual.clave.isBlank() -> "La contraseña es obligatoria"
            estadoActual.clave.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }

        val nuevosErrores = LoginErrores(
            correo = errorCorreo,
            clave = errorClave
        )

        val hayErrores = listOfNotNull(
            nuevosErrores.correo,
            nuevosErrores.clave
        ).isNotEmpty()

        _estado.update { it.copy(errores = nuevosErrores) }

        return !hayErrores
    }
}