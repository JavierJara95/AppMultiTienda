package com.duoc.appmultitienda.operador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.duoc.appmultitienda.operador.model.ComprobanteMovimientoDemo
import com.duoc.appmultitienda.operador.model.ProductoOperador

import com.duoc.appmultitienda.operador.repository.ConfirmacionMovimientoDemoRepository
import com.duoc.appmultitienda.operador.repository.FakeConfirmacionMovimientoDemoRepository

import com.duoc.appmultitienda.operador.validation.MovimientoValidator
import com.duoc.appmultitienda.operador.validation.TipoMovimiento

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class MovimientoUiState(

    val tipo: TipoMovimiento = TipoMovimiento.ENTRADA,

    val cantidad: String = "",

    val motivo: String = "",

    val referencia: String = "",

    val errorCantidad: String? = null,

    val errorMotivo: String? = null,

    val revisando: Boolean = false,

    val confirmandoDemo: Boolean = false,

    val errorConfirmacion: String? = null,

    val comprobanteDemo: ComprobanteMovimientoDemo? = null
)


class MovimientoViewModel(

    private val repository: ConfirmacionMovimientoDemoRepository =
        FakeConfirmacionMovimientoDemoRepository()

) : ViewModel() {


    private val _uiState = MutableStateFlow(
        MovimientoUiState()
    )


    val uiState: StateFlow<MovimientoUiState> =
        _uiState.asStateFlow()


    // 1. SELECCIONAR ENTRADA O SALIDA

    fun seleccionarTipo(tipo: TipoMovimiento) {

        if (operacionBloqueada()) return

        _uiState.value = _uiState.value.copy(
            tipo = tipo,
            errorCantidad = null,
            revisando = false,
            errorConfirmacion = null
        )
    }


    // 2. ACTUALIZAR CANTIDAD

    fun actualizarCantidad(cantidad: String) {

        if (operacionBloqueada()) return

        _uiState.value = _uiState.value.copy(
            cantidad = cantidad,
            errorCantidad = null,
            revisando = false,
            errorConfirmacion = null
        )
    }


    // 3. ACTUALIZAR MOTIVO

    fun actualizarMotivo(motivo: String) {

        if (operacionBloqueada()) return

        _uiState.value = _uiState.value.copy(
            motivo = motivo,
            errorMotivo = null,
            revisando = false,
            errorConfirmacion = null
        )
    }


    // 4. ACTUALIZAR REFERENCIA

    fun actualizarReferencia(referencia: String) {

        if (operacionBloqueada()) return

        _uiState.value = _uiState.value.copy(
            referencia = referencia,
            revisando = false,
            errorConfirmacion = null
        )
    }


    // 5. REVISAR MOVIMIENTO

    fun revisarMovimiento(
        producto: ProductoOperador
    ) {

        if (operacionBloqueada()) return

        val estado = _uiState.value

        val resultado = MovimientoValidator.validar(
            tipo = estado.tipo,
            cantidadTexto = estado.cantidad,
            motivo = estado.motivo,
            stockActual = producto.stockActual
        )

        _uiState.value = estado.copy(
            errorCantidad = resultado.errorCantidad,
            errorMotivo = resultado.errorMotivo,
            revisando = resultado.esValido,
            errorConfirmacion = null
        )
    }


    // 6. CALCULAR STOCK PREVISTO

    fun calcularStockPrevisto(
        producto: ProductoOperador
    ): Int? {

        val estado = _uiState.value

        val resultado = MovimientoValidator.validar(
            tipo = estado.tipo,
            cantidadTexto = estado.cantidad,
            motivo = estado.motivo,
            stockActual = producto.stockActual
        )

        if (!resultado.esValido) {
            return null
        }

        val cantidad = estado.cantidad.toIntOrNull()
            ?: return null

        return when (estado.tipo) {

            TipoMovimiento.ENTRADA ->
                producto.stockActual + cantidad

            TipoMovimiento.SALIDA ->
                producto.stockActual - cantidad
        }
    }


    // 7. CONFIRMAR SIMULACIÓN

    fun confirmarSimulacion(
        producto: ProductoOperador
    ) {

        val estadoInicial = _uiState.value

        // Evitar confirmaciones simultáneas o repetidas.
        if (operacionBloqueada()) {
            return
        }

        // Comprobar que el operador revisó el formulario.
        if (!estadoInicial.revisando) {

            _uiState.value = estadoInicial.copy(
                errorConfirmacion =
                    "Primero debes revisar el movimiento."
            )

            return
        }

        // Validar nuevamente.
        val resultado = MovimientoValidator.validar(
            tipo = estadoInicial.tipo,
            cantidadTexto = estadoInicial.cantidad,
            motivo = estadoInicial.motivo,
            stockActual = producto.stockActual
        )

        if (!resultado.esValido) {

            _uiState.value = estadoInicial.copy(
                errorCantidad = resultado.errorCantidad,
                errorMotivo = resultado.errorMotivo,
                revisando = false,
                errorConfirmacion =
                    "Corrige los datos antes de confirmar."
            )

            return
        }

        val cantidad = estadoInicial.cantidad.toIntOrNull()

        if (cantidad == null) {

            _uiState.value = estadoInicial.copy(
                revisando = false,
                errorConfirmacion =
                    "La cantidad ingresada no es válida."
            )

            return
        }

        // Mostrar indicador de carga.
        _uiState.value = estadoInicial.copy(
            confirmandoDemo = true,
            errorConfirmacion = null
        )

        // Ejecutar la simulación mediante corrutinas.
        viewModelScope.launch {

            try {

                val comprobante =
                    repository.simularConfirmacion(
                        producto = producto,
                        tipo = estadoInicial.tipo,
                        cantidad = cantidad,
                        motivo = estadoInicial.motivo,
                        referencia = estadoInicial.referencia,
                        operadorId = "OPR-017"
                    )

                // Guardar comprobante ficticio en el estado.
                _uiState.value = _uiState.value.copy(
                    confirmandoDemo = false,
                    comprobanteDemo = comprobante,
                    errorConfirmacion = null
                )

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                _uiState.value = _uiState.value.copy(
                    confirmandoDemo = false,
                    errorConfirmacion =
                        e.message
                            ?: "No fue posible simular la confirmación."
                )
            }
        }
    }


    // 8. COMPROBAR SI LA OPERACIÓN ESTÁ BLOQUEADA

    private fun operacionBloqueada(): Boolean {

        val estado = _uiState.value

        return estado.confirmandoDemo ||
                estado.comprobanteDemo != null
    }


    // 9. REINICIAR FORMULARIO

    fun reiniciarFormulario(
        tipoInicial: TipoMovimiento = TipoMovimiento.ENTRADA
    ) {

        // Evitamos reiniciar mientras se procesa.
        if (_uiState.value.confirmandoDemo) {
            return
        }

        _uiState.value = MovimientoUiState(
            tipo = tipoInicial
        )
    }
}