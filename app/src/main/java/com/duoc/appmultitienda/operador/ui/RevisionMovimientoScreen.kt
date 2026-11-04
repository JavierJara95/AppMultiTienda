package com.duoc.appmultitienda.operador.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.duoc.appmultitienda.operador.model.ProductoOperador
import com.duoc.appmultitienda.operador.validation.TipoMovimiento
import com.duoc.appmultitienda.operador.viewmodel.MovimientoViewModel

@Composable
fun RevisionMovimientoScreen(
    producto: ProductoOperador,
    nombreSucursal: String,
    viewModel: MovimientoViewModel,
    onVolverEditar: () -> Unit,
    onComprobante: () -> Unit
) {

    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    val stockPrevisto = viewModel.calcularStockPrevisto(producto)

    val tipoTexto = when (estado.tipo) {
        TipoMovimiento.ENTRADA -> "Entrada"
        TipoMovimiento.SALIDA -> "Salida"
    }

    LaunchedEffect(estado.comprobanteDemo) {
        if (estado.comprobanteDemo != null) {
            onComprobante()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF061D30))
                .padding(20.dp)
        ) {

            Text(
                text = "Revisar movimiento",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = nombreSucursal,
                color = Color(0xFFBCD5E4))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "Resumen de la operación",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    DatoRevision("Producto", producto.nombre)

                    DatoRevision("Código", producto.codigo)

                    DatoRevision("Tipo", tipoTexto)

                    DatoRevision("Cantidad", "${estado.cantidad} uds")

                    DatoRevision("Motivo", estado.motivo)

                    DatoRevision(
                        "Referencia",
                        estado.referencia.ifBlank { "Sin referencia" }
                    )

                    HorizontalDivider()

                    DatoRevision(
                        "Stock actual (demostración)",
                        "${producto.stockActual} uds"
                    )

                    DatoRevision(
                        "Stock previsto",
                        stockPrevisto?.let { "$it uds" } ?: "No disponible"
                    )
                }
            }

            Text(
                text = "Esta operación es una simulación. " +
                        "No modificará el inventario oficial.",
                color = Color(0xFF55708E),
                fontSize = 13.sp
            )

            if (estado.confirmandoDemo) {

                Row {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text("Procesando simulación...")
                }
            }

            estado.errorConfirmacion?.let { mensaje ->

                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    viewModel.confirmarSimulacion(producto)
                },
                enabled = estado.revisando &&
                        stockPrevisto != null &&
                        !estado.confirmandoDemo &&
                        estado.comprobanteDemo == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0C4869)
                )
            ) {

                Text("Confirmar simulación")
            }

            OutlinedButton(
                onClick = onVolverEditar,
                enabled = !estado.confirmandoDemo &&
                        estado.comprobanteDemo == null,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Volver y corregir")
            }
        }
    }
}

@Composable
private fun DatoRevision(
    titulo: String,
    valor: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = titulo,
            color = Color(0xFF55708E),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = valor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}