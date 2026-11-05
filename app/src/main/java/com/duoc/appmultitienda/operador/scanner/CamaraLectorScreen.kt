package com.duoc.appmultitienda.operador.scanner

import android.Manifest
import android.content.pm.PackageManager

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

import java.util.concurrent.Executors


// COLORES MULTITIENDA

private val AzulCamara = Color(0xFF061D30)
private val AzulBotonCamara = Color(0xFF0C4869)
private val GrisCamara = Color(0xFF55708E)
private val FondoCamara = Color(0xFFF8FAFC)
private val VerdeCamara = Color(0xFF008752)


// PANTALLA DE CÁMARA CON RECONOCIMIENTO

@Composable
fun CamaraLectorScreen(

    nombreSucursal: String,

    onVolver: () -> Unit,

    onCodigoEscaneado: (String) -> Unit

)  {

    // CONTEXTO DE ANDROID

    val contexto = LocalContext.current

    // CICLO DE VIDA

    val lifecycleOwner = LocalLifecycleOwner.current


    // COMPROBAR EL PERMISO DE CÁMARA

    val tienePermiso = ContextCompat.checkSelfPermission(

        contexto,

        Manifest.permission.CAMERA

    ) == PackageManager.PERMISSION_GRANTED


    // ESTADOS DE LA PANTALLA

    var errorCamara by remember {
        mutableStateOf<String?>(null)
    }

    var codigoDetectado by remember {
        mutableStateOf<String?>(null)
    }


    // COMPONENTE QUE MOSTRARÁ LA CÁMARA

    val previewView = remember(contexto) {

        PreviewView(contexto).apply {

            implementationMode =
                PreviewView.ImplementationMode.COMPATIBLE

            scaleType =
                PreviewView.ScaleType.FILL_CENTER
        }
    }


    // CONECTAR CAMERAX CON ML KIT

    DisposableEffect(
        contexto,
        lifecycleOwner,
        previewView,
        tienePermiso
    ) {

        var activo = true

        var proveedorCamara: ProcessCameraProvider? = null

        var vistaPrevia: Preview? = null

        var analisisImagenes: ImageAnalysis? = null

        var analizador: CodigoBarrasAnalyzer? = null


        // EJECUTOR PARA ANALIZAR IMÁGENES

        val executorAnalisis =
            Executors.newSingleThreadExecutor()


        if (tienePermiso) {

            val futuroProveedor =
                ProcessCameraProvider.getInstance(contexto)


            futuroProveedor.addListener(

                {

                    if (activo) {

                        try {

                            // 1. OBTENER EL PROVEEDOR

                            val proveedor =
                                futuroProveedor.get()


                            // 2. CREAR LA VISTA PREVIA

                            val preview = Preview.Builder()
                                .build()

                            preview.setSurfaceProvider(
                                previewView.surfaceProvider
                            )


                            // 3. CONFIGURAR EL ANÁLISIS

                            val imageAnalysis =
                                ImageAnalysis.Builder()

                                    .setBackpressureStrategy(
                                        ImageAnalysis
                                            .STRATEGY_KEEP_ONLY_LATEST
                                    )

                                    .build()


                            // 4. CREAR EL ANALIZADOR ML KIT

                            val barcodeAnalyzer =
                                CodigoBarrasAnalyzer(

                                    mainExecutor =
                                        ContextCompat.getMainExecutor(
                                            contexto
                                        ),

                                    onCodigoDetectado = { codigo ->

                                        if (activo) {

                                            codigoDetectado = codigo
                                        }
                                    },

                                    onError = { mensaje ->

                                        if (activo) {

                                            errorCamara = mensaje
                                        }
                                    }
                                )


                            // 5. CONECTAR EL ANALIZADOR

                            imageAnalysis.setAnalyzer(

                                executorAnalisis,

                                barcodeAnalyzer
                            )


                            // 6. SELECCIONAR LA CÁMARA TRASERA

                            val selector =
                                CameraSelector.DEFAULT_BACK_CAMERA


                            // 7. CONECTAR LA CÁMARA

                            proveedor.bindToLifecycle(

                                lifecycleOwner,

                                selector,

                                preview,

                                imageAnalysis
                            )


                            // 8. GUARDAR REFERENCIAS

                            proveedorCamara = proveedor

                            vistaPrevia = preview

                            analisisImagenes = imageAnalysis

                            analizador = barcodeAnalyzer

                            errorCamara = null


                        } catch (e: Exception) {

                            errorCamara =
                                "No fue posible iniciar el lector: " +
                                        (e.message ?: "Error desconocido.")

                        }
                    }
                },

                ContextCompat.getMainExecutor(contexto)
            )

        } else {

            errorCamara =
                "MultiTienda no tiene permiso de cámara."
        }


        // LIBERAR RECURSOS AL SALIR

        onDispose {

            activo = false

            analisisImagenes?.clearAnalyzer()

            analizador?.close()

            val preview = vistaPrevia

            val analysis = analisisImagenes

            val proveedor = proveedorCamara


            if (proveedor != null) {

                val casosUso = listOfNotNull(
                    preview,
                    analysis
                )

                if (casosUso.isNotEmpty()) {

                    proveedor.unbind(
                        *casosUso.toTypedArray()
                    )
                }
            }

            executorAnalisis.shutdown()
        }
    }


    // INTERFAZ DE MULTITIENDA

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCamara)
    ) {

        // ENCABEZADO

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulCamara)
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
                text = "Escáner de productos",
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
                .padding(16.dp),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Apunta al código de barras",
                color = AzulCamara,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )


            Text(
                text = "Coloca el código dentro del recuadro.",

                color = GrisCamara,

                fontSize = 14.sp,

                textAlign = TextAlign.Center
            )


            // VISTA DE CÁMARA

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),

                shape = RoundedCornerShape(20.dp),

                colors = CardDefaults.cardColors(
                    containerColor = Color.Black
                ),

                border = BorderStroke(
                    2.dp,
                    AzulBotonCamara
                )
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),

                    contentAlignment = Alignment.Center
                ) {

                    if (tienePermiso) {

                        AndroidView(

                            factory = {
                                previewView
                            },

                            modifier = Modifier.fillMaxSize()
                        )


                        // GUÍA VISUAL DEL CÓDIGO

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .height(150.dp),

                            color = Color.Transparent,

                            border = BorderStroke(
                                3.dp,
                                Color(0xFF39D5B5)
                            ),

                            shape = RoundedCornerShape(14.dp)
                        ) {}

                    } else {

                        Text(
                            text = "Cámara no autorizada",
                            color = Color.White
                        )
                    }


                    // MOSTRAR ERRORES

                    errorCamara?.let { mensaje ->

                        Text(
                            text = mensaje,

                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color(0xCC000000))
                                .padding(12.dp),

                            color = Color.White,

                            textAlign = TextAlign.Center,

                            fontSize = 12.sp
                        )
                    }
                }
            }


            // RESULTADO DEL ESCANEO

            codigoDetectado?.let { codigo ->

                Card(
                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(16.dp),

                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFDDF5E9)
                    )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            text = "Código detectado correctamente",
                            color = VerdeCamara,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = codigo,
                            color = AzulCamara,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Código reconocido. Presiona " +
                                    "Buscar producto escaneado para " +
                                    "consultar su inventario.",

                            color = GrisCamara,
                            fontSize = 12.sp
                        )
                    }
                }

            } ?: run {

                Text(
                    text = "Esperando un código de barras...",

                    color = GrisCamara,

                    fontSize = 13.sp,

                    textAlign = TextAlign.Center
                )
            }
// BOTÓN PARA BUSCAR EL PRODUCTO ESCANEADO

            codigoDetectado?.let { codigo ->

                Button(

                    onClick = {

                        onCodigoEscaneado(codigo)
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),

                    shape = RoundedCornerShape(16.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdeCamara
                    )
                ) {

                    Text(
                        text = "Buscar producto escaneado",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // BOTÓN VOLVER

            Button(
                onClick = onVolver,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulBotonCamara
                ),

                shape = RoundedCornerShape(16.dp)
            ) {

                Text(
                    text = "Volver al escáner",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}