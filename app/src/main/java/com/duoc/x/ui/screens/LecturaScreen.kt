package com.duoc.x.ui.screens

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
import androidx.compose.runtime.*
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

@Composable
fun LecturaScreen(
    normaNombre: String,
    articuloNumero: String,
    onAgregarNotaClick: () -> Unit,
    onBackClick: () -> Unit,
    onNavigateToNormas: () -> Unit,
    onNavigateToMisEstudios: () -> Unit,
    onProfileClick: () -> Unit
) {
    // Estado local para tamaño de texto y destacado/subrayado
    var fontSizeSp by remember { mutableIntStateOf(16) }
    var esDestacado by remember { mutableStateOf(false) }

    // Texto de prueba según el artículo seleccionado
    val contenidoArticulo = remember(articuloNumero) {
        when {
            articuloNumero.contains("1") -> "Las relaciones laborales entre empleadores y trabajadores se regularán por este Código y por sus leyes complementarias.\n\nEstas normas no se aplicarán, sin embargo, a los funcionarios de la Administración del Estado, centralizada y descentralizada, del Congreso Nacional y del Poder Judicial, ni a los del Ministerio Público."
            articuloNumero.contains("2") -> "Las funciones o servicios prestados en una empresa deben considerar la igual dignidad de los trabajadores y la protección social.\n\nSon contrarios a los principios de las leyes laborales los actos de discriminación. Se consideran actos de discriminación las distinciones, exclusiones o preferencias basadas en motivos de raza, color, sexo, edad, estado civil, sindicación o religión."
            else -> "Para todos los efectos legales se entiende por empleador la persona natural o jurídica que utiliza los servicios de uno o más trabajadores en virtud de un contrato de trabajo.\n\nTrabajador es toda persona natural que presta servicios personales intelectuales o materiales bajo dependencia o subordinación, en virtud de un contrato de trabajo."
        }
    }

    Scaffold(
        topBar = {
            CodigoChTopBar(
                subtitle = if (normaNombre.isNotEmpty()) "4. LECTURA • $normaNombre" else "4. LECTURA",
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
            // Cuerpo de la pantalla desplazable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Control de tamaño de fuente (A- / A+) estilo Mockup
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
                            text = "Tamaño de fuente",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { if (fontSizeSp > 12) fontSizeSp -= 2 },
                                enabled = fontSizeSp > 12,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BorderBeige)
                            ) {
                                Text("A-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMain)
                            }

                            Text(
                                text = "${fontSizeSp}sp",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )

                            OutlinedButton(
                                onClick = { if (fontSizeSp < 28) fontSizeSp += 2 },
                                enabled = fontSizeSp < 28,
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

                        // Cuadro del texto legal con resaltado de color
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (esDestacado) HighlightYellow else QuoteBg,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Text(
                                text = contenidoArticulo,
                                fontSize = fontSizeSp.sp,
                                lineHeight = (fontSizeSp + 8).sp,
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
                    onClick = { esDestacado = !esDestacado },
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
                    onClick = onAgregarNotaClick,
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