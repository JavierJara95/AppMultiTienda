package com.duoc.appmultitienda.gestion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.appmultitienda.gestion.model.*
import com.duoc.appmultitienda.gestion.repository.*
import com.duoc.appmultitienda.gestion.viewmodel.*

@Composable
fun ReposicionDashboardEntry() {
    val historial = remember { FakeHistorialAlertasRepository() }
    val reposiciones = remember { FakeReposicionRepository() }
    val vm: ReposicionDashboardViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            ReposicionDashboardViewModel(historial, reposiciones) as T
    })
    ReposicionDashboardScreen(vm)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReposicionDashboardScreen(vm: ReposicionDashboardViewModel) {
    val state by vm.uiState.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    Scaffold(topBar = { TopAppBar(title = { Text("MultiTienda · Gestión") }) }) { contentPadding ->
        Column(Modifier.fillMaxSize().padding(contentPadding).padding(horizontal = 16.dp)) {
            PrimaryTabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Dashboard") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Reposición") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 10.dp)) {
                listOf("TODAS" to "Todas", "SUC-01" to "Alameda", "SUC-02" to "Norte").forEach { (id, label) ->
                    FilterChip(selected = state.sucursal == id, onClick = { vm.seleccionarSucursal(id) }, label = { Text(label) })
                }
            }
            if (tab == 0) {
                val r = state.resumen
                Text("Resumen de alertas registradas", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text("Alertas activas: ${r.alertasActivas}")
                Text("Productos bajo mínimo: ${r.productosBajoMinimo}")
                Text("Quiebres: ${r.productosAgotados}")
                Text("Unidades restantes en productos con alerta: ${r.unidadesEnAlertas}")
                Text("Solicitudes pendientes: ${r.solicitudesPendientes}")
                Text("Movimientos de prueba: ${r.movimientosRegistrados}")
                Spacer(Modifier.height(12.dp))
                Text("Estas cifras son parciales: solo representan registros del repositorio simulado, no todo el inventario empresarial.", style = MaterialTheme.typography.bodySmall)
            } else {
                Text("Alertas que requieren atención", style = MaterialTheme.typography.titleLarge)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.alertasVisibles, key = { "${it.sucursalId}_${it.productoCodigo}" }) { alerta ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(alerta.productoNombre, style = MaterialTheme.typography.titleMedium)
                                Text("${alerta.sucursalNombre} · ${alerta.estado}")
                                Text("Stock: ${alerta.stockActual} / mínimo: ${alerta.stockMinimo}")
                                Button(onClick = { vm.elegirAlerta(alerta) }) { Text("Solicitar reposición") }
                            }
                        }
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("Solicitudes enviadas: ${state.solicitudes.size}")
                        state.solicitudes.forEach { Text("${it.id}: ${it.productoNombre} · ${it.cantidad} uds · ${it.estado}") }
                    }
                }
            }
            state.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        }
    }
    val seleccionado = state.seleccionado
    if (seleccionado != null) {
        AlertDialog(
            onDismissRequest = { if (!state.enviando) vm.cerrarFormulario() },
            title = { Text("Solicitar reposición") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${seleccionado.productoNombre} · ${seleccionado.sucursalNombre}")
                    OutlinedTextField(value = state.cantidad, onValueChange = vm::cambiarCantidad, label = { Text("Cantidad solicitada") }, singleLine = true)
                    OutlinedTextField(value = state.motivo, onValueChange = vm::cambiarMotivo, label = { Text("Motivo (5–200 caracteres)") })
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Text("Esta solicitud NO modifica las existencias.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(enabled = !state.enviando, onClick = { vm.enviarSolicitud() }) { Text(if (state.enviando) "Guardando…" else "Enviar") } },
            dismissButton = { TextButton(enabled = !state.enviando, onClick = vm::cerrarFormulario) { Text("Cancelar") } }
        )
    }
}
