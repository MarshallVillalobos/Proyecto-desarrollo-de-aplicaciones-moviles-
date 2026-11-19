package com.duoc.x.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.x.ui.components.CodigoChTopBar
import com.duoc.x.ui.theme.BgApp
import com.duoc.x.ui.theme.PrimaryNavy
import com.duoc.x.viewmodel.DevViewModel

@Composable
fun DevScreen(
    viewModel: DevViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    Scaffold(
        topBar = {
            CodigoChTopBar(
                subtitle = "Endpoints Xano",
                title = "Panel Desarrollo",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = BgApp
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner de confirmación / error
            estado.mensajeFeedback?.let { mensaje ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (estado.esError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = mensaje,
                        color = if (estado.esError) Color(0xFFB71C1C) else Color(0xFF1B5E20),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // =========================================================
            // SECCIÓN 1: BORRAR USUARIOS (DELETE loginrequest/{id})
            // =========================================================
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Usuarios en Xano (DELETE)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryNavy)
                    if (estado.usuarios.isEmpty()) {
                        Text("No hay usuarios cargados.", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        estado.usuarios.forEach { usuario ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "#${usuario.id ?: "?"} - ${usuario.correo}",
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { usuario.id?.let { viewModel.eliminarUsuario(it) } },
                                    enabled = usuario.id != null
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar usuario", tint = Color(0xFFC62828))
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }

            // =========================================================
            // SECCIÓN 2: AÑADIR ARTÍCULO (POST articulo)
            // =========================================================
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("2. Añadir Artículo (POST)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryNavy)

                    OutlinedTextField(
                        value = estado.nuevoCodigoId,
                        onValueChange = viewModel::onNuevoCodigoIdChange,
                        label = { Text("Código ID (ej: cc_art10)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = estado.nuevaNormaTitulo,
                        onValueChange = viewModel::onNuevaNormaChange,
                        label = { Text("Norma (ej: Código Civil)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = estado.nuevoNumero,
                        onValueChange = viewModel::onNuevoNumeroChange,
                        label = { Text("Número (ej: Art. 10)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = estado.nuevoExtracto,
                        onValueChange = viewModel::onNuevoExtractoChange,
                        label = { Text("Extracto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estado.nuevoContenido,
                        onValueChange = viewModel::onNuevoContenidoChange,
                        label = { Text("Contenido completo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = viewModel::crearArticulo,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar Artículo en Xano")
                    }
                }
            }

            // =========================================================
            // SECCIÓN 3: MODIFICAR ARTÍCULO (PATCH articulo/{id})
            // =========================================================
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3. Modificar Artículo (PATCH)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryNavy)
                    Text("Toca el lápiz de un artículo para cargarlo y editarlo:", fontSize = 13.sp, color = Color.Gray)

                    estado.articulos.forEach { art ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "#${art.xanoId ?: "?"} | ${art.normaTitulo} - ${art.numero}",
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.seleccionarArticuloParaEditar(art) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar artículo", tint = PrimaryNavy)
                            }
                        }
                        HorizontalDivider()
                    }

                    if (estado.articuloEditandoXanoId != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Editando registro Xano #${estado.articuloEditandoXanoId}",
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryNavy
                        )
                        OutlinedTextField(
                            value = estado.editNormaTitulo,
                            onValueChange = viewModel::onEditNormaChange,
                            label = { Text("Norma") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = estado.editNumero,
                            onValueChange = viewModel::onEditNumeroChange,
                            label = { Text("Número") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = estado.editExtracto,
                            onValueChange = viewModel::onEditExtractoChange,
                            label = { Text("Extracto") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = estado.editContenido,
                            onValueChange = viewModel::onEditContenidoChange,
                            label = { Text("Contenido") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = viewModel::guardarCambiosArticulo,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Actualizar Artículo en Xano")
                        }
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "3. Artículos en Xano (${estado.articulos.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PrimaryNavy
                            )
                            IconButton(onClick = viewModel::consultarArticulos) {
                                Icon(Icons.Default.Refresh, contentDescription = "Recargar artículos", tint = PrimaryNavy)
                            }
                        }

                        if (estado.articulos.isEmpty()) {
                            Text("No hay artículos registrados en Xano.", fontSize = 13.sp, color = Color.Gray)
                        } else {
                            estado.articulos.forEach { art ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "#${art.xanoId ?: "?"} | ${art.normaTitulo} - ${art.numero} (${art.id})",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = PrimaryNavy
                                        )
                                        Text(
                                            text = art.extracto.ifBlank { art.contenido },
                                            fontSize = 13.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                    IconButton(
                                        onClick = { art.xanoId?.let { viewModel.eliminarArticulo(it) } },
                                        enabled = art.xanoId != null
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar artículo",
                                            tint = Color(0xFFC62828)
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}