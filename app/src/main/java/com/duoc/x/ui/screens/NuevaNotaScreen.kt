package com.duoc.x.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.x.navigation.Screen
import com.duoc.x.ui.components.CodigoChBottomBar
import com.duoc.x.ui.components.CodigoChTopBar
import com.duoc.x.ui.theme.*
import com.duoc.x.viewmodel.EstudioViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NuevaNotaScreen(
    normaNombre: String,
    articuloNumero: String,
    viewModel: EstudioViewModel,
    onGuardarNotaSuccess: () -> Unit,
    onBackClick: () -> Unit,
    onNavigateToNormas: () -> Unit,
    onNavigateToMisEstudios: () -> Unit,
    onProfileClick: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Estado local para disparar la animación de éxito al guardar (IE 2.2.2)
    var mostrarAnimacionExito by remember { mutableStateOf(false) }

    val articulo = estado.articulos.find {
        it.normaTitulo.equals(normaNombre, ignoreCase = true) &&
                it.numero.equals(articuloNumero, ignoreCase = true)
    }
    val textoCita = articulo?.extracto
        ?: "Empleador: la persona natural o jurídica que utiliza los servicios de otras personas..."

    Scaffold(
        topBar = {
            CodigoChTopBar(
                subtitle = "$normaNombre · $articuloNumero",
                title = "Nueva nota",
                showBackButton = true,
                onBackClick = onBackClick,
                onProfileClick = onProfileClick
            )
        },
        bottomBar = {
            CodigoChBottomBar(
                currentRoute = Screen.NuevaNota.route,
                onNavigateToNormas = onNavigateToNormas,
                onNavigateToMisEstudios = onNavigateToMisEstudios
            )
        },
        containerColor = BgApp
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgApp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Bloque superior con la cita del artículo (Estilo Mockup con borde izquierdo azul)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(QuoteBg, shape = RoundedCornerShape(8.dp))
                        .drawBehind {
                            val strokeWidth = 4.dp.toPx()
                            drawLine(
                                color = PrimaryNavy,
                                start = Offset(strokeWidth / 2, 0f),
                                end = Offset(strokeWidth / 2, size.height),
                                strokeWidth = strokeWidth
                            )
                        }
                        .padding(start = 16.dp, top = 12.dp, end = 12.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "\"$textoCita\"",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = TextMuted
                    )
                }

                // Etiqueta del formulario
                Text(
                    text = "Tu nota",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMain
                )

                // Campo de texto validado desde EstudioViewModel con retroalimentación visual e ícono
                OutlinedTextField(
                    value = estado.borradorNota,
                    onValueChange = viewModel::onBorradorNotaChange,
                    placeholder = {
                        Text(
                            text = "Escribe aquí tu apunte o recordatorio para la prueba...",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    },
                    isError = estado.errorNota != null,
                    trailingIcon = {
                        if (estado.errorNota != null) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = "Error en el campo de nota",
                                tint = DangerRed
                            )
                        }
                    },
                    supportingText = {
                        estado.errorNota?.let { mensajeError ->
                            Text(
                                text = mensajeError,
                                color = DangerRed,
                                fontSize = 12.sp
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain,
                        errorTextColor = TextMain,
                        cursorColor = PrimaryNavy,
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        errorContainerColor = CardWhite,
                        focusedBorderColor = PrimaryNavy,
                        unfocusedBorderColor = BorderBeige,
                        errorBorderColor = DangerRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )

                // Animación funcional de confirmación al guardar la nota (IE 2.2.2)
                AnimatedVisibility(
                    visible = mostrarAnimacionExito,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BadgeBlueBg,
                        border = BorderStroke(1.dp, PrimaryNavy),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Nota guardada",
                                tint = PrimaryNavy
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¡Apunte guardado correctamente!",
                                color = PrimaryNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Botones inferiores: Cancelar y Guardar (Mockup Pantalla 5)
            HorizontalDivider(color = BorderBeige, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgApp)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, BorderBeige),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CardWhite,
                        contentColor = TextMain
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Cancelar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        if (viewModel.validarYGuardarNota(normaNombre, articuloNumero)) {
                            coroutineScope.launch {
                                mostrarAnimacionExito = true
                                delay(650)
                                mostrarAnimacionExito = false
                                onGuardarNotaSuccess()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Guardar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CardWhite
                    )
                }
            }
        }
    }
}