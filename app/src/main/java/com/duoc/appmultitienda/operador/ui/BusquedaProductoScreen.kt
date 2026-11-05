package com.duoc.appmultitienda.operador.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duoc.appmultitienda.operador.model.ProductoOperador
import com.duoc.appmultitienda.operador.viewmodel.BusquedaProductoViewModel

private val AzulOscuroBusqueda = Color(0xFF061D30)
private val AzulBotonBusqueda = Color(0xFF0C4869)
private val GrisBusqueda = Color(0xFF55708E)
private val FondoBusqueda = Color(0xFFF8FAFC)

@Composable
fun BusquedaProductoScreen(
    sucursalId: String,
    nombreSucursal: String,
    viewModel: BusquedaProductoViewModel,
    onVolver: () -> Unit,
    onProductoSeleccionado: (ProductoOperador) -> Unit
) {


    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoBusqueda)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulOscuroBusqueda)
                .padding(20.dp)
        ) {

            TextButton(
                onClick = onVolver
            ) {
                Text(
                    text = "‹ Volver",
                    color = Color.White
                )
            }

            Text(
                text = "Buscar producto",
                fontSize = 26.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = nombreSucursal,
                color = Color(0xFFBCD5E4)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Consulta de inventario",
                color = AzulOscuroBusqueda,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Busca por código interno, código de barras o nombre del producto.",
                color = GrisBusqueda
            )

            OutlinedTextField(
                value = estado.consulta,
                onValueChange = viewModel::actualizarConsulta,
                label = {
                    Text("Código o nombre")
                },
                placeholder = {
                    Text("Ej.: CAF-001")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = estado.error != null,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        viewModel.buscar(sucursalId)
                    }
                )
            )

            if (estado.error != null) {

                Text(
                    text = estado.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    viewModel.buscar(sucursalId)
                },
                enabled = !estado.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulBotonBusqueda
                )
            ) {

                Text(
                    text = "Buscar producto",
                    fontWeight = FontWeight.Bold
                )
            }

            if (estado.cargando) {

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (
                estado.busquedaRealizada &&
                estado.productos.isEmpty()
            ) {

                Text(
                    text = "No encontramos productos con ese código o nombre.",
                    color = GrisBusqueda,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            estado.productos.forEach { producto ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onProductoSeleccionado(producto)
                        },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(
                        1.dp,
                        Color(0xFFDCE5ED)
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = producto.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AzulOscuroBusqueda
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "${producto.codigo} · ${producto.categoria}",
                            color = GrisBusqueda
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Stock: ${producto.stockActual} uds",
                            color = AzulBotonBusqueda,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Mínimo: ${producto.stockMinimo} uds",
                            color = GrisBusqueda
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Ver ficha del producto →",
                            color = AzulBotonBusqueda,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider()

            Text(
                text = "Consultar un producto no modifica el stock.",
                color = GrisBusqueda,
                fontSize = 13.sp
            )

            Text(
                text = "Datos de demostración. Los saldos oficiales se consultarán mediante API REST.",
                color = GrisBusqueda,
                fontSize = 12.sp
            )
        }
    }
}