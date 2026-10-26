package com.duoc.x.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.x.model.ArticuloItem
import com.duoc.x.navigation.Screen
import com.duoc.x.ui.components.CodigoChBottomBar
import com.duoc.x.ui.components.CodigoChTopBar
import com.duoc.x.ui.theme.*
import com.duoc.x.viewmodel.EstudioViewModel

@Composable
fun MisEstudiosScreen(
    viewModel: EstudioViewModel,
    onArticuloClick: (String, String) -> Unit,
    onNavigateToNormas: () -> Unit,
    onProfileClick: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current

    // Estado para alternar entre los chips "Notas" y "Destacados"
    var tabSeleccionado by remember { mutableStateOf("Notas") }

    // Estado para controlar el Modal de confirmación al eliminar una nota
    var articuloAEliminar by remember { mutableStateOf<ArticuloItem?>(null) }

    val listaMostrada = if (tabSeleccionado == "Notas") {
        estado.articulos.filter { it.tieneNota }
    } else {
        estado.articulos.filter { it.esDestacado }
    }

    // Modal (AlertDialog) para confirmar la eliminación de una nota
    articuloAEliminar?.let { articulo ->
        AlertDialog(
            onDismissRequest = { articuloAEliminar = null },
            containerColor = CardWhite,
            title = {
                Text(
                    text = "Eliminar nota",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar tu nota de ${articulo.normaTitulo} (${articulo.numero})?",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarNota(articulo.id)
                        articuloAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Eliminar", color = CardWhite)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { articuloAEliminar = null },
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
                title = "Mis estudios",
                showBackButton = false,
                onProfileClick = onProfileClick
            )
        },
        bottomBar = {
            CodigoChBottomBar(
                currentRoute = Screen.MisEstudios.route,
                onNavigateToNormas = onNavigateToNormas,
                onNavigateToMisEstudios = {}
            )
        },
        containerColor = BgApp
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgApp)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Fila de Chips: "Notas" y "Destacados" (Mockup Pantalla 6)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Notas", "Destacados").forEach { tab ->
                    val activo = tabSeleccionado == tab
                    Surface(
                        onClick = { tabSeleccionado = tab },
                        shape = RoundedCornerShape(22.dp),
                        color = if (activo) PrimaryNavy else CardWhite,
                        border = BorderStroke(1.dp, if (activo) PrimaryNavy else BorderBeige),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 18.dp)
                        ) {
                            Text(
                                text = tab,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (activo) CardWhite else TextMain
                            )
                        }
                    }
                }
            }

            if (listaMostrada.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (tabSeleccionado == "Notas") {
                            "Aún no tienes notas guardadas."
                        } else {
                            "Aún no tienes artículos destacados."
                        },
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listaMostrada, key = { it.id }) { articulo ->
                        EstudioCardItem(
                            articulo = articulo,
                            onClick = { onArticuloClick(articulo.normaTitulo, articulo.numero) },
                            onShareClick = {
                                // Recurso Nativo 2: Compartir/Exportar apunte legal con Intent nativo
                                val textoCompartir = buildString {
                                    append("📘 ${articulo.normaTitulo} - ${articulo.numero}\n\n")
                                    append("\"${articulo.extracto}\"\n\n")
                                    if (articulo.tieneNota) {
                                        append("📝 Mi nota de estudio:\n${articulo.nota}")
                                    }
                                }
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "${articulo.normaTitulo} - ${articulo.numero}")
                                    putExtra(Intent.EXTRA_TEXT, textoCompartir)
                                }
                                context.startActivity(
                                    Intent.createChooser(sendIntent, "Compartir apunte legal")
                                )
                            },
                            onDeleteNoteClick = if (articulo.tieneNota) {
                                { articuloAEliminar = articulo }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EstudioCardItem(
    articulo: ArticuloItem,
    onClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteNoteClick: (() -> Unit)?
) {
    val ambosActivos = articulo.tieneNota && articulo.esDestacado
    val textoInsignia = when {
        ambosActivos -> "Nota + destacado"
        articulo.tieneNota -> "Nota"
        else -> "Destacado"
    }
    val fondoInsignia = if (ambosActivos || !articulo.tieneNota) HighlightYellow else BadgeBlueBg
    val colorTextoInsignia = if (ambosActivos || !articulo.tieneNota) TextMain else PrimaryNavy

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, BorderBeige)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = articulo.normaTitulo,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = articulo.numero,
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }

                // Acciones: Compartir (Recurso Nativo) y Eliminar Nota (Modal)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir o exportar apunte",
                            tint = PrimaryNavy,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (onDeleteNoteClick != null) {
                        IconButton(
                            onClick = onDeleteNoteClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Eliminar nota",
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = if (articulo.tieneNota) articulo.nota else articulo.extracto,
                fontSize = 13.sp,
                color = TextMain,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = fondoInsignia
            ) {
                Text(
                    text = textoInsignia,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorTextoInsignia,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}