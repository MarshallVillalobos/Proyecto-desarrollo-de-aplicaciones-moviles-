package com.duoc.x.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.x.navigation.Screen
import com.duoc.x.ui.components.CodigoChBottomBar
import com.duoc.x.ui.components.CodigoChTopBar
import com.duoc.x.ui.theme.*
import com.duoc.x.viewmodel.EstudioViewModel

@Composable
fun LecturaScreen(
    normaNombre: String,
    articuloNumero: String,
    viewModel: EstudioViewModel,
    onAgregarNotaClick: () -> Unit,
    onBackClick: () -> Unit,
    onNavigateToNormas: () -> Unit,
    onNavigateToMisEstudios: () -> Unit,
    onProfileClick: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val articulo = estado.articulos.find {
        it.normaTitulo.equals(normaNombre, ignoreCase = true) &&
                it.numero.equals(articuloNumero, ignoreCase = true)
    }

    val esDestacado = articulo?.esDestacado ?: false
    val contenidoArticulo = articulo?.contenido ?: "Contenido del artículo no disponible."

    // Animación de transición de color al destacar el artículo
    val colorFondoTexto by animateColorAsState(
        targetValue = if (esDestacado) HighlightYellow else QuoteBg,
        animationSpec = tween(durationMillis = 350),
        label = "AnimacionResaltadoLegal"
    )

    Scaffold(
        topBar = {
            CodigoChTopBar(
                subtitle = normaNombre.ifEmpty { "Lectura" },
                title = articuloNumero.ifEmpty { "Artículo" },
                showBackButton = true,
                onBackClick = onBackClick,
                onProfileClick = onProfileClick
            )
        },
        bottomBar = {
            CodigoChBottomBar(
                currentRoute = Screen.Lectura.route,
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
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Control de tamaño de fuente (A- / A+) conectado al ViewModel
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderBeige, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = CardWhite
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tamaño de letra",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = viewModel::disminuirFuente,
                                enabled = estado.fontSizeSp > 12,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BorderBeige)
                            ) {
                                Text("A-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMain)
                            }

                            Text(
                                text = "${estado.fontSizeSp} sp",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )

                            OutlinedButton(
                                onClick = viewModel::aumentarFuente,
                                enabled = estado.fontSizeSp < 28,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BorderBeige)
                            ) {
                                Text("A+", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMain)
                            }
                        }
                    }
                }

                // Tarjeta contenedora con la lectura del artículo
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderBeige, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = articuloNumero.ifEmpty { "Artículo" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = TextMain
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = colorFondoTexto,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Text(
                                text = contenidoArticulo,
                                fontSize = estado.fontSizeSp.sp,
                                lineHeight = (estado.fontSizeSp + 8).sp,
                                fontFamily = FontFamily.Serif,
                                color = TextMain
                            )
                        }
                    }
                }
            }

            // Barra inferior con acciones rápidas: Destacar / Agregar nota
            HorizontalDivider(color = BorderBeige, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardWhite)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.alternarDestacado(normaNombre, articuloNumero) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, if (esDestacado) PrimaryNavy else BorderBeige),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (esDestacado) HighlightYellow else CardWhite
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = if (esDestacado) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = PrimaryNavy,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (esDestacado) "Destacado" else "Destacar",
                        color = PrimaryNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = {
                        viewModel.prepararBorradorNota(normaNombre, articuloNumero)
                        onAgregarNotaClick()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = CardWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Agregar nota",
                        color = CardWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}