package com.duoc.x.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.x.navigation.Screen
import com.duoc.x.ui.theme.*

/**
 * Barra superior reutilizable según el Mockup de Código CH.
 * Incluye subtítulo, título en estilo Serif, botón de retroceso opcional e ícono global de Perfil.
 */
@Composable
fun CodigoChTopBar(
    subtitle: String,
    title: String,
    showBackButton: Boolean = false,
    isProfileScreen: Boolean = false,
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Column(modifier = Modifier.background(BgApp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showBackButton) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás",
                            tint = TextMain
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Column {
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = title,
                        fontSize = if (showBackButton) 20.sp else 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = TextMain
                    )
                }
            }

            // Botón circular de Perfil en la esquina superior derecha
            Surface(
                onClick = onProfileClick,
                shape = CircleShape,
                color = if (isProfileScreen) PrimaryNavy else CardWhite,
                border = BorderStroke(1.5.dp, PrimaryNavy),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Abrir perfil",
                        tint = if (isProfileScreen) CardWhite else PrimaryNavy
                    )
                }
            }
        }
        HorizontalDivider(color = BorderBeige, thickness = 1.dp)
    }
}

/**
 * Barra de navegación inferior (Bottom Navigation) con las pestañas "Normas" y "Mis estudios".
 */
@Composable
fun CodigoChBottomBar(
    currentRoute: String,
    onNavigateToNormas: () -> Unit,
    onNavigateToMisEstudios: () -> Unit
) {
    Column {
        HorizontalDivider(color = BorderBeige, thickness = 1.dp)
        NavigationBar(
            containerColor = CardWhite,
            tonalElevation = 0.dp
        ) {
            val isNormasSelected = currentRoute != Screen.MisEstudios.route && currentRoute != Screen.Perfil.route
            val isMisEstudiosSelected = currentRoute == Screen.MisEstudios.route

            NavigationBarItem(
                selected = isNormasSelected,
                onClick = onNavigateToNormas,
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = "Normas"
                    )
                },
                label = { Text("Normas", fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryNavy,
                    selectedTextColor = PrimaryNavy,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = BadgeBlueBg
                )
            )

            NavigationBarItem(
                selected = isMisEstudiosSelected,
                onClick = onNavigateToMisEstudios,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = "Mis estudios"
                    )
                },
                label = { Text("Mis estudios", fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryNavy,
                    selectedTextColor = PrimaryNavy,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = BadgeBlueBg
                )
            )
        }
    }
}