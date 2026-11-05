package com.duoc.appmultitienda.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.appmultitienda.core.data.InventarioGateway
import com.duoc.appmultitienda.gestion.model.FilaReporte
import com.duoc.appmultitienda.gestion.model.SucursalGestion
import com.duoc.appmultitienda.gestion.repository.ReportesSucursalesRepository
import com.duoc.appmultitienda.gestion.ui.ReportesSoloEntry
import com.duoc.appmultitienda.gestion.viewmodel.ReportesSucursalesViewModel
import com.duoc.appmultitienda.gestion.viewmodel.SesionViewModel
import com.duoc.appmultitienda.vmFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Solo Gerencia original: recibir, consultar y exportar CSV y PDF. */
private class ReportesLocales(private val repo: InventarioGateway) : ReportesSucursalesRepository {
    private val _sucursales = MutableStateFlow<List<SucursalGestion>>(emptyList())
    override val sucursales: StateFlow<List<SucursalGestion>> = _sucursales

    override suspend fun cambiarEstado(id: String, activa: Boolean): Boolean = false

    override suspend fun obtenerMovimientos(sucursalId: String?): List<FilaReporte> {
        repo.refrescar()
        val s = repo.datos.value.snapshot
        _sucursales.value = s.sucursales.map { SucursalGestion(it.id, it.nombre, it.direccion, it.activa) }
        return s.movimientos.filter { sucursalId == null || it.tiendaId == sucursalId }.map { m ->
            val nombre = s.existencias.firstOrNull { it.tiendaId == m.tiendaId && it.producto.sku == m.sku }?.producto?.nombre ?: m.sku
            FilaReporte(m.id, m.tiendaId, nombre, m.tipo, m.cantidad, m.instanteUtc)
        }
    }
}

@Composable
fun GerenciaOriginalRoot(repo: InventarioGateway, sesion: SesionViewModel) {
    val adapter = remember(repo) { ReportesLocales(repo) }
    val vm: ReportesSucursalesViewModel = viewModel(
        key = "gerencia-informes-original",
        factory = vmFactory { ReportesSucursalesViewModel(adapter) }
    )
    Column(Modifier.fillMaxSize()) {
        Surface(color = Marino) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Gerencia · Informes", style = MaterialTheme.typography.titleLarge, color = androidx.compose.ui.graphics.Color.White)
                TextButton(onClick = sesion::logout) { Text("Cerrar sesión", color = androidx.compose.ui.graphics.Color.White) }
            }
        }
        Box(Modifier.weight(1f)) { ReportesSoloEntry(vm) }
    }
}
