package com.duoc.appmultitienda.gestion.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duoc.appmultitienda.gestion.model.UsuarioSesion
import com.duoc.appmultitienda.gestion.model.Rol
import com.duoc.appmultitienda.gestion.ui.GestionEntry
import com.duoc.appmultitienda.gestion.ui.DiferenciasEntry
import com.duoc.appmultitienda.gestion.ui.HistorialAlertasEntry
import com.duoc.appmultitienda.gestion.ui.ReposicionDashboardEntry
import com.duoc.appmultitienda.gestion.ui.ReportesSucursalesEntry
import com.duoc.appmultitienda.gestion.ui.ReportesSoloEntry
import com.duoc.appmultitienda.gestion.repository.FuenteGestionCompartida
import com.duoc.appmultitienda.gestion.viewmodel.ReportesSucursalesViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Punto de entrada aislado del módulo Javier. No modifica MainActivity permanentemente.
 * No necesita Navigation Compose para esta fase: navegación por estado de Compose.
 */
@Composable
fun JavierGestionEntry() {
    var sesion by remember { mutableStateOf<UsuarioSesion?>(null) }
    var loginKey by rememberSaveable { mutableIntStateOf(0) }
    val fuente = remember { FuenteGestionCompartida() }

    MaterialTheme {
        if (sesion == null) {
            // Al cerrar sesión se crea una instancia limpia del formulario y de su ViewModel.
            key(loginKey) {
                GestionEntry(loginKey = "javier-login-$loginKey", onSessionStarted = { sesion = it })
            }
        } else {
            PanelGestion(
                sesion = sesion!!,
                fuente = fuente,
                onSalir = {
                    sesion = null
                    loginKey++
                }
            )
        }
    }
}

@Composable
private fun PanelGestion(sesion: UsuarioSesion, fuente: FuenteGestionCompartida, onSalir: () -> Unit) {
    val disponibles = remember(sesion.rol) { GestionPermisos.rutasPara(sesion.rol) }
    var seleccion by remember(sesion.rol) { mutableStateOf(disponibles.firstOrNull()) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("MultiTienda · Javier", style = MaterialTheme.typography.titleMedium)
                Text("${sesion.rol.etiqueta} · ${sesion.identificador}", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onSalir) { Text("Salir") }
        }
        if (disponibles.isEmpty()) {
            Text(
                "El módulo Operador corresponde a Esteban. Se integrará después.",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                disponibles.forEach { ruta ->
                    FilterChip(
                        selected = seleccion == ruta,
                        onClick = { seleccion = ruta },
                        label = { Text(ruta.etiqueta) }
                    )
                }
            }
            HorizontalDivider()
            Box(Modifier.weight(1f)) {
                when (seleccion) {
                    GestionRuta.DIFERENCIAS -> DiferenciasEntry(repository = fuente, actorId = sesion.identificador)
                    GestionRuta.HISTORIAL_ALERTAS -> HistorialAlertasEntry(repository = fuente)
                    GestionRuta.DASHBOARD_REPOSICION -> ReposicionDashboardEntry()
                    GestionRuta.REPORTES, GestionRuta.REPORTES_SUCURSALES -> {
                        val reportesVm: ReportesSucursalesViewModel = viewModel(
                            factory = object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                    ReportesSucursalesViewModel(fuente) as T
                            }
                        )
                        LaunchedEffect(seleccion) { reportesVm.actualizarReporte() }
                        if (seleccion == GestionRuta.REPORTES) {
                            ReportesSoloEntry(vm = reportesVm)
                        } else {
                            ReportesSucursalesEntry(vm = reportesVm)
                        }
                    }
                    null -> Unit
                }
            }
        }
    }
}
