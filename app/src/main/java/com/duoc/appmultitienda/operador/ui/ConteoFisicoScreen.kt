package com.duoc.appmultitienda.operador.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.duoc.appmultitienda.operador.model.ProductoOperador
import com.duoc.appmultitienda.operador.viewmodel.ConteoFisicoViewModel


// COLORES DE MULTITIENDA

private val AzulConteo = Color(0xFF061D30)
private val AzulBotonConteo = Color(0xFF0C4869)
private val GrisTextoConteo = Color(0xFF55708E)
private val GrisBordeConteo = Color(0xFFDCE5ED)
private val FondoConteo = Color(0xFFF8FAFC)

private val VerdeConteo = Color(0xFF008752)
private val AmarilloConteo = Color(0xFF946200)
private val RojoConteo = Color(0xFFC62828)


// PANTALLA DEL CONTEO FÍSICO

@Composable
fun ConteoFisicoScreen(

    producto: ProductoOperador,

    nombreSucursal: String,

    viewModel: ConteoFisicoViewModel,

    operadorId: String = "OPR-017",

    onVolver: () -> Unit

) {

    // Obtenemos los datos que guarda el ViewModel.

    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val formularioBloqueado =
        estado.enviando || estado.conteoRegistrado != null

    // Identificamos el resultado del conteo.

    val diferencia = estado.diferencia

    val colorDiferencia = when {

        diferencia == null -> GrisTextoConteo

        diferencia == 0 -> VerdeConteo

        diferencia < 0 -> RojoConteo

        else -> AmarilloConteo
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoConteo)
    ) {

        // ENCABEZADO

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulConteo)
                .padding(20.dp)
        ) {

            TextButton(
                onClick = onVolver
            ) {

                Text(
                    text = "‹  Conteo físico",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
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

                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),

                shape = RoundedCornerShape(18.dp),

                border = BorderStroke(
                    1.dp,
                    GrisBordeConteo
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),

                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "Producto seleccionado",
                        color = GrisTextoConteo,
                        fontSize = 13.sp
                    )

                    Text(
                        text = producto.nombre,
                        color = AzulConteo,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${producto.codigo} · ${producto.categoria}",
                        color = GrisTextoConteo,
                        fontSize = 13.sp
                    )
                }
            }

            // STOCK REGISTRADO

            Card(
                modifier = Modifier.fillMaxWidth(),

                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F2F7)
                ),

                shape = RoundedCornerShape(18.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),

                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "Stock registrado (demostración)",
                        color = GrisTextoConteo,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "${producto.stockActual} unidades",
                        color = AzulConteo,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Saldo de $nombreSucursal",
                        color = GrisTextoConteo,
                        fontSize = 12.sp
                    )
                }
            }

            // CANTIDAD CONTADA

            Text(
                text = "Cantidad contada",
                color = AzulConteo,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = estado.cantidadTexto,

                onValueChange = viewModel::actualizarCantidad,
                enabled = !formularioBloqueado,

                label = {
                    Text("Unidades encontradas")
                },

                placeholder = {
                    Text("Ej.: 14")
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
                            ?: "Ingresa la cantidad que encontraste."
                    )
                }
            )

            // OBSERVACIÓN

            Text(
                text = "Observación",
                color = AzulConteo,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = estado.observacion,

                onValueChange = viewModel::actualizarObservacion,
                enabled = !formularioBloqueado,

                label = {
                    Text("Explica lo encontrado")
                },

                placeholder = {
                    Text("Ej.: Faltan dos bolsas")
                },

                modifier = Modifier.fillMaxWidth(),

                minLines = 3,

                maxLines = 5,

                isError = estado.errorObservacion != null,

                supportingText = {

                    Text(
                        estado.errorObservacion
                            ?: "Obligatoria si existe una diferencia."
                    )
                }
            )

            // BOTÓN PARA REVISAR

            Button(
                onClick = {

                    viewModel.revisarConteo(producto)
                },
                enabled = !formularioBloqueado,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),

                shape = RoundedCornerShape(15.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulBotonConteo
                )
            ) {

                Text(
                    text = "Revisar conteo",
                    fontWeight = FontWeight.Bold
                )
            }

            // RESULTADO DEL CONTEO

            if (estado.conteoRevisado && diferencia != null) {

                Card(
                    modifier = Modifier.fillMaxWidth(),

                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),

                    shape = RoundedCornerShape(18.dp),

                    border = BorderStroke(
                        1.dp,
                        GrisBordeConteo
                    )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),

                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            text = "Resultado del conteo",
                            color = AzulConteo,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Stock registrado: ${producto.stockActual}",
                            color = GrisTextoConteo
                        )

                        Text(
                            text = "Cantidad contada: ${estado.cantidadTexto}",
                            color = GrisTextoConteo
                        )

                        Text(
                            text = "Diferencia: ${
                                if (diferencia > 0) "+$diferencia"
                                else diferencia.toString()
                            } unidades",

                            color = colorDiferencia,

                            fontSize = 19.sp,

                            fontWeight = FontWeight.Bold
                        )

                        HorizontalDivider()

                        if (diferencia == 0) {

                            Text(
                                text = "Las cantidades coinciden.",
                                color = VerdeConteo,
                                fontWeight = FontWeight.Bold
                            )

                        } else {

                            Text(
                                text = "Se detectó una diferencia.",
                                color = colorDiferencia,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Este conteo deberá enviarse " +
                                        "al encargado para su revisión.",
                                color = GrisTextoConteo,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = "Todavía no se ha enviado el conteo.",
                            color = GrisTextoConteo,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            // ENVÍO DEL CONTEO A APROBACIÓN

            if (
                estado.conteoRevisado &&
                estado.conteoRegistrado == null
            ) {

                Button(
                    onClick = {

                        viewModel.enviarAprobacion(
                            producto = producto,
                            operadorId = operadorId
                        )
                    },

                    enabled = !estado.enviando,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),

                    shape = RoundedCornerShape(15.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulBotonConteo
                    )
                ) {

                    Text(
                        text = "Enviar a aprobación",
                        fontWeight = FontWeight.Bold
                    )
                }
            }


// INDICADOR DE CARGA

            if (estado.enviando) {

                Row(
                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.Center,

                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Text(
                        text = "Guardando solicitud de demostración...",
                        color = GrisTextoConteo,
                        fontSize = 13.sp
                    )
                }
            }


// MENSAJE DE ERROR

            estado.errorEnvio?.let { mensaje ->

                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }


// RESULTADO DEL REGISTRO

            estado.conteoRegistrado?.let { conteo ->

                Card(
                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(18.dp),

                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFDDF5E9)
                    )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),

                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        Text(
                            text = "Solicitud registrada en demostración",
                            color = VerdeConteo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )

                        Text(
                            text = "N.º ${conteo.id}",
                            color = AzulConteo,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Producto: ${conteo.nombreProducto}",
                            color = GrisTextoConteo
                        )

                        Text(
                            text = "Diferencia: ${conteo.diferencia} unidades",
                            color = GrisTextoConteo
                        )

                        Text(
                            text = "Estado: PENDIENTE DE APROBACIÓN",
                            color = VerdeConteo,
                            fontWeight = FontWeight.Bold
                        )

                        HorizontalDivider()

                        Text(
                            text = "El conteo se guardó temporalmente " +
                                    "en la aplicación. No se ha enviado al " +
                                    "servidor ni al encargado real.",
                            color = GrisTextoConteo,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // REGLA DE NEGOCIO

            Card(
                modifier = Modifier.fillMaxWidth(),

                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF2CC)
                ),

                shape = RoundedCornerShape(16.dp)
            ) {

                Text(
                    text = "El conteo físico no modifica el stock " +
                            "registrado. Las diferencias deben ser " +
                            "revisadas y aprobadas por el encargado.",

                    modifier = Modifier.padding(16.dp),

                    color = Color(0xFF795600),

                    fontSize = 13.sp
                )
            }

            // VOLVER

            OutlinedButton(
                onClick = onVolver,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),

                shape = RoundedCornerShape(15.dp)
            ) {

                Text(
                    text = "Volver al producto",
                    color = AzulConteo
                )
            }
        }
    }
}