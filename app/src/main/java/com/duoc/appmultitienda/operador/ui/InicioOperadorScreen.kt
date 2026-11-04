package com.duoc.appmultitienda.operador.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AzulFondo = Color(0xFF04283C)
private val AzulTarjeta = Color(0xFF10506B)
private val AzulClaro = Color(0xFFE4F3FF)
private val AzulTexto = Color(0xFF09243C)
private val GrisTexto = Color(0xFF55708E)

@Composable
fun InicioOperadorScreen(
    nombreSucursal: String,
    onCambiarUbicacion: () -> Unit,
    onEscanear: () -> Unit,
    onConteo: () -> Unit,
    onEntrada: () -> Unit,
    onSalida: () -> Unit,
    onHistorial: () -> Unit = {},
    onPerfil: () -> Unit = {}
) {

    Scaffold(
        containerColor = AzulFondo,

        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {

                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Inicio"
                        )
                    },
                    label = { Text("Inicio") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onEscanear,
                    icon = {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Escáner"
                        )
                    },
                    label = { Text("Escáner") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onHistorial,
                    icon = {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Historial"
                        )
                    },
                    label = { Text("Historial") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onPerfil,
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Perfil"
                        )
                    },
                    label = { Text("Perfil") }
                )
            }
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),

            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // ENCABEZADO

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF075E66)
                ) {

                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = Color(0xFF48E1CB),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Hola, Operador",
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = nombreSucursal,
                        color = Color(0xFFB8D3E4),
                        fontSize = 13.sp
                    )
                }

                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notificaciones",
                    tint = Color.White
                )
            }

            // INVENTARIO

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),

                colors = CardDefaults.cardColors(
                    containerColor = AzulTarjeta
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF105570),
                                    Color(0xFF142E59)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {

                    Text(
                        text = "Inventario de la tienda",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = "42",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "Productos activos",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(62.dp),
                            color = Color(0xFF537F94)
                        )

                        Spacer(Modifier.width(20.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = "3",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "Alertas de stock",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Datos de demostración · Inventario simulado",
                        color = Color(0xFFB4D0DE),
                        fontSize = 12.sp
                    )
                }
            }

            // OPERACIONES

            Text(
                text = "Registrar operación",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                BotonOperacion(
                    titulo = "Escanear\nproducto",
                    icono = Icons.Default.QrCodeScanner,
                    fondoIcono = AzulClaro,
                    colorIcono = Color(0xFF0085FF),
                    modifier = Modifier.weight(1f),
                    onClick = onEscanear
                )

                BotonOperacion(
                    titulo = "Conteo\nfísico",
                    icono = Icons.Default.Assignment,
                    fondoIcono = Color(0xFFF0E4FF),
                    colorIcono = Color(0xFF9145FF),
                    modifier = Modifier.weight(1f),
                    onClick = onConteo
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                BotonOperacion(
                    titulo = "Entrada\nde stock",
                    icono = Icons.Default.MoveToInbox,
                    fondoIcono = Color(0xFFDEF8EA),
                    colorIcono = Color(0xFF009D67),
                    modifier = Modifier.weight(1f),
                    onClick = onEntrada
                )

                BotonOperacion(
                    titulo = "Salida\nde stock",
                    icono = Icons.Default.Outbox,
                    fondoIcono = Color(0xFFFFE8ED),
                    colorIcono = Color(0xFFED3154),
                    modifier = Modifier.weight(1f),
                    onClick = onSalida
                )
            }

            // CONTEOS PENDIENTES

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = "Tus conteos",
                            fontWeight = FontWeight.Bold,
                            color = AzulTexto,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "Hoy",
                            color = GrisTexto,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "2 pendientes de revisión",
                        color = GrisTexto
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "El encargado resolverá las diferencias.",
                        color = GrisTexto,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Información de demostración",
                        color = GrisTexto,
                        fontSize = 11.sp
                    )
                }
            }

            // CAMBIAR SUCURSAL

            OutlinedButton(
                onClick = onCambiarUbicacion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),

                shape = RoundedCornerShape(16.dp),

                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = AzulTexto
                ),

                border = BorderStroke(
                    1.dp,
                    Color(0xFFDCE5ED)
                )
            ) {

                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Cambiar ubicación",
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "OPR-017 · Turno de mañana",
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFB8D3E4),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun BotonOperacion(
    titulo: String,
    icono: ImageVector,
    fondoIcono: Color,
    colorIcono: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Card(
        modifier = modifier
            .height(96.dp)
            .clickable(onClick = onClick),

        shape = RoundedCornerShape(17.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = fondoIcono
            ) {

                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorIcono,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = titulo,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AzulTexto,
                lineHeight = 16.sp
            )
        }
    }
}