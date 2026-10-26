package com.duoc.x.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.x.R
import com.duoc.x.ui.theme.*
import com.duoc.x.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    // Observamos el estado reactivo desde el LoginViewModel
    val estado by viewModel.estado.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgApp)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Cabecera: Contenedor con el Logo oficial y textos de Código CH
        Surface(
            modifier = Modifier
                .size(84.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            color = PrimaryNavy
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_app),
                    contentDescription = "Logotipo Código CH",
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "CH",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CardWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Código CH",
            fontFamily = FontFamily.Serif,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain
        )

        Text(
            text = "Estudio y consulta móvil de legislación actualizada",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
        )

        // Campo 1: Correo electrónico con validación visual e ícono de error
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Correo",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = estado.correo,
                onValueChange = viewModel::onCorreoChange,
                placeholder = {
                    Text("estudiante.derecho@duocuc.cl", color = TextMuted, fontSize = 14.sp)
                },
                singleLine = true,
                isError = estado.errores.correo != null,
                trailingIcon = {
                    if (estado.errores.correo != null) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Error en correo",
                            tint = DangerRed
                        )
                    }
                },
                supportingText = {
                    estado.errores.correo?.let { mensajeError ->
                        Text(
                            text = mensajeError,
                            color = DangerRed,
                            fontSize = 12.sp
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardWhite,
                    unfocusedContainerColor = CardWhite,
                    errorContainerColor = CardWhite,
                    focusedBorderColor = PrimaryNavy,
                    unfocusedBorderColor = BorderBeige,
                    errorBorderColor = DangerRed
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo 2: Contraseña con transformación visual, ícono de alerta y botón de mostrar/ocultar
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Contraseña",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = estado.clave,
                onValueChange = viewModel::onClaveChange,
                placeholder = {
                    Text("••••••••••••", color = TextMuted, fontSize = 14.sp)
                },
                singleLine = true,
                visualTransformation = if (estado.claveVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                isError = estado.errores.clave != null,
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        if (estado.errores.clave != null) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = "Error en contraseña",
                                tint = DangerRed
                            )
                        }
                        IconButton(onClick = viewModel::alternarVisibilidadClave) {
                            Icon(
                                imageVector = if (estado.claveVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = "Alternar visibilidad de contraseña",
                                tint = TextMuted
                            )
                        }
                    }
                },
                supportingText = {
                    estado.errores.clave?.let { mensajeError ->
                        Text(
                            text = mensajeError,
                            color = DangerRed,
                            fontSize = 12.sp
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardWhite,
                    unfocusedContainerColor = CardWhite,
                    errorContainerColor = CardWhite,
                    focusedBorderColor = PrimaryNavy,
                    unfocusedBorderColor = BorderBeige,
                    errorBorderColor = DangerRed
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Botón principal: Iniciar sesión
        Button(
            onClick = {
                if (viewModel.validarFormulario()) {
                    onLoginSuccess(estado.correo)
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Iniciar sesión",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = CardWhite
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Enlace inferior: Crear cuenta (Autocompleta credenciales de prueba al tocarlo)
        Box(
            modifier = Modifier
                .heightIn(min = 44.dp)
                .clickable {
                    viewModel.onCorreoChange("estudiante.derecho@duocuc.cl")
                    viewModel.onClaveChange("duoc2025")
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Crear cuenta",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryNavy,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}