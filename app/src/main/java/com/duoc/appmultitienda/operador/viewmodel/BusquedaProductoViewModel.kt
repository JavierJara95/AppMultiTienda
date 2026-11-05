package com.duoc.appmultitienda.operador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.duoc.appmultitienda.operador.model.ProductoOperador
import com.duoc.appmultitienda.operador.repository.FakeProductoOperadorRepository
import com.duoc.appmultitienda.operador.repository.ProductoOperadorRepository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class BusquedaProductoUiState(

    val consulta: String = "",

    val productos: List<ProductoOperador> = emptyList(),

    val cargando: Boolean = false,

    val busquedaRealizada: Boolean = false,

    val error: String? = null,

    val busquedaPorEscaner: Boolean = false
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

    private var trabajoBusqueda: Job? = null

    private var versionBusqueda = 0L


    // ACTUALIZAR CONSULTA

    fun actualizarConsulta(texto: String) {

        cancelarBusquedaAnterior()

        _uiState.value = BusquedaProductoUiState(
            consulta = texto
        )
    }


    // CANCELAR CONSULTAS ANTERIORES

    private fun cancelarBusquedaAnterior() {

        versionBusqueda++

        trabajoBusqueda?.cancel()

        trabajoBusqueda = null
    }


    // LIMPIAR BÚSQUEDA

    fun limpiarBusqueda() {

        cancelarBusquedaAnterior()

        _uiState.value = BusquedaProductoUiState()
    }


    // BÚSQUEDA MANUAL

    fun buscar(sucursalId: String) {

        val texto = _uiState.value.consulta.trim()

        ejecutarBusqueda(
            codigoOTexto = texto,
            sucursalId = sucursalId,
            busquedaExacta = false
        )
    }


    // BÚSQUEDA MEDIANTE CÁMARA

    fun buscarCodigoEscaneado(
        codigo: String,
        sucursalId: String
    ) {

        ejecutarBusqueda(
            codigoOTexto = codigo.trim(),
            sucursalId = sucursalId,
            busquedaExacta = true
        )
    }


    // EJECUTAR BÚSQUEDA

    private fun ejecutarBusqueda(
        codigoOTexto: String,
        sucursalId: String,
        busquedaExacta: Boolean
    ) {

        cancelarBusquedaAnterior()

        val versionActual = versionBusqueda

        val texto = codigoOTexto.trim()

        val sucursal = sucursalId.trim()


        // VALIDAR TEXTO

        if (texto.isBlank()) {

            _uiState.value = BusquedaProductoUiState(

                consulta = texto,

                error = if (busquedaExacta) {
                    "No se recibió un código válido."
                } else {
                    "Debes ingresar un código o nombre."
                },

                busquedaPorEscaner = busquedaExacta
            )

            return
        }


        // VALIDAR SUCURSAL

        if (sucursal.isBlank()) {

            _uiState.value = BusquedaProductoUiState(

                consulta = texto,

                error = "Debes seleccionar una sucursal.",

                busquedaPorEscaner = busquedaExacta
            )

            return
        }


        // ACTIVAR CARGA

        _uiState.value = BusquedaProductoUiState(

            consulta = texto,

            productos = emptyList(),

            cargando = true,

            busquedaRealizada = false,

            error = null,

            busquedaPorEscaner = busquedaExacta
        )


        // CONSULTAR REPOSITORIO

        trabajoBusqueda = viewModelScope.launch {

            try {

                // NOMBRE CORRECTO DE TU REPOSITORIO

                val resultados = repository.buscarProductos(

                    sucursalId = sucursal,

                    consulta = texto
                )


                // IGNORAR BÚSQUEDAS ANTERIORES

                if (versionActual != versionBusqueda) {
                    return@launch
                }


                // FILTRAR SUCURSAL

                val productosSucursal = resultados.filter {

                    it.sucursalId == sucursal
                }


                // COINCIDENCIA EXACTA PARA EL ESCÁNER

                val productosEncontrados = if (busquedaExacta) {

                    productosSucursal.filter { producto ->

                        val codigoInterno =
                            producto.codigo.equals(
                                texto,
                                ignoreCase = true
                            )

                        val codigoBarras =
                            producto.codigoBarras.trim() == texto

                        codigoInterno || codigoBarras
                    }

                } else {

                    productosSucursal
                }


                // MOSTRAR RESULTADOS

                _uiState.value = BusquedaProductoUiState(

                    consulta = texto,

                    productos = productosEncontrados,

                    cargando = false,

                    busquedaRealizada = true,

                    error = null,

                    busquedaPorEscaner = busquedaExacta
                )


            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                if (versionActual != versionBusqueda) {
                    return@launch
                }

                _uiState.value = BusquedaProductoUiState(

                    consulta = texto,

                    productos = emptyList(),

                    cargando = false,

                    busquedaRealizada = false,

                    error = "No se pudo consultar el inventario.",

                    busquedaPorEscaner = busquedaExacta
                )
            }
        }
    }


    // OBTENER PRODUCTO POR CÓDIGO EXACTO

    fun obtenerProductoExacto(
        codigo: String,
        sucursalId: String
    ): ProductoOperador? {

        val codigoLimpio = codigo.trim()

        if (
            codigoLimpio.isBlank() ||
            sucursalId.isBlank()
        ) {
            return null
        }

        return _uiState.value.productos.firstOrNull { producto ->

            producto.sucursalId == sucursalId &&

                    (
                            producto.codigo.equals(
                                codigoLimpio,
                                ignoreCase = true
                            ) ||

                                    producto.codigoBarras.trim() ==
                                    codigoLimpio
                            )
        }
    }


    // LIBERAR RECURSOS

    override fun onCleared() {

        cancelarBusquedaAnterior()

        super.onCleared()
    }
}