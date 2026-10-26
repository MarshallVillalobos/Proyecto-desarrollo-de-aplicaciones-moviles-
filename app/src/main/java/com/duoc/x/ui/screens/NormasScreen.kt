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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.x.model.NormaItem
import com.duoc.x.navigation.Screen
import com.duoc.x.ui.components.CodigoChBottomBar
import com.duoc.x.ui.components.CodigoChTopBar
import com.duoc.x.ui.theme.*
import com.duoc.x.viewmodel.EstudioViewModel

@Composable
fun NormasScreen(
    viewModel: EstudioViewModel,
    onNormaClick: (String) -> Unit,
    onNavigateToMisEstudios: () -> Unit,
    onProfileClick: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val categorias = listOf("Todas", "Civil", "Laboral", "Constitución")

    val normasFiltradas = estado.normas.filter { norma ->
        val coincideBusqueda = norma.titulo.contains(estado.searchQuery, ignoreCase = true) ||
                norma.descripcion.contains(estado.searchQuery, ignoreCase = true)
        val coincideCategoria = estado.selectedCategoria == "Todas" ||
                norma.categoria.equals(estado.selectedCategoria, ignoreCase = true)
        coincideBusqueda && coincideCategoria
    }

    Scaffold(
        topBar = {
            CodigoChTopBar(
                subtitle = "Código CH",
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
            // 1. Barra de búsqueda conectada al ViewModel
            OutlinedTextField(
                value = estado.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardWhite, shape = RoundedCornerShape(12.dp)),
                placeholder = {
                    Text("Buscar norma o materia...", color = TextMuted, fontSize = 14.sp)
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

            // 2. Filtros por categoría
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categorias) { cat ->
                    val isSelected = estado.selectedCategoria == cat
                    Surface(
                        onClick = { viewModel.onCategoriaSelected(cat) },
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
                    items(normasFiltradas, key = { it.id }) { norma ->
                        NormaCardItem(
                            norma = norma,
                            onClick = { onNormaClick(norma.titulo) } // Envía el título real
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