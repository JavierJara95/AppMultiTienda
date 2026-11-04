package com.duoc.appmultitienda.operador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.appmultitienda.operador.model.ProductoOperador
import com.duoc.appmultitienda.operador.repository.FakeProductoOperadorRepository
import com.duoc.appmultitienda.operador.repository.ProductoOperadorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BusquedaProductoUiState(
    val consulta: String = "",
    val productos: List<ProductoOperador> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null,
    val busquedaRealizada: Boolean = false
)

class BusquedaProductoViewModel(
    private val repository: ProductoOperadorRepository =
        FakeProductoOperadorRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        BusquedaProductoUiState()
    )

    val uiState: StateFlow<BusquedaProductoUiState> =
        _uiState.asStateFlow()

    fun actualizarConsulta(texto: String) {

        _uiState.value = _uiState.value.copy(
            consulta = texto,
            error = null,
            productos = emptyList(),
            busquedaRealizada = false
        )
    }

    fun buscar(sucursalId: String) {

        val consulta = _uiState.value.consulta.trim()

        if (consulta.isBlank()) {

            _uiState.value = _uiState.value.copy(
                error = "Debes ingresar un código o nombre.",
                productos = emptyList(),
                busquedaRealizada = false
            )

            return
        }

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                cargando = true,
                error = null,
                productos = emptyList(),
                busquedaRealizada = false
            )

            try {

                val resultados = repository.buscarProductos(
                    sucursalId,
                    consulta
                )

                _uiState.value = _uiState.value.copy(
                    productos = resultados,
                    cargando = false,
                    busquedaRealizada = true
                )

            } catch (e: Exception) {

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "No fue posible consultar productos."
                )
            }
        }
    }

    fun limpiarBusqueda() {

        _uiState.value = BusquedaProductoUiState()
    }
}