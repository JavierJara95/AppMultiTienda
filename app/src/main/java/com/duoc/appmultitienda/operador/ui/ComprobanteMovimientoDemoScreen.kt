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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.duoc.appmultitienda.operador.model.ComprobanteMovimientoDemo
import com.duoc.appmultitienda.operador.validation.TipoMovimiento


// COLORES DE MULTITIENDA

private val AzulComprobante = Color(0xFF061D30)
private val AzulBotonComprobante = Color(0xFF0C4869)
private val GrisComprobante = Color(0xFF55708E)
private val FondoComprobante = Color(0xFFF8FAFC)
private val VerdeComprobante = Color(0xFF008752)


// PANTALLA PRINCIPAL DEL COMPROBANTE

@Composable
fun ComprobanteMovimientoDemoScreen(

    comprobante: ComprobanteMovimientoDemo,

    nombreSucursal: String,

    onVolverInicio: () -> Unit

) {

    // Determinar si es entrada o salida

    val esEntrada =
        comprobante.tipo == TipoMovimiento.ENTRADA

    val tipoTexto =
        if (esEntrada) "ENTRADA" else "SALIDA"

    val cantidadTexto =
        if (esEntrada) {
            "+${comprobante.cantidad}"
        } else {
            "-${comprobante.cantidad}"
        }

    // CONTENEDOR PRINCIPAL

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoComprobante)
    ) {

        // ENCABEZADO

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulComprobante)
                .padding(20.dp)
        ) {

            Text(
                text = "Comprobante de movimiento",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = nombreSucursal,
                color = Color(0xFFBCD5E4),
                fontSize = 14.sp
            )
        }

        // CONTENIDO CON DESPLAZAMIENTO

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // TARJETA DEL COMPROBANTE

            Card(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(20.dp),

                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),

                border = BorderStroke(
                    1.dp,
                    Color(0xFFDCE5ED)
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // INDICADOR DE DEMOSTRACIÓN

                    Surface(
                        color = Color(0xFFDDF5E9),
                        shape = RoundedCornerShape(50)
                    ) {

                        Text(
                            text = "SIMULACIÓN COMPLETADA",
                            color = VerdeComprobante,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,

                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 8.dp
                            )
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Text(
                        text = "MULTITIENDA",
                        color = AzulComprobante,
                        fontWeight = FontWeight.Bold,
                        fontSize = 23.sp
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Comprobante de demostración",
                        color = GrisComprobante,
                        fontSize = 13.sp
                    )

                    Spacer(
                        modifier = Modifier.height(22.dp)
                    )

                    HorizontalDivider()

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    // IDENTIFICADOR

                    Text(
                        text = "N.º de comprobante",
                        color = GrisComprobante,
                        fontSize = 13.sp
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = comprobante.idDemostracion,
                        color = AzulComprobante,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(22.dp)
                    )

                    // DATOS DEL PRODUCTO

                    DatoComprobante(
                        titulo = "Sucursal",
                        valor = nombreSucursal
                    )

                    DatoComprobante(
                        titulo = "Producto",
                        valor = comprobante.nombreProducto
                    )

                    DatoComprobante(
                        titulo = "Código",
                        valor = comprobante.codigoProducto
                    )

                    HorizontalDivider()

                    // DATOS DEL MOVIMIENTO

                    DatoComprobante(
                        titulo = "Tipo de movimiento",
                        valor = tipoTexto
                    )

                    DatoComprobante(
                        titulo = "Cantidad",
                        valor = "$cantidadTexto uds"
                    )

                    DatoComprobante(
                        titulo = "Stock anterior",
                        valor = "${comprobante.stockAnterior} uds"
                    )

                    DatoComprobante(
                        titulo = "Stock previsto",
                        valor = "${comprobante.stockPrevisto} uds"
                    )

                    HorizontalDivider()

                    // INFORMACIÓN ADICIONAL

                    DatoComprobante(
                        titulo = "Motivo",
                        valor = comprobante.motivo
                    )

                    DatoComprobante(
                        titulo = "Referencia",
                        valor = comprobante.referencia.ifBlank {
                            "Sin referencia"
                        }
                    )

                    DatoComprobante(
                        titulo = "Operador",
                        valor = comprobante.operadorId
                    )

                    DatoComprobante(
                        titulo = "Fecha UTC",
                        valor = comprobante.fechaUtc
                    )
                }
            }

            // AVISO IMPORTANTE

            Card(
                modifier = Modifier.fillMaxWidth(),

                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF2CC)
                ),

                shape = RoundedCornerShape(16.dp)
            ) {

                Text(
                    text = "Este comprobante es una simulación. " +
                            "No representa una operación oficial y " +
                            "no modifica el inventario de la tienda.",

                    modifier = Modifier.padding(16.dp),

                    color = Color(0xFF795600),

                    fontSize = 13.sp,

                    lineHeight = 19.sp,

                    textAlign = TextAlign.Start
                )
            }

            // BOTÓN PARA VOLVER A INICIO

            Button(
                onClick = onVolverInicio,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),

                shape = RoundedCornerShape(15.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulBotonComprobante
                )
            ) {

                Text(
                    text = "Volver a Inicio",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }
    }
}


// COMPONENTE PARA MOSTRAR CADA DATO

@Composable
private fun DatoComprobante(

    titulo: String,

    valor: String

) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),

        verticalAlignment = Alignment.Top
    ) {

        Text(
            text = titulo,
            color = GrisComprobante,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Text(
            text = valor,
            color = AzulComprobante,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}