package com.duoc.appmultitienda.gestion.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.duoc.appmultitienda.core.model.*
import com.duoc.appmultitienda.core.ui.*
import com.duoc.appmultitienda.gestion.export.*
import com.duoc.appmultitienda.gestion.viewmodel.*

@Composable
fun ReportesScreen(d: Datos, s: GestionUi, vm: GestionViewModel) {
    val f = s.reporte
    val lanzador =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri
            ->
            vm.resultadoUri(uri?.toString())
        }
    LaunchedEffect(s.exportacion, s.documento?.nombre) {
        if (s.exportacion == "ELEGIR") {
            vm.selectorAbierto()
            try {
                s.documento?.let { lanzador.launch(it.nombre) }
            } catch (_: Exception) {
                vm.falloSelector(
                    "No hay un gestor de documentos disponible. Instala o habilita uno para guardar CSV."
                )
            }
        }
    }
    val busy = s.exportacion in listOf("PREPARANDO", "ELEGIR", "SELECTOR", "GUARDANDO")
    Pagina {
        Segmentos(
            listOf(
                "Movimientos" to "Movimientos",
                "Stock" to "Stock",
                "Diferencias" to "Diferencias",
            ),
            f.tipo,
            enabled = !busy,
        ) {
            vm.reporte(f.copy(tipo = it))
        }
        Selector(
            "Ubicación del reporte",
            f.tiendaId,
            listOf("TODAS" to "Todas las sucursales") +
                d.snapshot.sucursales
                    .filter { it.permitida }
                    .map { it.id to (it.nombre + if (!it.activa) " · Inactiva" else "") },
            enabled = !busy,
        ) {
            vm.reporte(f.copy(tiendaId = it))
        }
        if (f.tipo != "Stock") {
            Campo(
                "Desde · fecha UTC",
                f.desde,
                { vm.reporte(f.copy(desde = it)) },
                helper = "AAAA-MM-DD",
                enabled = !busy,
            )
            Campo(
                "Hasta · fecha UTC",
                f.hasta,
                { vm.reporte(f.copy(hasta = it)) },
                helper = "AAAA-MM-DD",
                enabled = !busy,
            )
        } else
            AvisoBox(
                "Stock vigente",
                "El reporte muestra el último inventario guardado localmente; no reconstruye saldos históricos.",
            )
        if (f.tipo == "Movimientos")
            Selector(
                "Tipo de movimiento",
                f.movimiento,
                listOf(
                    "TODOS" to "Todos los tipos",
                    "ENTRADA" to "Entrada",
                    "SALIDA" to "Salida",
                    "AJUSTE" to "Ajuste",
                ),
                enabled = !busy,
            ) {
                vm.reporte(f.copy(movimiento = it))
            }
        Tarjeta {
            Text("Vista previa", style = MaterialTheme.typography.titleMedium)
            when (f.tipo) {
                "Stock" -> {
                    val filas = CsvExporter.stock(d.snapshot, f)
                    Text(
                        "${filas.size} registros",
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    filas.take(4).forEach {
                        Linea("${it.producto.sku} · ${it.producto.nombre}", "${it.cantidad} uds")
                    }
                }
                "Diferencias" -> {
                    val filas = CsvExporter.conteos(d.snapshot, f)
                    Text(
                        "${filas.size} registros",
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    filas.take(4).forEach {
                        Linea("${it.id} · ${it.sku}", "${it.diferencia.conSigno()} · ${it.estado}")
                    }
                }
                else -> {
                    val filas = CsvExporter.movimientos(d.snapshot, f)
                    Text(
                        "${filas.size} registros",
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    filas.take(4).forEach {
                        Linea("${it.id} · ${it.sku}", "${it.tipo} ${it.cantidad.conSigno()}")
                    }
                }
            }
        }
        s.errorReporte?.let { AvisoBox("Revisa el reporte", it, "error") }
        Principal(
            if (s.exportacion == "GUARDANDO") "Guardando CSV…" else "Exportar CSV",
            icon = Icons.Default.Download,
            color = Color(0xFF006FDE),
            enabled = s.errorReporte == null || s.exportacion == "ERROR",
            busy = busy,
            onClick = vm::prepararCsv,
        )
        AvisoBox(
            "Elige dónde guardar",
            "Se mostrará el selector de documentos Android. El archivo se escribe en el destino que autorices.",
        )
        Text(
            "UTF-8 · Datos sintéticos · Exportar no cambia el inventario.",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun ExportacionResultadoScreen(s: GestionUi, volver: () -> Unit, reintentar: () -> Unit) {
    val success = s.exportacion == "EXITO"
    Pagina {
        Spacer(Modifier.height(20.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(
                color = if (success) Color(0xFFDEF8ED) else Color(0xFFE5EFF8),
                shape = androidx.compose.foundation.shape.CircleShape,
            ) {
                Icon(
                    if (success) Icons.Default.Check else Icons.Default.Close,
                    null,
                    tint = if (success) Exito else Petroleo,
                    modifier = Modifier.padding(22.dp).size(42.dp),
                )
            }
        }
        Text(
            if (success) "CSV exportado" else "Exportación cancelada",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            if (success) "Archivo guardado en el destino elegido."
            else "Cerraste el selector sin guardar. No se creó ningún archivo.",
            color = Muted,
        )
        s.documento?.let { doc ->
            Tarjeta {
                Text("${doc.tipo} · ${doc.ubicacion}", style = MaterialTheme.typography.titleMedium)
                Linea("Periodo UTC", doc.periodo)
                Linea("Registros", doc.registros.toString())
                if (success)
                    Text(
                        s.archivoGuardado,
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
            }
        }
        AvisoBox(
            if (success) "El inventario no cambió" else "Puedes volver a exportar",
            if (success) "Exportar solo genera una copia de los datos del reporte."
            else "Cancelar no es un error. Se mantienen los filtros del reporte.",
        )
        if (!success)
            Principal("Exportar de nuevo", icon = Icons.Default.Download, onClick = reintentar)
        Secundario("Volver a reportes", onClick = volver)
    }
}
