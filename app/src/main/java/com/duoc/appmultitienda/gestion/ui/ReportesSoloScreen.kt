package com.duoc.appmultitienda.gestion.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.duoc.appmultitienda.gestion.export.PdfReporteExportador
import com.duoc.appmultitienda.gestion.viewmodel.ReportesSucursalesViewModel

/** Única pantalla habilitada para GERENCIA. No expone controles administrativos. */
@Composable
fun ReportesSoloEntry(vm: ReportesSucursalesViewModel) {
    val contexto = LocalContext.current
    val filas by vm.filas.collectAsState()
    val seleccion by vm.seleccion.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    var pdfPendiente by remember { mutableStateOf<ByteArray?>(null) }
    var csvPendiente by remember { mutableStateOf<ByteArray?>(null) }

    val guardarCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri == null) vm.notificarExportacion("Exportación CSV cancelada")
        else try {
            val bytes = csvPendiente ?: error("No se generó CSV")
            contexto.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("No se pudo abrir destino")
            vm.notificarExportacion("CSV guardado correctamente")
        } catch (e: Exception) { vm.notificarExportacion("Error guardando CSV: ${e.message}") }
        csvPendiente = null
    }
    val guardarPdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri == null) vm.notificarExportacion("Exportación PDF cancelada")
        else try {
            val bytes = pdfPendiente ?: error("No se generó PDF")
            contexto.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("No se pudo abrir destino")
            vm.notificarExportacion("PDF guardado correctamente")
        } catch (e: Exception) { vm.notificarExportacion("Error guardando PDF: ${e.message}") }
        pdfPendiente = null
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Reportes de inventario", style = MaterialTheme.typography.headlineSmall)
        Text("Perfil Gerencia: consulta y exportación exclusivamente", style = MaterialTheme.typography.bodySmall)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            val opciones = listOf(null to "Todas", "SUC-01" to "Alameda", "SUC-02" to "Norte")
            opciones.forEachIndexed { index, (id, nombre) ->
                SegmentedButton(
                    selected = seleccion == id,
                    onClick = { vm.seleccionar(id) },
                    shape = SegmentedButtonDefaults.itemShape(index, opciones.size)
                ) { Text(nombre) }
            }
        }
        Text("Registros: ${filas.size}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                csvPendiente = vm.csvActual().toByteArray(Charsets.UTF_8)
                guardarCsv.launch("multitienda_reporte.csv")
            }) { Text("Descargar CSV") }
            OutlinedButton(onClick = {
                try {
                    pdfPendiente = PdfReporteExportador.generar(filas, seleccion ?: "Todas")
                    guardarPdf.launch("multitienda_reporte.pdf")
                } catch (e: Exception) { vm.notificarExportacion("Error creando PDF: ${e.message}") }
            }) { Text("Descargar PDF") }
        }
        HorizontalDivider()
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(filas, key = { it.id }) { fila ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${fila.id} · ${fila.producto}", style = MaterialTheme.typography.titleSmall)
                        Text("${fila.tipo} · ${fila.cantidad} uds · ${fila.sucursalId}")
                        Text(fila.fechaUtc, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        mensaje?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = vm::limpiarMensaje) { Text("Cerrar aviso") }
        }
    }
}
