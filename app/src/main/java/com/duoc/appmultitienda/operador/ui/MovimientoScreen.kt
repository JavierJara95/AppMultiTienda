package com.duoc.appmultitienda.operador.ui


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.duoc.appmultitienda.operador.model.ProductoOperador
import com.duoc.appmultitienda.operador.validation.TipoMovimiento
import com.duoc.appmultitienda.operador.viewmodel.MovimientoViewModel

private val AzulFondoMov = Color(0xFF061D30)
private val AzulBotonMov = Color(0xFF0C4869)
private val GrisTextoMov = Color(0xFF55708E)
private val GrisBordeMov = Color(0xFFDCE5ED)
private val FondoMov = Color(0xFFF8FAFC)

@Composable
fun MovimientoScreen(
    producto: ProductoOperador,
    nombreSucursal: String,
    viewModel: MovimientoViewModel,
    onVolver: () -> Unit,
    onAbrirRevision: () -> Unit
) {

    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    val cantidad = estado.cantidad.toIntOrNull()

    val stockPrevisto = viewModel.calcularStockPrevisto(
        producto
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoMov)
    ) {

        // ENCABEZADO

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulFondoMov)
                .padding(20.dp)
        ) {

            TextButton(
                onClick = onVolver
            ) {

                Text(
                    text = "‹  Registrar movimiento",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = nombreSucursal,
                color = Color(0xFFBCD5E4),
                fontSize = 14.sp
            )
        }

        // FORMULARIO

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),

            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // PRODUCTO

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),

                border = BorderStroke(
                    1.dp,
                    GrisBordeMov
                ),

                shape = RoundedCornerShape(18.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {

                    Text(
                        text = producto.nombre,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulFondoMov
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "${producto.codigo} · ${producto.categoria}",
                        color = GrisTextoMov
                    )

                    Text(
                        text = producto.codigoBarras,
                        color = GrisTextoMov,
                        fontSize = 13.sp
                    )
                }
            }

            // STOCK ACTUAL

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Stock de demostración",
                    color = GrisTextoMov,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${producto.stockActual} uds",
                    fontWeight = FontWeight.Bold,
                    color = AzulFondoMov,
                    fontSize = 18.sp
                )
            }

            // TIPO DE MOVIMIENTO

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Button(
                    onClick = {
                        viewModel.seleccionarTipo(
                            TipoMovimiento.ENTRADA
                        )
                    },

                    modifier = Modifier.weight(1f),

                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (estado.tipo == TipoMovimiento.ENTRADA)
                                AzulBotonMov
                            else
                                Color(0xFFE3EAF0),

                        contentColor =
                            if (estado.tipo == TipoMovimiento.ENTRADA)
                                Color.White
                            else
                                AzulFondoMov
                    )
                ) {

                    Text("Entrada")
                }

                Button(
                    onClick = {
                        viewModel.seleccionarTipo(
                            TipoMovimiento.SALIDA
                        )
                    },

                    modifier = Modifier.weight(1f),

                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (estado.tipo == TipoMovimiento.SALIDA)
                                AzulBotonMov
                            else
                                Color(0xFFE3EAF0),

                        contentColor =
                            if (estado.tipo == TipoMovimiento.SALIDA)
                                Color.White
                            else
                                AzulFondoMov
                    )
                ) {

                    Text("Salida")
                }
            }

            // CANTIDAD

            OutlinedTextField(
                value = estado.cantidad,
                onValueChange = viewModel::actualizarCantidad,

                label = {
                    Text(
                        if (estado.tipo == TipoMovimiento.ENTRADA)
                            "Cantidad de entrada"
                        else
                            "Cantidad de salida"
                    )
                },

                placeholder = {
                    Text("Ej.: 6")
                },

                modifier = Modifier.fillMaxWidth(),

                singleLine = true,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),

                isError = estado.errorCantidad != null,

                supportingText = {

                    Text(
                        estado.errorCantidad
                            ?: "Unidades enteras mayores que 0."
                    )
                }
            )

            // MOTIVO

            OutlinedTextField(
                value = estado.motivo,
                onValueChange = viewModel::actualizarMotivo,

                label = {
                    Text("Motivo del movimiento")
                },

                placeholder = {
                    Text("Ej.: Recepción de proveedor")
                },

                modifier = Modifier.fillMaxWidth(),

                singleLine = true,

                isError = estado.errorMotivo != null,

                supportingText = {

                    estado.errorMotivo?.let {

                        Text(it)
                    }
                }
            )

            // REFERENCIA

            OutlinedTextField(
                value = estado.referencia,
                onValueChange = viewModel::actualizarReferencia,

                label = {
                    Text("Referencia (opcional)")
                },

                placeholder = {
                    Text("Ej.: Guía REC-892")
                },

                modifier = Modifier.fillMaxWidth(),

                singleLine = true
            )

            // PREVISUALIZACIÓN

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F2F7)
                ),

                shape = RoundedCornerShape(16.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {

                    Text(
                        text = "Revisa antes de registrar",
                        color = AzulFondoMov,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = if (
                            cantidad != null &&
                            stockPrevisto != null
                        ) {

                            val tipoTexto =
                                if (estado.tipo == TipoMovimiento.ENTRADA)
                                    "Entrada"
                                else
                                    "Salida"

                            "$tipoTexto de $cantidad uds: " +
                                    "saldo previsto " +
                                    "${producto.stockActual} → $stockPrevisto."
                        } else {

                            "Completa los datos válidos para calcular " +
                                    "el saldo previsto."
                        },

                        color = GrisTextoMov
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Todavía no se ha guardado.",
                        color = GrisTextoMov,
                        fontSize = 12.sp
                    )
                }
            }

            // BOTÓN REVISAR

            Button(
                onClick = {
                    viewModel.revisarMovimiento(
                        producto
                    )
                },
                enabled = !estado.confirmandoDemo &&
                        estado.comprobanteDemo == null,


                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulBotonMov
                ),

                shape = RoundedCornerShape(16.dp)
            ) {

                Text(
                    text = "Revisar movimiento",
                    fontWeight = FontWeight.Bold
                )
            }

            // RESULTADO DE LA REVISIÓN

            AnimatedVisibility(
                visible = estado.revisando
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth(),

                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFDDF5E9)
                    ),

                    shape = RoundedCornerShape(16.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),

                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            text = "Movimiento validado",
                            color = Color(0xFF008752),
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Los datos son válidos para continuar " +
                                    "a la revisión.",
                            color = AzulFondoMov
                        )

                        Text(
                            text = "Todavía no se ha modificado el inventario.",
                            color = GrisTextoMov,
                            fontSize = 12.sp
                        )

                        Button(
                            onClick = onAbrirRevision,

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),

                            colors = ButtonDefaults.buttonColors(
                                containerColor = AzulBotonMov
                            ),

                            shape = RoundedCornerShape(14.dp)
                        ) {

                            Text(
                                text = "Continuar a revisión",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}