package com.duoc.appmultitienda.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.appmultitienda.gestion.model.*
import com.duoc.appmultitienda.gestion.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

fun construirResumen(alertas: List<AlertaStock>, solicitudes: List<SolicitudReposicion>, movimientos: List<MovimientoAuditoria>, sucursal: String): DashboardResumen {
    val visibles = alertas.filter { sucursal == "TODAS" || it.sucursalId == sucursal }
    val activas = visibles.filter { it.estado != EstadoStock.NORMAL }
    return DashboardResumen(
        unidadesEnAlertas = activas.sumOf { it.stockActual },
        productosBajoMinimo = activas.count { it.estado == EstadoStock.BAJO },
        productosAgotados = activas.count { it.estado == EstadoStock.QUIEBRE },
        alertasActivas = activas.size,
        solicitudesPendientes = solicitudes.count { it.estado == EstadoReposicion.PENDIENTE && (sucursal == "TODAS" || it.sucursalId == sucursal) },
        movimientosRegistrados = movimientos.count { sucursal == "TODAS" || it.sucursalId == sucursal }
    )
}

data class ReposicionDashboardUiState(
    val alertas: List<AlertaStock> = emptyList(),
    val solicitudes: List<SolicitudReposicion> = emptyList(),
    val movimientos: List<MovimientoAuditoria> = emptyList(),
    val sucursal: String = "TODAS",
    val cantidad: String = "",
    val motivo: String = "",
    val seleccionado: AlertaStock? = null,
    val enviando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
) {
    val alertasVisibles: List<AlertaStock> get() = alertas.filter { (sucursal == "TODAS" || it.sucursalId == sucursal) && it.estado != EstadoStock.NORMAL }
    val resumen: DashboardResumen get() = construirResumen(alertas, solicitudes, movimientos, sucursal)
}

class ReposicionDashboardViewModel(
    historial: HistorialAlertasRepository,
    private val reposiciones: ReposicionRepository
) : ViewModel() {
    private val formulario = MutableStateFlow(ReposicionDashboardUiState())
    val uiState: StateFlow<ReposicionDashboardUiState> = combine(
        historial.observarAlertas(), historial.observarMovimientos(), reposiciones.observarSolicitudes(), formulario
    ) { alertas, movimientos, solicitudes, form ->
        form.copy(alertas = alertas, movimientos = movimientos, solicitudes = solicitudes)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReposicionDashboardUiState())

    fun seleccionarSucursal(id: String) { formulario.update { it.copy(sucursal = id) } }
    fun elegirAlerta(alerta: AlertaStock) { formulario.update { it.copy(seleccionado = alerta, cantidad = "", motivo = "", error = null, mensaje = null) } }
    fun cambiarCantidad(valor: String) { formulario.update { it.copy(cantidad = valor, error = null) } }
    fun cambiarMotivo(valor: String) { formulario.update { it.copy(motivo = valor, error = null) } }
    fun cerrarFormulario() { formulario.update { it.copy(seleccionado = null, error = null) } }
    fun limpiarMensaje() { formulario.update { it.copy(mensaje = null) } }

    fun enviarSolicitud(actorId: String = "ENC-004") {
        val actual = formulario.value
        val alerta = actual.seleccionado ?: return
        val error = ReposicionValidator.validar(actual.cantidad, actual.motivo)
        if (error != null) { formulario.update { it.copy(error = error) }; return }
        if (actual.enviando) return
        formulario.update { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            val resultado = reposiciones.solicitar(alerta, actual.cantidad.toInt(), actual.motivo, actorId)
            formulario.update {
                resultado.fold(
                    onSuccess = { solicitud -> it.copy(enviando = false, seleccionado = null, mensaje = "Solicitud ${solicitud.id} registrada", error = null) },
                    onFailure = { ex -> it.copy(enviando = false, error = ex.message ?: "Error al guardar") }
                )
            }
        }
    }
}
