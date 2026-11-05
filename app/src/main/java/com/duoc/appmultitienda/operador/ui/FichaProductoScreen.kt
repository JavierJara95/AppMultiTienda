package com.duoc.appmultitienda.operador.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.duoc.appmultitienda.operador.model.ProductoOperador

private val AzulOscuroFicha = Color(0xFF061D30)
private val AzulBotonFicha = Color(0xFF0C4869)
private val AzulTextoFicha = Color(0xFF14334D)
private val GrisTextoFicha = Color(0xFF55708E)
private val GrisBordeFicha = Color(0xFFDCE5ED)
private val FondoFicha = Color(0xFFF8FAFC)

private val VerdeFicha = Color(0xFF008752)
private val VerdeClaroFicha = Color(0xFFDDF5E9)
private val RojoFicha = Color(0xFFC62828)
private val RojoClaroFicha = Color(0xFFFFE5E9)
private val AmarilloClaroFicha = Color(0xFFFFF2CC)

// Determina el estado del inventario
private fun obtenerEstadoStock(
    stockActual: Int,
    stockMinimo: Int
): String {

    return when {

        stockActual == 0 -> "Quiebre de stock"

        stockActual <= stockMinimo -> "Stock bajo"

        else -> "Disponible"
    }
}

// Pantalla principal
@Composable
fun FichaProductoScreen(
    producto: ProductoOperador,
    nombreSucursal: String,
    onVolver: () -> Unit,
    onConteo: () -> Unit,
    onMovimiento: () -> Unit,
    onHistorial: () -> Unit
) {

    val estadoStock = obtenerEstadoStock(
        producto.stockActual,
        producto.stockMinimo
    )

    val colorEstado = when (estadoStock) {
        "Disponible" -> VerdeFicha
        "Stock bajo" -> Color(0xFF946200)
        else -> RojoFicha
    }

    val fondoEstado = when (estadoStock) {
        "Disponible" -> VerdeClaroFicha
        "Stock bajo" -> AmarilloClaroFicha
        else -> RojoClaroFicha
    }

    Scaffold(
        containerColor = FondoFicha,

        bottomBar = {

            NavigationBar(
                containerColor = Color.White
            ) {

                NavigationBarItem(
                    selected = false,
                    onClick = onVolver,
                    icon = { Text("⌂", fontSize = 23.sp) },
                    label = { Text("Inicio") }
                )

                NavigationBarItem(
                    selected = true,
                    onClick = onVolver,
                    icon = { Text("▥", fontSize = 23.sp) },
                    label = { Text("Escáner") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onHistorial,
                    icon = { Text("◷", fontSize = 23.sp) },
                    label = { Text("Historial") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    enabled = false,
                    icon = { Text("♙", fontSize = 23.sp) },
                    label = { Text("Perfil") }
                )
            }
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // ENCABEZADO

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulOscuroFicha)
                    .padding(20.dp)
            ) {

                TextButton(
                    onClick = onVolver
                ) {

                    Text(
                        text = "‹  Producto",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = nombreSucursal,
                    color = Color(0xFFBCD5E4),
                    fontSize = 14.sp
                )
            }

            // CONTENIDO

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),

                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // TARJETA DEL PRODUCTO

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),

                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),

                    border = BorderStroke(
                        1.dp,
                        GrisBordeFicha
                    )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),

                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFE8F2F7)
                        ) {

                            Box(
                                contentAlignment = Alignment.Center
                            ) {

                                Text(
                                    text = "▣",
                                    fontSize = 30.sp,
                                    color = AzulBotonFicha
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.width(14.dp)
                        )

                        Column {

                            Text(
                                text = producto.nombre,
                                color = AzulOscuroFicha,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = "${producto.codigo} · ${producto.categoria}",
                                color = GrisTextoFicha,
                                fontSize = 13.sp
                            )

                            Text(
                                text = producto.codigoBarras,
                                color = GrisTextoFicha,
                                fontSize = 12.sp
                            )

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            Row(
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {

                                Surface(
                                    color = fondoEstado,
                                    shape = RoundedCornerShape(50)
                                ) {

                                    Text(
                                        text = estadoStock,
                                        color = colorEstado,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 5.dp
                                        )
                                    )
                                }

                                Surface(
                                    color = Color(0xFFEAF0F7),
                                    shape = RoundedCornerShape(50)
                                ) {

                                    Text(
                                        text = "Mínimo: ${producto.stockMinimo}",
                                        color = GrisTextoFicha,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 5.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // STOCK

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),

                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF4F8FC)
                    ),

                    border = BorderStroke(
                        1.dp,
                        GrisBordeFicha
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = "Stock de demostración",
                            color = GrisTextoFicha,
                            fontSize = 14.sp
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "${producto.stockActual}",
                                color = AzulOscuroFicha,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )

                            Text(
                                text = "uds",
                                color = AzulOscuroFicha,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = estadoStock,
                                color = colorEstado,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "Mínimo: ${producto.stockMinimo} uds",
                            color = GrisTextoFicha,
                            fontSize = 13.sp
                        )

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        Text(
                            text = "Saldo simulado de $nombreSucursal",
                            color = GrisTextoFicha,
                            fontSize = 12.sp
                        )
                    }
                }

                // OPERACIONES

                Text(
                    text = "Operaciones",
                    color = AzulOscuroFicha,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onConteo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),

                    shape = RoundedCornerShape(15.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulBotonFicha
                    )
                ) {

                    Text(
                        text = "Realizar conteo físico",
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onMovimiento,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),

                    shape = RoundedCornerShape(15.dp),

                    border = BorderStroke(
                        1.dp,
                        GrisBordeFicha
                    ),

                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = AzulOscuroFicha
                    )
                ) {

                    Text(
                        text = "Registrar entrada o salida",
                        fontWeight = FontWeight.Bold
                    )
                }

                // DATOS DEL PRODUCTO

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),

                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),

                    border = BorderStroke(
                        1.dp,
                        GrisBordeFicha
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            text = "Datos del producto",
                            color = AzulOscuroFicha,
                            fontWeight = FontWeight.Bold
                        )

                        FilaDatoProducto(
                            titulo = "Unidad",
                            valor = "Unidad (ud)"
                        )

                        FilaDatoProducto(
                            titulo = "Ubicación interna",
                            valor = "No especificada"
                        )

                        FilaDatoProducto(
                            titulo = "Stock mínimo",
                            valor = "${producto.stockMinimo} uds"
                        )
                    }
                }

                // REGLA DE NEGOCIO

                Text(
                    text = "El stock es independiente por sucursal. " +
                            "Los conteos requieren aprobación para generar un ajuste.",
                    color = GrisTextoFicha,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                OutlinedButton(
                    onClick = onHistorial,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),

                    shape = RoundedCornerShape(15.dp),

                    border = BorderStroke(
                        1.dp,
                        GrisBordeFicha
                    ),

                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = AzulOscuroFicha
                    )
                ) {

                    Text(
                        text = "Ver historial del producto",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// COMPONENTE REUTILIZABLE

@Composable
private fun FilaDatoProducto(
    titulo: String,
    valor: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = titulo,
            color = GrisTextoFicha,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp
        )

        Text(
            text = valor,
            color = AzulTextoFicha,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}