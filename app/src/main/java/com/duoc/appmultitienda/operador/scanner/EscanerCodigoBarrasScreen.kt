package com.duoc.appmultitienda.operador.scanner

import android.Manifest
import android.content.pm.PackageManager

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.core.content.ContextCompat


// COLORES DE MULTITIENDA

private val AzulEscaner = Color(0xFF061D30)
private val AzulBotonEscaner = Color(0xFF0C4869)
private val GrisEscaner = Color(0xFF55708E)
private val FondoEscaner = Color(0xFFF8FAFC)


// PANTALLA DEL ESCÁNER

@Composable
fun EscanerCodigoBarrasScreen(

    nombreSucursal: String,

    onVolver: () -> Unit,

    onAbrirCamara: () -> Unit,

    onBusquedaManual: () -> Unit

){

    // Obtener acceso al contexto de Android.
    val contexto = LocalContext.current

    // Comprobar si ya tenemos permiso de cámara.
    fun tienePermisoCamara(): Boolean {

        return ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Guardar si tenemos permiso.
    var permisoConcedido by remember {

        mutableStateOf(
            tienePermisoCamara()
        )
    }

    // Guardar si el usuario rechazó una solicitud.
    var permisoRechazado by remember {

        mutableStateOf(false)
    }

    // Preparar la solicitud de permiso.
    val solicitarPermiso = rememberLauncherForActivityResult(

        contract = ActivityResultContracts.RequestPermission()

    ) { autorizado ->

        permisoConcedido = autorizado

        permisoRechazado = !autorizado

        if (autorizado) {

            onAbrirCamara()
        }
    }

    // INTERFAZ

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoEscaner)
    ) {

        // ENCABEZADO

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulEscaner)
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
                text = "Escanear producto",
                color = Color.White,
                fontSize = 24.sp,
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

        // CONTENIDO

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.Center
        ) {

            // TARJETA PRINCIPAL

            Card(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(20.dp),

                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),

                    horizontalAlignment = Alignment.CenterHorizontally,

                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text = "▥",
                        fontSize = 56.sp,
                        color = AzulBotonEscaner
                    )

                    Text(
                        text = "Lector de códigos de barras",
                        color = AzulEscaner,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Utilizaremos la cámara del teléfono " +
                                "para reconocer el código de barras " +
                                "de un producto.",

                        color = GrisEscaner,

                        textAlign = TextAlign.Center,

                        fontSize = 14.sp
                    )

                    HorizontalDivider()

                    // ESTADO DEL PERMISO

                    if (permisoConcedido) {

                        Text(
                            text = "Permiso de cámara autorizado",
                            color = Color(0xFF008752),
                            fontWeight = FontWeight.Bold
                        )

                    } else {

                        Text(
                            text = "Necesitamos permiso para " +
                                    "utilizar la cámara.",

                            color = GrisEscaner,

                            textAlign = TextAlign.Center
                        )
                    }

                    // MENSAJE DE RECHAZO

                    if (permisoRechazado) {

                        Text(
                            text = "No autorizaste la cámara. " +
                                    "Puedes volver a intentarlo " +
                                    "o utilizar la búsqueda manual.",

                            color = MaterialTheme.colorScheme.error,

                            textAlign = TextAlign.Center,

                            fontSize = 13.sp
                        )
                    }

                    // BOTÓN PRINCIPAL

                    Button(
                        onClick = {

                            if (tienePermisoCamara()) {

                                permisoConcedido = true

                                permisoRechazado = false

                                onAbrirCamara()

                            } else {

                                solicitarPermiso.launch(
                                    Manifest.permission.CAMERA
                                )
                            }
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),

                        shape = RoundedCornerShape(16.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzulBotonEscaner
                        )
                    ) {

                        Text(
                            text = if (permisoConcedido) {
                                "Continuar al escáner"
                            } else {
                                "Permitir acceso a cámara"
                            },

                            fontWeight = FontWeight.Bold
                        )
                    }


                    // BOTÓN VOLVER
                    OutlinedButton(
                        onClick = onBusquedaManual,

                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(16.dp)
                    ) {

                        Text(
                            text = "Buscar producto manualmente"
                        )
                    }
                    OutlinedButton(
                        onClick = onVolver,

                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(16.dp)
                    ) {

                        Text(
                            text = "Volver a MultiTienda"
                        )
                    }
                }
            }
        }
    }
}