package com.duoc.appmultitienda.operador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.duoc.appmultitienda.operador.model.ConteoFisicoOperador
import com.duoc.appmultitienda.operador.model.EstadoConteoFisico
import com.duoc.appmultitienda.operador.model.ProductoOperador

import com.duoc.appmultitienda.operador.repository.ConteoFisicoRepository
import com.duoc.appmultitienda.operador.repository.FakeConteoFisicoRepository

import com.duoc.appmultitienda.operador.validation.ConteoFisicoValidator

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


// 1. DATOS QUE CONTROLA EL VIEWMODEL

data class ConteoFisicoUiState(

    val cantidadTexto: String = "",

    val observacion: String = "",

    val errorCantidad: String? = null,

    val errorObservacion: String? = null,

    val diferencia: Int? = null,

    val conteoRevisado: Boolean = false,

    val enviando: Boolean = false,

    val errorEnvio: String? = null,

    val conteoRegistrado: ConteoFisicoOperador? = null
)


// 2. VIEWMODEL DEL CONTEO FÍSICO

class ConteoFisicoViewModel(

    private val repository: ConteoFisicoRepository =
        FakeConteoFisicoRepository()

) : ViewModel() {


    private val _uiState = MutableStateFlow(
        ConteoFisicoUiState()
    )

    val uiState: StateFlow<ConteoFisicoUiState> =
        _uiState.asStateFlow()


    // 3. COMPROBAR SI EL FORMULARIO ESTÁ BLOQUEADO

    private fun formularioBloqueado(): Boolean {

        val estado = _uiState.value

        return estado.enviando ||
                estado.conteoRegistrado != null
    }


    // 4. ACTUALIZAR LA CANTIDAD CONTADA

    fun actualizarCantidad(texto: String) {

        if (formularioBloqueado()) return

        _uiState.value = _uiState.value.copy(

            cantidadTexto = texto,

            errorCantidad = null,

            diferencia = null,

            conteoRevisado = false,

            errorEnvio = null
        )
    }


    // 5. ACTUALIZAR LA OBSERVACIÓN

    fun actualizarObservacion(texto: String) {

        if (formularioBloqueado()) return

        _uiState.value = _uiState.value.copy(

            observacion = texto,

            errorObservacion = null,

            diferencia = null,

            conteoRevisado = false,

            errorEnvio = null
        )
    }


    // 6. REVISAR EL CONTEO

    fun revisarConteo(producto: ProductoOperador) {

        if (formularioBloqueado()) return

        val estadoActual = _uiState.value

        val resultado = ConteoFisicoValidator.validar(

            cantidadTexto = estadoActual.cantidadTexto,

            stockRegistrado = producto.stockActual,

            observacion = estadoActual.observacion
        )

        if (!resultado.esValido) {

            _uiState.value = estadoActual.copy(

                errorCantidad = resultado.errorCantidad,

                errorObservacion = resultado.errorObservacion,

                diferencia = null,

                conteoRevisado = false
            )

            return
        }

        val cantidadContada =
            estadoActual.cantidadTexto.toIntOrNull()

        if (cantidadContada == null) {

            _uiState.value = estadoActual.copy(

                errorCantidad =
                    "La cantidad ingresada no es válida.",

                diferencia = null,

                conteoRevisado = false
            )

            return
        }

        val diferenciaCalculada =
            cantidadContada - producto.stockActual

        _uiState.value = estadoActual.copy(

            errorCantidad = null,

            errorObservacion = null,

            diferencia = diferenciaCalculada,

            conteoRevisado = true,

            errorEnvio = null
        )
    }


    // 7. CONSULTAR SI EXISTE UNA DIFERENCIA

    fun tieneDiferencia(): Boolean {

        val estado = _uiState.value

        return estado.conteoRevisado &&
                estado.diferencia != null &&
                estado.diferencia != 0
    }


    // 8. ENVIAR CONTEO A APROBACIÓN SIMULADA

    fun enviarAprobacion(
        producto: ProductoOperador,
        operadorId: String
    ) {

        val estadoInicial = _uiState.value

        // Evitamos enviar dos veces el mismo formulario.

        if (formularioBloqueado()) {
            return
        }

        // Primero debe haberse revisado el conteo.

        if (!estadoInicial.conteoRevisado) {

            _uiState.value = estadoInicial.copy(
                errorEnvio =
                    "Primero debes revisar el conteo."
            )

            return
        }

        // Validamos nuevamente los datos.

        val resultado = ConteoFisicoValidator.validar(

            cantidadTexto = estadoInicial.cantidadTexto,

            stockRegistrado = producto.stockActual,

            observacion = estadoInicial.observacion
        )

        if (!resultado.esValido) {

            _uiState.value = estadoInicial.copy(

                errorCantidad = resultado.errorCantidad,

                errorObservacion = resultado.errorObservacion,

                conteoRevisado = false,

                diferencia = null,

                errorEnvio =
                    "Corrige los datos antes de continuar."
            )

            return
        }

        val cantidadContada =
            estadoInicial.cantidadTexto.toIntOrNull()

        if (cantidadContada == null) {

            _uiState.value = estadoInicial.copy(

                errorCantidad =
                    "La cantidad ingresada no es válida.",

                conteoRevisado = false,

                errorEnvio =
                    "Debes ingresar una cantidad válida."
            )

            return
        }

        // Construimos una solicitud de conteo.

        val solicitud = ConteoFisicoOperador(

            id = "",

            sucursalId = producto.sucursalId,

            codigoProducto = producto.codigo,

            nombreProducto = producto.nombre,

            stockRegistrado = producto.stockActual,

            cantidadContada = cantidadContada,

            observacion = estadoInicial.observacion.trim(),

            operadorId = operadorId,

            fechaUtc = "",

            estado = EstadoConteoFisico.BORRADOR
        )

        // Informamos que comenzó el envío.

        _uiState.value = estadoInicial.copy(

            enviando = true,

            errorEnvio = null
        )

        // Ejecutamos el repositorio mediante una corrutina.

        viewModelScope.launch {

            try {

                val conteoGuardado =
                    repository.registrarConteo(solicitud)

                _uiState.value = _uiState.value.copy(

                    enviando = false,

                    conteoRegistrado = conteoGuardado,

                    errorEnvio = null
                )

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                _uiState.value = _uiState.value.copy(

                    enviando = false,

                    errorEnvio =
                        e.message
                            ?: "No fue posible registrar el conteo."
                )
            }
        }
    }


    // 9. LIMPIAR EL FORMULARIO

    fun reiniciarConteo() {

        if (_uiState.value.enviando) {
            return
        }

        _uiState.value = ConteoFisicoUiState()
    }
}