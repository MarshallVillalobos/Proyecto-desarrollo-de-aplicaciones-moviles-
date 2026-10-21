package com.duoc.x.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duoc.x.navigation.Screen
import com.duoc.x.ui.components.CodigoChBottomBar
import com.duoc.x.ui.components.CodigoChTopBar
import com.duoc.x.ui.theme.*
@Composable
fun MisEstudiosScreen(
    onNavigateToNormas: () -> Unit,
    onProfileClick: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("6. Pantalla Mis Estudios (Base)", color = TextMain)
        }
    }
}