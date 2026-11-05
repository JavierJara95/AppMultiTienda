package com.duoc.appmultitienda.operador.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.duoc.appmultitienda.core.ui.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun ScannerScreen(buscando: Boolean, onCode: (String) -> Unit, manual: (Boolean) -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var denied by rememberSaveable { mutableStateOf(false) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            granted = it
            denied = !it
        }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME)
                granted =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    if (!granted) {
        Pagina {
            AvisoBox(
                if (denied) "Sin acceso a la cámara" else "Escaneo con cámara",
                if (denied)
                    "Puedes ingresar el código manualmente o habilitar el permiso en Android."
                else "Permite usar la cámara para identificar códigos de barras de prueba.",
                if (denied) "aviso" else "info",
            )
            Principal("Permitir cámara", icon = Icons.Default.CameraAlt) {
                launcher.launch(Manifest.permission.CAMERA)
            }
            Secundario("Ingresar código manualmente", icon = Icons.Default.Keyboard) {
                manual(denied)
            }
            Text(
                "Consultar un producto no cambia el stock.",
                color = Muted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        return
    }
    var error by remember { mutableStateOf<String?>(null) }
    var torch by rememberSaveable { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    LaunchedEffect(torch, camera) { camera?.cameraControl?.enableTorch(torch) }
    Box(Modifier.fillMaxSize().background(Marino)) {
        CamaraPreview(
            enabled = !buscando,
            onCode = onCode,
            onCamera = { camera = it },
            onError = { error = it },
        )
        Canvas(Modifier.fillMaxSize().padding(horizontal = 36.dp, vertical = 150.dp)) {
            val l = 28.dp.toPx()
            val stroke = 4.dp.toPx()
            for ((x, y) in
                listOf(0f to 0f, size.width to 0f, 0f to size.height, size.width to size.height)) {
                drawLine(
                    Celeste,
                    Offset(x, y),
                    Offset(if (x == 0f) l else x - l, y),
                    strokeWidth = stroke,
                )
                drawLine(
                    Celeste,
                    Offset(x, y),
                    Offset(x, if (y == 0f) l else y - l),
                    strokeWidth = stroke,
                )
            }
            drawLine(
                Celeste.copy(alpha = .7f),
                Offset(0f, size.height / 2),
                Offset(size.width, size.height / 2),
                strokeWidth = 1.dp.toPx(),
            )
        }
        IconButton(
            onClick = { torch = !torch },
            modifier =
                Modifier.align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(
                        Marino.copy(alpha = .8f),
                        androidx.compose.foundation.shape.CircleShape,
                    ),
        ) {
            Icon(
                if (torch) Icons.Default.FlashOff else Icons.Default.FlashOn,
                if (torch) "Apagar linterna" else "Encender linterna",
                tint = Color.White,
            )
        }
        Column(
            Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Marino.copy(alpha = .86f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (error != null) AvisoBox("Cámara no disponible", error.orEmpty(), "error")
            Text(
                if (buscando) "Consultando producto…"
                else "Alinea el código de barras dentro del recuadro",
                color = Color.White,
            )
            if (buscando) LinearProgressIndicator(Modifier.fillMaxWidth())
            Secundario(
                "Ingresar código manualmente",
                enabled = !buscando,
                icon = Icons.Default.Keyboard,
            ) {
                manual(false)
            }
            Text(
                "Códigos de barras y referencias de prueba\nConsultar un producto no cambia el stock.",
                color = Color(0xFFD3E8F5),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun CamaraPreview(
    enabled: Boolean,
    onCode: (String) -> Unit,
    onCamera: (Camera) -> Unit,
    onError: (String) -> Unit,
) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val currentCode by rememberUpdatedState(onCode)
    val currentEnabled by rememberUpdatedState(enabled)
    val currentError by rememberUpdatedState(onError)
    val currentCamera by rememberUpdatedState(onCamera)
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
    DisposableEffect(owner, previewView) {
        val executor = Executors.newSingleThreadExecutor()
        val scanner = BarcodeScanning.getClient()
        val alive = AtomicBoolean(true)
        val accepted = AtomicBoolean(false)
        var provider: ProcessCameraProvider? = null
        val preview =
            Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
        val analysis =
            ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
        analysis.setAnalyzer(executor) { proxy ->
            val media = proxy.image
            if (!alive.get() || !currentEnabled || accepted.get() || media == null) proxy.close()
            else {
                val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
                try {
                    scanner
                        .process(image)
                        .addOnSuccessListener { results ->
                            if (alive.get() && currentEnabled)
                                results
                                    .firstOrNull { !it.rawValue.isNullOrBlank() }
                                    ?.rawValue
                                    ?.let { value ->
                                        if (accepted.compareAndSet(false, true)) currentCode(value)
                                    }
                        }
                        .addOnFailureListener {
                            if (alive.get())
                                currentError(
                                    "No se pudo analizar el código. Usa el ingreso manual."
                                )
                        }
                        .addOnCompleteListener { proxy.close() }
                } catch (_: Exception) {
                    proxy.close()
                }
            }
        }
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                if (alive.get())
                    try {
                        provider = future.get()
                        val camera =
                            provider!!.bindToLifecycle(
                                owner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analysis,
                            )
                        currentCamera(camera)
                    } catch (_: Exception) {
                        if (alive.get())
                            currentError("No se pudo abrir la cámara de este dispositivo.")
                    }
            },
            ContextCompat.getMainExecutor(context),
        )
        onDispose {
            alive.set(false)
            analysis.clearAnalyzer()
            provider?.unbind(preview, analysis)
            scanner.close()
            executor.shutdown()
        }
    }
}
