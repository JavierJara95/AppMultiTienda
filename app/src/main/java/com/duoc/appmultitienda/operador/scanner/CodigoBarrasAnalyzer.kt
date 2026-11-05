package com.duoc.appmultitienda.operador.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning

import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean


// Analiza imágenes de CameraX para reconocer códigos de barras.

class CodigoBarrasAnalyzer(

    private val mainExecutor: Executor,

    private val onCodigoDetectado: (String) -> Unit,

    private val onError: (String) -> Unit = {}

) : ImageAnalysis.Analyzer, AutoCloseable {


    // Formatos de códigos que queremos reconocer.

    private val opciones = BarcodeScannerOptions.Builder()

        .setBarcodeFormats(

            Barcode.FORMAT_EAN_13,

            Barcode.FORMAT_EAN_8,

            Barcode.FORMAT_UPC_A,

            Barcode.FORMAT_UPC_E,

            Barcode.FORMAT_CODE_128,

            Barcode.FORMAT_CODE_39,

            Barcode.FORMAT_QR_CODE
        )

        .build()


    // Crear el lector de ML Kit.

    private val scanner: BarcodeScanner =
        BarcodeScanning.getClient(opciones)


    // Evitar analizar varias imágenes simultáneamente.

    private val procesando = AtomicBoolean(false)


    // Evitar entregar repetidamente el mismo resultado.

    private val codigoEntregado = AtomicBoolean(false)


    // Identificar cuando el analizador deja de utilizarse.

    private val cerrado = AtomicBoolean(false)


    // ANALIZAR UNA IMAGEN DE LA CÁMARA

    override fun analyze(imageProxy: ImageProxy) {

        // Si el analizador está cerrado, liberamos la imagen.

        if (cerrado.get()) {

            imageProxy.close()

            return
        }


        // Si ya encontramos un código, no continuamos.

        if (codigoEntregado.get()) {

            imageProxy.close()

            return
        }


        // Evitar analizar otra imagen mientras estamos ocupados.

        if (!procesando.compareAndSet(false, true)) {

            imageProxy.close()

            return
        }


        // Obtener la imagen enviada por CameraX.

        val imagenCamara = imageProxy.image


        // Comprobar que exista una imagen válida.

        if (imagenCamara == null) {

            procesando.set(false)

            imageProxy.close()

            return
        }


        try {

            // Crear la imagen que necesita ML Kit.

            val imagen = InputImage.fromMediaImage(

                imagenCamara,

                imageProxy.imageInfo.rotationDegrees
            )


            // Analizar la imagen.

            scanner.process(imagen)

                .addOnSuccessListener(mainExecutor) { codigos ->

                    if (!cerrado.get() && !codigoEntregado.get()) {

                        // Buscar el primer código que tenga un valor.

                        val codigoEncontrado = codigos

                            .mapNotNull { codigo ->

                                codigo.rawValue
                            }

                            .firstOrNull { valor ->

                                valor.isNotBlank()
                            }


                        // Entregar el código reconocido.

                        if (codigoEncontrado != null) {

                            if (
                                codigoEntregado.compareAndSet(
                                    false,
                                    true
                                )
                            ) {

                                onCodigoDetectado(
                                    codigoEncontrado
                                )
                            }
                        }
                    }
                }

                .addOnFailureListener(mainExecutor) { error ->

                    if (!cerrado.get()) {

                        onError(
                            error.message
                                ?: "No se pudo analizar el código."
                        )
                    }
                }

                .addOnCompleteListener(mainExecutor) {

                    // Liberar la imagen cuando termine el análisis.

                    imageProxy.close()

                    procesando.set(false)

                    // Si el analizador fue cerrado durante
                    // la operación, liberar también ML Kit.

                    if (cerrado.get()) {

                        scanner.close()
                    }
                }

        } catch (e: Exception) {

            imageProxy.close()

            procesando.set(false)

            if (!cerrado.get()) {

                onError(
                    e.message
                        ?: "Error al procesar la imagen."
                )
            }
        }
    }


    // LIBERAR EL ANALIZADOR

    override fun close() {

        if (cerrado.compareAndSet(false, true)) {

            // Si no estamos procesando imágenes,
            // podemos liberar inmediatamente el lector.

            if (!procesando.get()) {

                scanner.close()
            }
        }
    }
}