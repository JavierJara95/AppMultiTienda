package com.duoc.appmultitienda.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.appmultitienda.gestion.model.DiferenciaInventario
import com.duoc.appmultitienda.gestion.model.EstadoConteo
import com.duoc.appmultitienda.gestion.repository.DiferenciasRepository
import com.duoc.appmultitienda.gestion.repository.ResultadoDecision
import com.duoc.appmultitienda.gestion.validation.DecisionValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class FiltroDiferencias { PENDIENTES, RESUELTAS }

data class DiferenciasUiState(
    val cargando: Boolean = true,
    val conteos: List<DiferenciaInventario> = emptyList(),
    val filtro: FiltroDiferencias = FiltroDiferencias.PENDIENTES,
    val idSeleccionado: String? = null,
    val accion: Boolean? = null, // true=aprobar, false=rechazar
    val motivo: String = "",
    val errorMotivo: String? = null,
    val procesando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
) {
    val visibles: List<DiferenciaInventario> get() = conteos.filter {
        if (filtro == FiltroDiferencias.PENDIENTES) it.estado == EstadoConteo.PENDIENTE
        else it.estado != EstadoConteo.PENDIENTE
    }
    val seleccionado: DiferenciaInventario? get() = conteos.firstOrNull { it.idConteo == idSeleccionado }
}

class DiferenciasViewModel(private val repository: DiferenciasRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DiferenciasUiState())
    val uiState: StateFlow<DiferenciasUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.observarConteos().collect { registros ->
                    _uiState.value = _uiState.value.copy(conteos = registros, cargando = false, error = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(cargando = false, error = "No fue posible consultar los conteos")
            }
        }
    }

    fun cambiarFiltro(filtro: FiltroDiferencias) { _uiState.value = _uiState.value.copy(filtro = filtro, idSeleccionado = null, accion = null) }
    fun abrirDetalle(id: String) { _uiState.value = _uiState.value.copy(idSeleccionado = id, accion = null, motivo = "", errorMotivo = null, mensaje = null) }
    fun cerrarDetalle() { if (!_uiState.value.procesando) _uiState.value = _uiState.value.copy(idSeleccionado = null, accion = null) }
    fun elegirAccion(aprobar: Boolean) { _uiState.value = _uiState.value.copy(accion = aprobar, motivo = "", errorMotivo = null, mensaje = null) }
    fun cambiarMotivo(valor: String) { _uiState.value = _uiState.value.copy(motivo = valor, errorMotivo = null) }
    fun limpiarMensaje() { _uiState.value = _uiState.value.copy(mensaje = null) }

    fun confirmarDecision(actorId: String = "ENC-004") {
        val previo = _uiState.value
        if (previo.procesando) return
        val id = previo.idSeleccionado ?: return
        val aprobar = previo.accion ?: return
        val error = DecisionValidator.errorMotivo(previo.motivo)
        if (error != null) {
            _uiState.value = previo.copy(errorMotivo = error)
            return
        }
        _uiState.value = previo.copy(procesando = true, error = null)
        viewModelScope.launch {
            val resultado = try { repository.decidir(id, aprobar, previo.motivo, actorId) }
                            catch (e: Exception) { ResultadoDecision.Error("Error de comunicación. Intenta otra vez") }
            _uiState.value = when (resultado) {
                is ResultadoDecision.Exito -> _uiState.value.copy(
                    procesando = false, idSeleccionado = null, accion = null,
                    mensaje = "${if (aprobar) "Ajuste aprobado" else "Conteo rechazado"}. Saldo: ${resultado.saldoFinal} uds"
                )
                is ResultadoDecision.Conflicto -> _uiState.value.copy(
                    procesando = false, accion = null,
                    error = "El saldo cambió a ${resultado.saldoVigente} uds. Requiere nuevo conteo o revisión."
                )
                is ResultadoDecision.Error -> _uiState.value.copy(procesando = false, error = resultado.mensaje)
            }
        }
    }
}
