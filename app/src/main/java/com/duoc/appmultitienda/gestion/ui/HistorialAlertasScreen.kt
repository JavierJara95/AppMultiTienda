package com.duoc.appmultitienda.gestion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.appmultitienda.gestion.model.*
import com.duoc.appmultitienda.gestion.repository.FakeHistorialAlertasRepository
import com.duoc.appmultitienda.gestion.viewmodel.*

enum class PestanaGestion { HISTORIAL, ALERTAS }

@Composable
fun HistorialAlertasEntry(repository: com.duoc.appmultitienda.gestion.repository.HistorialAlertasRepository = FakeHistorialAlertasRepository()) {
    val vm: HistorialAlertasViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HistorialAlertasViewModel(repository) as T
    })
    HistorialAlertasScreen(vm)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialAlertasScreen(vm: HistorialAlertasViewModel) {
    val estado by vm.uiState.collectAsState()
    var pestana by remember { mutableStateOf(PestanaGestion.HISTORIAL) }
    Scaffold(topBar = { TopAppBar(title = { Text("MultiTienda · Gestión") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            TabRow(selectedTabIndex = pestana.ordinal) {
                PestanaGestion.entries.forEach { p ->
                    Tab(selected = pestana == p, onClick = { pestana = p }, text = { Text(if (p == PestanaGestion.HISTORIAL) "Historial" else "Alertas") })
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Ubicación", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("TODAS" to "Todas", "SUC-01" to "Alameda", "SUC-02" to "Norte").forEach { (id, nombre) ->
                    FilterChip(selected = estado.sucursalId == id, onClick = { vm.seleccionarSucursal(id) }, label = { Text(nombre) })
                }
            }
            if (pestana == PestanaGestion.HISTORIAL) {
                OutlinedTextField(value = estado.busqueda, onValueChange = vm::buscar,
                    label = { Text("Buscar producto o ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FiltroMovimiento.entries.forEach { f ->
                        FilterChip(selected = estado.filtroMovimiento == f, onClick = { vm.seleccionarMovimiento(f) },
                            label = { Text(f.name.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                Text("${estado.movimientosVisibles.size} movimientos", style = MaterialTheme.typography.titleMedium)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(estado.movimientosVisibles, key = { it.id }) { m ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text("${m.tipo.name} · ${m.id}", style = MaterialTheme.typography.titleSmall)
                                Text(m.productoNombre)
                                Text("${m.cantidad} uds · Saldo: ${m.saldoFinal} · ${m.sucursalNombre}")
                                Text("${m.actorId} · ${m.fechaUtc}", style = MaterialTheme.typography.bodySmall)
                                Text("Motivo: ${m.motivo}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    if (estado.movimientosVisibles.isEmpty()) item { Text("No hay movimientos con esos filtros") }
                }
            } else {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("Solo alertas activas", modifier = Modifier.weight(1f))
                    Switch(checked = estado.soloAlertasActivas, onCheckedChange = vm::mostrarSoloAlertasActivas)
                }
                Text("${estado.alertasVisibles.size} productos", style = MaterialTheme.typography.titleMedium)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(estado.alertasVisibles, key = { "${it.sucursalId}-${it.productoCodigo}" }) { a ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text("${a.estado.name} · ${a.productoNombre}", style = MaterialTheme.typography.titleSmall)
                                Text("${a.sucursalNombre} · ${a.productoCodigo}")
                                Text("Existencias: ${a.stockActual} · Mínimo: ${a.stockMinimo}")
                            }
                        }
                    }
                    if (estado.alertasVisibles.isEmpty()) item { Text("Sin alertas en esta ubicación") }
                }
            }
        }
    }
}
