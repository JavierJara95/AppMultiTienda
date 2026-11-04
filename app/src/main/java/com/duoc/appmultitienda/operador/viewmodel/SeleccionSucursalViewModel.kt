package com.duoc.appmultitienda.operador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.appmultitienda.operador.model.SucursalOperador
import com.duoc.appmultitienda.operador.repository.SucursalOperadorRepository
import com.duoc.appmultitienda.operador.repository.FakeSucursalOperadorRepository
import com.duoc.appmultitienda.operador.validation.SeleccionSucursalValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SeleccionSucursalUiState(
    val sucursales: List<SucursalOperador> = emptyList(),
    val sucursalSeleccionada: SucursalOperador? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val confirmada: Boolean = false
)

class SeleccionSucursalViewModel(
    private val repository: SucursalOperadorRepository =
        FakeSucursalOperadorRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SeleccionSucursalUiState()
    )

    val uiState: StateFlow<SeleccionSucursalUiState> =
        _uiState.asStateFlow()

    init {
        cargarSucursales()
    }

    fun cargarSucursales() {

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                cargando = true,
                error = null,
                confirmada = false
            )

            try {

                val lista = repository.obtenerSucursales()

                _uiState.value = _uiState.value.copy(
                    sucursales = lista,
                    cargando = false
                )

            } catch (e: Exception) {

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "No fue posible cargar las sucursales."
                )
            }
        }
    }

    fun seleccionarSucursal(sucursal: SucursalOperador) {

        val sucursalDisponible = _uiState.value.sucursales
            .find { it.id == sucursal.id }

        val error = SeleccionSucursalValidator.validar(
            sucursalDisponible
        )

        if (error != null) {

            _uiState.value = _uiState.value.copy(
                error = error,
                confirmada = false
            )

            return
        }

        _uiState.value = _uiState.value.copy(
            sucursalSeleccionada = sucursalDisponible,
            error = null,
            confirmada = false
        )
    }

    fun confirmarSucursal() {

        val error = SeleccionSucursalValidator.validar(
            _uiState.value.sucursalSeleccionada
        )

        if (error != null) {

            _uiState.value = _uiState.value.copy(
                error = error,
                confirmada = false
            )

            return
        }

        _uiState.value = _uiState.value.copy(
            confirmada = true,
            error = null
        )
    }
    fun reiniciarSeleccion() {

        _uiState.value = _uiState.value.copy(
            sucursalSeleccionada = null,
            confirmada = false,
            error = null
        )
    }
}