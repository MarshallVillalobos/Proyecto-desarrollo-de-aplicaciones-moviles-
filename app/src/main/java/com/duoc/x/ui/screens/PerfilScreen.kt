package com.duoc.x.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.duoc.x.R
import com.duoc.x.navigation.Screen
import com.duoc.x.ui.components.CodigoChBottomBar
import com.duoc.x.ui.components.CodigoChTopBar
import com.duoc.x.ui.theme.*
import com.duoc.x.viewmodel.EstudioViewModel
import kotlin.math.roundToInt

@Composable
fun PerfilScreen(
    viewModel: EstudioViewModel,
    onBackClick: () -> Unit,
    onNavigateToNormas: () -> Unit,
    onNavigateToMisEstudios: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current

    // Estado para mostrar u ocultar el Modal de confirmación al cerrar sesión
    var mostrarModalCerrarSesion by remember { mutableStateOf(false) }

    // Lanzador de permisos en tiempo de ejecución para Notificaciones (Android 13+)
    val permisoNotificacionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            enviarNotificacionRecordatorio(context)
        } else {
            Toast.makeText(
                context,
                "Permiso de notificaciones denegado por el usuario",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Modal emergente (AlertDialog) para confirmar el cierre de sesión
    if (mostrarModalCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarModalCerrarSesion = false },
            containerColor = CardWhite,
            title = {
                Text(
                    text = "Cerrar sesión",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de cerrar sesión en Código CH?",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarModalCerrarSesion = false
                        viewModel.cerrarSesion()
                        onLogoutSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Sí, cerrar sesión", color = CardWhite)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarModalCerrarSesion = false },
                    border = BorderStroke(1.dp, BorderBeige)
                ) {
                    Text("Cancelar", color = TextMain)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CodigoChTopBar(
                subtitle = "Código CH",
                title = "Perfil",
                showBackButton = true,
                isProfileScreen = true,
                onBackClick = onBackClick,
                onProfileClick = {}
            )
        },
        bottomBar = {
            CodigoChBottomBar(
                currentRoute = Screen.Perfil.route,
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
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Tarjeta de Avatar y Datos del Estudiante
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, BorderBeige)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar circular con iniciales
                    val iniciales = estado.usuarioNombre
                        .split(" ")
                        .take(2)
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .joinToString("")
                        .ifEmpty { "CV" }

                    Surface(
                        shape = CircleShape,
                        color = BadgeBlueBg,
                        border = BorderStroke(2.dp, PrimaryNavy),
                        modifier = Modifier.size(76.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = iniciales,
                                fontFamily = FontFamily.Serif,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = estado.usuarioNombre,
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = estado.usuarioCorreo,
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BadgeBlueBg
                    ) {
                        Text(
                            text = "Perfil Estudiante (Sesión Room activa)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryNavy,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 2. Tarjeta de Preferencia de lectura con deslizador (A- / A+)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, BorderBeige)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Preferencia de lectura",
                        fontFamily = FontFamily.Serif,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                    Text(
                        text = "Tamaño de letra predeterminado para los artículos",
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, BorderBeige),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = viewModel::disminuirFuente,
                                enabled = estado.fontSizeSp > 12,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BorderBeige),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = BgApp),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Text("A−", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 14.sp)
                            }

                            Slider(
                                value = estado.fontSizeSp.toFloat(),
                                onValueChange = { nuevoValor ->
                                    viewModel.cambiarFuenteDirecto(nuevoValor.roundToInt())
                                },
                                valueRange = 12f..28f,
                                steps = 7,
                                colors = SliderDefaults.colors(
                                    thumbColor = PrimaryNavy,
                                    activeTrackColor = PrimaryNavy,
                                    inactiveTrackColor = BorderBeige
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedButton(
                                onClick = viewModel::aumentarFuente,
                                enabled = estado.fontSizeSp < 28,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BorderBeige),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = BgApp),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Text("A+", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 16.sp)
                            }
                        }
                    }

                    val etiquetaTamano = when {
                        estado.fontSizeSp <= 14 -> "Pequeño"
                        estado.fontSizeSp <= 18 -> "Mediano"
                        else -> "Grande"
                    }

                    Text(
                        text = "Actual: $etiquetaTamano (${estado.fontSizeSp} sp)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryNavy,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 3. Tarjeta de Recurso Nativo : Recordatorio de estudio
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, BorderBeige)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Recordatorio de estudio",
                        fontFamily = FontFamily.Serif,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                    Text(
                        text = "Recibe una notificación en tu dispositivo para repasar tus apuntes y artículos destacados.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val tienePermiso = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED

                                if (tienePermiso) {
                                    enviarNotificacionRecordatorio(context)
                                } else {
                                    permisoNotificacionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                enviarNotificacionRecordatorio(context)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "Activar recordatorio",
                            tint = CardWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Programar recordatorio diario",
                            color = CardWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. Botón inferior: Cerrar sesión (Abre el Modal de confirmación)
            OutlinedButton(
                onClick = { mostrarModalCerrarSesion = true },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, DangerRed),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardWhite,
                    contentColor = DangerRed
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Cerrar sesión",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DangerRed
                )
            }
        }
    }
}

/**
 * Recurso Nativo: Construye y emite una notificación local en el dispositivo móvil.
 */
private fun enviarNotificacionRecordatorio(context: Context) {
    val channelId = "codigo_ch_estudio_channel"
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Recordatorios de Estudio Legal",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Canal para recordatorios de lectura y apuntes en Código CH"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.logo_app)
        .setContentTitle("📘 Código CH · Recordatorio de estudio")
        .setContentText("Es hora de repasar tus artículos destacados y notas legales.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(1001, notification)
    Toast.makeText(
        context,
        "Recordatorio activado: Revisa tu barra de notificaciones",
        Toast.LENGTH_SHORT
    ).show()
}