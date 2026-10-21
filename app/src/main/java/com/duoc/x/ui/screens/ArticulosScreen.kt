package com.duoc.x.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.x.navigation.Screen
import com.duoc.x.ui.components.CodigoChBottomBar
import com.duoc.x.ui.components.CodigoChTopBar
import com.duoc.x.ui.theme.*

// Modelo interno para los datos de prueba de artículos
data class ArticuloPlaceholder(
    val id: String,
    val numero: String,
    val extracto: String,
    val tieneNota: Boolean = false
)

@Composable
fun ArticulosScreen(
    normaNombre: String,
    onArticuloClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onNavigateToNormas: () -> Unit,
    onNavigateToMisEstudios: () -> Unit,
    onProfileClick: () -> Unit
) {
    // Generación de artículos ficticios según la norma seleccionada
    val listaArticulos = remember(normaNombre) {
        when {
            normaNombre.contains("Trabajo", ignoreCase = true) -> listOf(
                ArticuloPlaceholder(
                    id = "art1",
                    numero = "Art. 1",
                    extracto = "Las relaciones laborales entre empleadores y trabajadores se regularán por este Código y por sus leyes complementarias.",
                    tieneNota = true
                ),
                ArticuloPlaceholder(
                    id = "art2",
                    numero = "Art. 2",
                    extracto = "Las funciones o servicios prestados en una empresa deben considerar la igual dignidad de los trabajadores y la protección social.",
                    tieneNota = false
                ),
                ArticuloPlaceholder(
                    id = "art3",
                    numero = "Art. 3",
                    extracto = "Para todos los efectos legales se entiende por empleador la persona natural o jurídica que utiliza los servicios de uno o más trabajadores...",
                    tieneNota = true
                ),
                ArticuloPlaceholder(
                    id = "art4",
                    numero = "Art. 4",
                    extracto = "Se presume de derecho que representa al empleador y que en tal carácter obliga a este con los trabajadores el gerente, el director...",
                    tieneNota = false
                )
            )
            normaNombre.contains("Civil", ignoreCase = true) -> listOf(
                ArticuloPlaceholder(
                    id = "art1",
                    numero = "Art. 1",
                    extracto = "La ley es una declaración de la voluntad soberana que, manifestada en la forma prescrita por la Constitución, manda, prohíbe o permite.",
                    tieneNota = false
                ),
                ArticuloPlaceholder(
                    id = "art2",
                    numero = "Art. 2",
                    extracto = "La costumbre no constituye derecho sino en los casos en que la ley se remite a ella.",
                    tieneNota = true
                ),
                ArticuloPlaceholder(
                    id = "art3",
                    numero = "Art. 3",
                    extracto = "Sólo toca al legislador explicar o interpretar la ley de un modo generalmente obligatorio.",
                    tieneNota = false
                )
            )
            else -> listOf(
                ArticuloPlaceholder(
                    id = "art1",
                    numero = "Art. 1",
                    extracto = "Las personas nacen libres e iguales en dignidad y derechos. La familia es el núcleo fundamental de la sociedad.",
                    tieneNota = true
                ),
                ArticuloPlaceholder(
                    id = "art2",
                    numero = "Art. 2",
                    extracto = "Es deber del Estado resguardar la seguridad nacional, dar protección a la población y a la familia.",
                    tieneNota = false
                )
            )
        }
    }

    Scaffold(
        topBar = {
            CodigoChTopBar(
                subtitle = "3. ARTÍCULOS",
                title = normaNombre.ifEmpty { "Norma Legal" },
                showBackButton = true,
                onBackClick = onBackClick,
                onProfileClick = onProfileClick
            )
        },
        bottomBar = {
            CodigoChBottomBar(
                currentRoute = Screen.Normas.route,
                onNavigateToNormas = onNavigateToNormas,
                onNavigateToMisEstudios = onNavigateToMisEstudios
            )
        },
        containerColor = BgApp
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgApp)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(listaArticulos) { articulo ->
                ArticuloCardItem(
                    articulo = articulo,
                    onClick = { onArticuloClick(articulo.numero) }
                )
            }
        }
    }
}

@Composable
fun ArticuloCardItem(
    articulo: ArticuloPlaceholder,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Encabezado de la tarjeta: Número de artículo + Insignia si tiene nota
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = articulo.numero,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = TextMain
                )

                if (articulo.tieneNota) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BadgeBlueBg,
                        border = BorderStroke(1.dp, PrimaryNavy.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Con nota",
                                tint = PrimaryNavy,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Con nota",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryNavy
                            )
                        }
                    }
                }
            }

            // Vista previa del texto del artículo
            Text(
                text = articulo.extracto,
                fontSize = 13.sp,
                color = TextMuted,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            // Indicador inferior de navegación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Leer completo",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryNavy
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = PrimaryNavy,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}