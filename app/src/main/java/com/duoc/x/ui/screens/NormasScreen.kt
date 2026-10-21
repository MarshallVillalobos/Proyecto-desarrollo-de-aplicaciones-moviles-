package com.duoc.x.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Search
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

// Modelo interno de datos para las normas
data class NormaItem(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormasScreen(
    onNormaClick: (String) -> Unit,
    onNavigateToMisEstudios: () -> Unit,
    onProfileClick: () -> Unit
) {
    // Lista de leyes y códigos según el Mockup
    val listaNormas = remember {
        listOf(
            NormaItem(
                id = "c_civil",
                titulo = "Código Civil",
                descripcion = "Materia: Derecho Civil, Contratos y Propiedad",
                categoria = "Civil"
            ),
            NormaItem(
                id = "c_trabajo",
                titulo = "Código del Trabajo",
                descripcion = "Materia: Derecho Laboral, Protección y Contratos",
                categoria = "Laboral"
            ),
            NormaItem(
                id = "c_constitucion",
                titulo = "Constitución Política",
                descripcion = "Materia: Derecho Político, Bases de la Institucionalidad",
                categoria = "Constitución"
            )
        )
    }

    // Estados para la búsqueda y el filtro de categorías
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoria by remember { mutableStateOf("Todas") }

    val categorias = listOf("Todas", "Civil", "Laboral", "Constitución")

    // Filtrado dinámico en tiempo real
    val normasFiltradas = listaNormas.filter { norma ->
        val coincideBusqueda = norma.titulo.contains(searchQuery, ignoreCase = true) ||
                norma.descripcion.contains(searchQuery, ignoreCase = true)
        val coincideCategoria = selectedCategoria == "Todas" || norma.categoria.equals(selectedCategoria, ignoreCase = true)
        coincideBusqueda && coincideCategoria
    }

    Scaffold(
        topBar = {
            CodigoChTopBar(
                subtitle = "2. NORMAS",
                title = "Normas",
                showBackButton = false,
                onProfileClick = onProfileClick
            )
        },
        bottomBar = {
            CodigoChBottomBar(
                currentRoute = Screen.Normas.route,
                onNavigateToNormas = {},
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
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Bar de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardWhite, shape = RoundedCornerShape(12.dp)),
                placeholder = {
                    Text("Buscar norma o categoría...", color = TextMuted, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = TextMuted
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryNavy,
                    unfocusedBorderColor = BorderBeige,
                    focusedContainerColor = CardWhite,
                    unfocusedContainerColor = CardWhite
                )
            )

            // 2. Filtros por categoría (Chips horizontal)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categorias) { cat ->
                    val isSelected = selectedCategoria == cat
                    Surface(
                        onClick = { selectedCategoria = cat },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) PrimaryNavy else CardWhite,
                        border = if (isSelected) null else BorderStroke(1.dp, BorderBeige),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) CardWhite else TextMain,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // 3. Lista de Códigos de Ley
            if (normasFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron normas coincidentes.",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(normasFiltradas) { norma ->
                        NormaCardItem(
                            norma = norma,
                            onClick = { onNormaClick(norma.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NormaCardItem(
    norma: NormaItem,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = norma.titulo,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = TextMain
                )
                Text(
                    text = norma.descripcion,
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Ir a artículos",
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}