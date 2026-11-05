package com.duoc.appmultitienda.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.appmultitienda.gestion.model.*
import com.duoc.appmultitienda.gestion.repository.HistorialAlertasRepository
import kotlinx.coroutines.flow.*

// El filtro queda fuera de Compose y se puede probar con JUnit.
enum class FiltroMovimiento { TODOS, ENTRADA, SALIDA, AJUSTE }

data class HistorialAlertasUiState(
    val movimientos: List<MovimientoAuditoria> = emptyList(),
    val alertas: List<AlertaStock> = emptyList(),
    val sucursalId: String = "TODAS",
    val filtroMovimiento: FiltroMovimiento = FiltroMovimiento.TODOS,
    val busqueda: String = "",
    val soloAlertasActivas: Boolean = true
) {
    val movimientosVisibles: List<MovimientoAuditoria> get() = movimientos.filter { m ->
        (sucursalId == "TODAS" || m.sucursalId == sucursalId) &&
            (filtroMovimiento == FiltroMovimiento.TODOS || m.tipo.name == filtroMovimiento.name) &&
            (busqueda.isBlank() || listOf(m.id, m.productoCodigo, m.productoNombre).any { it.contains(busqueda.trim(), ignoreCase = true) })
    }
    val alertasVisibles: List<AlertaStock> get() = alertas.filter { a ->
        (sucursalId == "TODAS" || a.sucursalId == sucursalId) &&
            (!soloAlertasActivas || a.estado != EstadoStock.NORMAL)
    }
}

class HistorialAlertasViewModel(repo: HistorialAlertasRepository) : ViewModel() {
    private val filtros = MutableStateFlow(HistorialAlertasUiState())
    val uiState: StateFlow<HistorialAlertasUiState> = combine(
        repo.observarMovimientos(), repo.observarAlertas(), filtros
    ) { movimientos, alertas, f ->
        f.copy(movimientos = movimientos, alertas = alertas)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorialAlertasUiState())

    fun seleccionarSucursal(id: String) { filtros.update { it.copy(sucursalId = id) } }
    fun seleccionarMovimiento(tipo: FiltroMovimiento) { filtros.update { it.copy(filtroMovimiento = tipo) } }
    fun buscar(texto: String) { filtros.update { it.copy(busqueda = texto) } }
    fun mostrarSoloAlertasActivas(activo: Boolean) { filtros.update { it.copy(soloAlertasActivas = activo) } }
}
