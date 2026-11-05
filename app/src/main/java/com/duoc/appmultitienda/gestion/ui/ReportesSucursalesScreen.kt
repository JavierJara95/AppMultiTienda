package com.duoc.appmultitienda.gestion.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.appmultitienda.gestion.model.SucursalGestion
import com.duoc.appmultitienda.gestion.viewmodel.ReportesSucursalesViewModel

@Composable
fun ReportesSucursalesEntry(vm: ReportesSucursalesViewModel = viewModel()) {
    val contexto = LocalContext.current
    val sucursales by vm.sucursales.collectAsState()
    val seleccion by vm.seleccion.collectAsState()
    val filas by vm.filas.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    var pestaña by remember { mutableIntStateOf(0) }
    var pendiente by remember { mutableStateOf<SucursalGestion?>(null) }
    val guardarCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri == null) {
            vm.notificarExportacion("Exportación cancelada. No se generó archivo.")
        } else {
            try {
                contexto.contentResolver.openOutputStream(uri)?.use { salida ->
                    salida.write(vm.csvActual().toByteArray(Charsets.UTF_8))
                } ?: error("No se pudo abrir el archivo")
                vm.notificarExportacion("CSV guardado correctamente")
            } catch (e: Exception) {
                vm.notificarExportacion("Error al guardar CSV: ${e.message ?: "desconocido"}")
            }
        }
    }
    Column(Modifier.fillMaxSize().background(GestionColores.fondo).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Administración y reportes", style = MaterialTheme.typography.headlineSmall)
        TabRow(selectedTabIndex = pestaña) {
            Tab(selected = pestaña == 0, onClick = { pestaña = 0 }, text = { Text("Sucursales") })
            Tab(selected = pestaña == 1, onClick = { pestaña = 1 }, text = { Text("Reportes") })
        }
        if (pestaña == 0) {
            Text("Control de sucursales · datos demostrativos", style = MaterialTheme.typography.bodySmall)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sucursales, key = { it.id }) { suc ->
                    GestionTarjeta(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(suc.nombre, style = MaterialTheme.typography.titleMedium)
                            Text("${suc.id} · ${suc.direccion}")
                            Text(if (suc.activa) "Activa" else "Inactiva")
                            OutlinedButton(onClick = { pendiente = suc }) {
                                Text(if (suc.activa) "Deshabilitar" else "Habilitar")
                            }
                        }
                    }
                }
            }
        } else {
            Text("Reporte de movimientos ficticios", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                val opciones = listOf(null to "Todas", "SUC-01" to "Alameda", "SUC-02" to "Norte")
                opciones.forEachIndexed { index, (id, texto) ->
                    SegmentedButton(
                        selected = seleccion == id,
                        onClick = { vm.seleccionar(id) },
                        shape = SegmentedButtonDefaults.itemShape(index, opciones.size)
                    ) { Text(texto) }
                }
            }
            Text("Registros: ${filas.size}")
            filas.forEach { Text("${it.id} · ${it.producto} · ${it.cantidad}") }
            Button(onClick = { guardarCsv.launch("multitienda_movimientos.csv") }) { Text("Exportar CSV") }
        }
        if (mensaje != null) {
            Text(mensaje!!, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = vm::limpiarMensaje) { Text("Cerrar mensaje") }
        }
    }
    pendiente?.let { suc ->
        AlertDialog(
            onDismissRequest = { pendiente = null },
            title = { Text(if (suc.activa) "¿Deshabilitar sucursal?" else "¿Habilitar sucursal?") },
            text = { Text("${suc.nombre}. La simulación conserva su inventario e historial.") },
            confirmButton = {
                TextButton(onClick = { vm.cambiarEstado(suc.id, !suc.activa); pendiente = null }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { pendiente = null }) { Text("Cancelar") } }
        )
    }
}
