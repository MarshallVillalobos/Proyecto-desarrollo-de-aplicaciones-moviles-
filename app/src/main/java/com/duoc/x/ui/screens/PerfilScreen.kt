package com.duoc.x.ui.screens

import androidx.compose.foundation.BorderStroke
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
fun PerfilScreen(
    onBackClick: () -> Unit,
    onNavigateToNormas: () -> Unit,
    onNavigateToMisEstudios: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
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
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("7. Pantalla de Perfil (Base)", color = TextMain)

            OutlinedButton(
                onClick = onLogoutSuccess,
                border = BorderStroke(1.5.dp, DangerRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Cerrar sesión (Volver a Login)")
            }
        }
    }
}