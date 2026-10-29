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
import com.duoc.appmultitienda.gestion.model.DiferenciaInventario
import com.duoc.appmultitienda.gestion.model.EstadoConteo
import com.duoc.appmultitienda.gestion.repository.FakeDiferenciasRepository
import com.duoc.appmultitienda.gestion.viewmodel.DiferenciasViewModel
import com.duoc.appmultitienda.gestion.viewmodel.FiltroDiferencias

@Composable
fun DiferenciasEntry() {
    val vm: DiferenciasViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DiferenciasViewModel(FakeDiferenciasRepository()) as T
    })
    DiferenciasScreen(vm)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiferenciasScreen(vm: DiferenciasViewModel) {
    val state by vm.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("MultiTienda · Diferencias") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Text("Conteos para revisión", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = state.filtro == FiltroDiferencias.PENDIENTES,
                    onClick = { vm.cambiarFiltro(FiltroDiferencias.PENDIENTES) }, label = { Text("Pendientes") })
                FilterChip(selected = state.filtro == FiltroDiferencias.RESUELTAS,
                    onClick = { vm.cambiarFiltro(FiltroDiferencias.RESUELTAS) }, label = { Text("Resueltas") })
            }
            state.mensaje?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.primary)
                TextButton(onClick = vm::limpiarMensaje) { Text("Cerrar aviso") }
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            when {
                state.cargando -> CircularProgressIndicator()
                state.visibles.isEmpty() -> Text("No hay conteos para este filtro")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                    items(state.visibles, key = { it.idConteo }) { conteo ->
                        DiferenciaCard(conteo) { vm.abrirDetalle(conteo.idConteo) }
                    }
                }
            }
        }
        state.seleccionado?.let { conteo ->
            AlertDialog(
                onDismissRequest = vm::cerrarDetalle,
                title = { Text("Conteo ${conteo.idConteo}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${conteo.nombreProducto}\n${conteo.nombreSucursal}\nOficial: ${conteo.stockOficial} uds\nContado: ${conteo.stockContado} uds\nDiferencia: ${conteo.diferencia} uds\nOperador: ${conteo.operadorId}\n${conteo.fechaUtc}\n\n${conteo.observacion}")
                        if (conteo.estado == EstadoConteo.PENDIENTE) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { vm.elegirAccion(true) }, enabled = !state.procesando) { Text("Aprobar") }
                                OutlinedButton(onClick = { vm.elegirAccion(false) }, enabled = !state.procesando) { Text("Rechazar") }
                            }
                        }
                        if (state.accion != null) {
                            Text(if (state.accion == true) "Motivo de aprobación" else "Motivo de rechazo")
                            OutlinedTextField(
                                value = state.motivo, onValueChange = vm::cambiarMotivo,
                                label = { Text("Justificación (5 a 200 caracteres)") },
                                isError = state.errorMotivo != null, modifier = Modifier.fillMaxWidth(),
                                supportingText = { state.errorMotivo?.let { Text(it) } }
                            )
                            Text("${state.motivo.trim().length}/200 caracteres")
                            if (state.procesando) CircularProgressIndicator()
                            Button(onClick = { vm.confirmarDecision() }, enabled = !state.procesando) {
                                Text(if (state.accion == true) "Confirmar ajuste" else "Confirmar rechazo")
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = vm::cerrarDetalle, enabled = !state.procesando) { Text("Cerrar") } }
            )
        }
    }
}

@Composable
private fun DiferenciaCard(conteo: DiferenciaInventario, onDetalle: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(conteo.nombreProducto, style = MaterialTheme.typography.titleMedium)
            Text("${conteo.idConteo} · ${conteo.nombreSucursal}")
            Text("Oficial: ${conteo.stockOficial} | Contado: ${conteo.stockContado}")
            Text("Diferencia: ${if (conteo.diferencia > 0) "+" else ""}${conteo.diferencia} uds")
            Text("${conteo.estado} · ${conteo.operadorId}")
            Button(onClick = onDetalle) { Text("Ver detalle") }
        }
    }
}
