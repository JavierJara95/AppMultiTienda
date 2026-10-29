package com.duoc.appmultitienda.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.appmultitienda.gestion.model.DiferenciaInventario
import com.duoc.appmultitienda.gestion.model.EstadoConteo
import com.duoc.appmultitienda.gestion.repository.DiferenciasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

enum class FiltroDiferencias { PENDIENTES, RESUELTAS }

data class DiferenciasUiState(
    val cargando: Boolean = true,
    val conteos: List<DiferenciaInventario> = emptyList(),
    val filtro: FiltroDiferencias = FiltroDiferencias.PENDIENTES,
    val idSeleccionado: String? = null,
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

    fun cambiarFiltro(filtro: FiltroDiferencias) {
        _uiState.value = _uiState.value.copy(filtro = filtro, idSeleccionado = null)
    }
    fun abrirDetalle(id: String) {
        _uiState.value = _uiState.value.copy(idSeleccionado = id)
    }
    fun cerrarDetalle() {
        _uiState.value = _uiState.value.copy(idSeleccionado = null)
    }
}
