package com.duoc.appmultitienda.operador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.appmultitienda.core.data.InventarioGateway
import com.duoc.appmultitienda.core.model.*
import com.duoc.appmultitienda.operador.validation.OperadorValidator as V
import com.google.gson.Gson
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MovimientoForm(
    val base: Existencia? = null,
    val tipo: String = "ENTRADA",
    val cantidad: String = "",
    val motivo: String = "",
    val referencia: String = "",
    val errores: Map<String, String> = emptyMap(),
    val ocupado: Boolean = false,
    val bloqueado: Boolean = false,
    val mensaje: String? = null,
    val request: MovimientoRequest? = null,
)

data class ConteoForm(
    val base: Existencia? = null,
    val cantidad: String = "",
    val observacion: String = "",
    val errores: Map<String, String> = emptyMap(),
    val ocupado: Boolean = false,
    val bloqueado: Boolean = false,
    val mensaje: String? = null,
    val request: ConteoRequest? = null,
)

data class BorradorMovimiento(val request: MovimientoRequest, val previo: Int)

data class BorradorConteo(val request: ConteoRequest, val esperado: Int)

data class OperadorUi(
    val codigo: String = "",
    val buscando: Boolean = false,
    val errorBusqueda: String? = null,
    val producto: Existencia? = null,
    val accion: String = "producto",
    val movimiento: MovimientoForm = MovimientoForm(),
    val conteo: ConteoForm = ConteoForm(),
    val reciboMovimiento: Movimiento? = null,
    val reciboConteo: Conteo? = null,
)

class OperadorViewModel(private val repo: InventarioGateway) : ViewModel() {
    private val _ui = MutableStateFlow(OperadorUi())
    val ui = _ui.asStateFlow()
    private val navegacion = Channel<String>(Channel.BUFFERED)
    val eventos = navegacion.receiveAsFlow()
    private val gson = Gson()

    private fun draftKey(kind: String, e: Existencia): String =
        "borrador:${repo.datos.value.usuario?.id}:${e.tiendaId}:${e.producto.sku}:$kind"

    fun accion(accion: String) {
        _ui.update { it.copy(accion = accion, errorBusqueda = null, codigo = "") }
    }

    fun codigo(value: String) {
        if (!_ui.value.buscando) _ui.update { it.copy(codigo = value, errorBusqueda = null) }
    }

    fun buscar(value: String = _ui.value.codigo) {
        if (_ui.value.buscando) return
        val error = V.codigo(value)
        if (error != null) {
            _ui.update { it.copy(errorBusqueda = error) }
            return
        }
        _ui.update { it.copy(buscando = true, codigo = value, errorBusqueda = null) }
        viewModelScope.launch {
            try {
                val e = repo.buscar(value)
                _ui.update { it.copy(producto = e) }
                abrirAccion(e)
            } catch (e: FalloDatos) {
                _ui.update { it.copy(errorBusqueda = e.message) }
                navegacion.send("manual")
            } finally {
                _ui.update { it.copy(buscando = false) }
            }
        }
    }

    private suspend fun abrirAccion(e: Existencia) {
        when (_ui.value.accion) {
            "conteo" -> {
                prepararConteo(e)
                navegacion.send("conteo")
            }
            "ENTRADA",
            "SALIDA" -> {
                prepararMovimiento(e, _ui.value.accion)
                navegacion.send("movimiento")
            }
            else -> navegacion.send("producto")
        }
    }

    fun elegir(e: Existencia) {
        _ui.update { it.copy(producto = e) }
        viewModelScope.launch { abrirAccion(e) }
    }

    fun abrirMovimiento(tipo: String = "ENTRADA") {
        val e = vigente() ?: return
        viewModelScope.launch {
            prepararMovimiento(e, tipo)
            navegacion.send("movimiento")
        }
    }

    fun abrirConteo() {
        val e = vigente() ?: return
        viewModelScope.launch {
            prepararConteo(e)
            navegacion.send("conteo")
        }
    }

    private fun vigente(): Existencia? {
        val selected = _ui.value.producto ?: return null
        return repo.datos.value.snapshot.existencias.firstOrNull {
            it.tiendaId == selected.tiendaId && it.producto.sku == selected.producto.sku
        } ?: selected
    }

    private suspend fun prepararMovimiento(e: Existencia, tipo: String) {
        val pending =
            repo.preferencias.leer(draftKey("movimiento", e))?.let {
                runCatching { gson.fromJson(it, BorradorMovimiento::class.java) }.getOrNull()
            }
        _ui.update {
            it.copy(
                producto = e,
                movimiento =
                    pending?.let { b ->
                        MovimientoForm(
                            base = e.copy(cantidad = b.previo, version = b.request.version),
                            tipo = b.request.tipo,
                            cantidad = b.request.cantidad.toString(),
                            motivo = b.request.motivo,
                            referencia = b.request.referencia,
                            bloqueado = true,
                            mensaje =
                                "Solicitud sin respuesta confirmada. Reintenta con los mismos datos para recuperar su comprobante.",
                            request = b.request,
                        )
                    } ?: MovimientoForm(base = e, tipo = tipo),
            )
        }
    }

    private suspend fun prepararConteo(e: Existencia) {
        val pending =
            repo.preferencias.leer(draftKey("conteo", e))?.let {
                runCatching { gson.fromJson(it, BorradorConteo::class.java) }.getOrNull()
            }
        _ui.update {
            it.copy(
                producto = e,
                conteo =
                    pending?.let { b ->
                        ConteoForm(
                            base = e.copy(cantidad = b.esperado, version = b.request.version),
                            cantidad = b.request.contado.toString(),
                            observacion = b.request.observacion,
                            bloqueado = true,
                            mensaje =
                                "Solicitud pendiente de confirmar. Reintenta sin duplicar el conteo.",
                            request = b.request,
                        )
                    } ?: ConteoForm(base = e),
            )
        }
    }

    fun editarMovimiento(campo: String, value: String) {
        val f = _ui.value.movimiento
        if (f.ocupado || f.bloqueado) return
        var n =
            when (campo) {
                "tipo" -> f.copy(tipo = value)
                "cantidad" -> f.copy(cantidad = value)
                "motivo" -> f.copy(motivo = value)
                else -> f.copy(referencia = value)
            }
        n = n.copy(errores = erroresMovimiento(n, false), mensaje = null)
        _ui.update { it.copy(movimiento = n) }
    }

    private fun erroresMovimiento(f: MovimientoForm, todo: Boolean): Map<String, String> =
        buildMap {
            if (todo || f.cantidad.isNotEmpty())
                V.movimiento(f.cantidad, f.tipo, f.base?.cantidad ?: 0)?.let { put("cantidad", it) }
            if (todo || f.motivo.isNotEmpty()) V.motivo(f.motivo)?.let { put("motivo", it) }
            V.referencia(f.referencia)?.let { put("referencia", it) }
        }

    fun revisarMovimiento() {
        val f = _ui.value.movimiento
        if (f.ocupado) return
        val errors = erroresMovimiento(f, true)
        _ui.update { it.copy(movimiento = f.copy(errores = errors)) }
        if (errors.isEmpty()) viewModelScope.launch { navegacion.send("revision") }
    }

    fun confirmarMovimiento() {
        val f = _ui.value.movimiento
        val e = f.base ?: return
        if (f.ocupado) return
        val errors = erroresMovimiento(f, true)
        if (errors.isNotEmpty()) {
            _ui.update { it.copy(movimiento = f.copy(errores = errors)) }
            return
        }
        val request =
            f.request
                ?: MovimientoRequest(
                    e.tiendaId,
                    e.producto.sku,
                    f.tipo,
                    f.cantidad.toInt(),
                    f.motivo.trim(),
                    f.referencia.trim(),
                    e.version,
                    UUID.randomUUID().toString(),
                )
        _ui.update {
            it.copy(
                movimiento =
                    f.copy(ocupado = true, bloqueado = true, mensaje = null, request = request)
            )
        }
        viewModelScope.launch {
            try {
                repo.preferencias.escribir(
                    draftKey("movimiento", e),
                    gson.toJson(BorradorMovimiento(request, e.cantidad)),
                )
                val receipt = repo.movimiento(request)
                runCatching { repo.preferencias.borrar(draftKey("movimiento", e)) }
                _ui.update {
                    it.copy(
                        reciboMovimiento = receipt,
                        movimiento =
                            it.movimiento.copy(ocupado = false, bloqueado = false, request = null),
                    )
                }
                navegacion.send("comprobante")
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                val failure = ex as? FalloDatos
                val incierto = failure?.incierto ?: true
                if (!incierto) runCatching { repo.preferencias.borrar(draftKey("movimiento", e)) }
                _ui.update {
                    it.copy(
                        movimiento =
                            it.movimiento.copy(
                                ocupado = false,
                                bloqueado = incierto,
                                request = request.takeIf { incierto },
                                base = failure?.vigente ?: e,
                                mensaje = ex.message ?: "No se pudo confirmar. Reintenta.",
                            )
                    )
                }
                if (failure?.vigente != null) navegacion.send("movimiento")
            }
        }
    }

    fun editarConteo(campo: String, value: String) {
        val f = _ui.value.conteo
        if (f.ocupado || f.bloqueado) return
        val n = if (campo == "cantidad") f.copy(cantidad = value) else f.copy(observacion = value)
        _ui.update { it.copy(conteo = n.copy(errores = erroresConteo(n, false), mensaje = null)) }
    }

    private fun erroresConteo(f: ConteoForm, todo: Boolean): Map<String, String> = buildMap {
        if (todo || f.cantidad.isNotEmpty()) V.conteo(f.cantidad)?.let { put("cantidad", it) }
        V.observacion(f.observacion)?.let { put("observacion", it) }
    }

    fun enviarConteo() {
        val f = _ui.value.conteo
        val e = f.base ?: return
        if (f.ocupado) return
        val errors = erroresConteo(f, true)
        if (errors.isNotEmpty()) {
            _ui.update { it.copy(conteo = f.copy(errores = errors)) }
            return
        }
        val request =
            f.request
                ?: ConteoRequest(
                    e.tiendaId,
                    e.producto.sku,
                    f.cantidad.toInt(),
                    f.observacion.trim(),
                    e.version,
                    UUID.randomUUID().toString(),
                )
        _ui.update {
            it.copy(
                conteo = f.copy(ocupado = true, bloqueado = true, mensaje = null, request = request)
            )
        }
        viewModelScope.launch {
            try {
                repo.preferencias.escribir(
                    draftKey("conteo", e),
                    gson.toJson(BorradorConteo(request, e.cantidad)),
                )
                val receipt = repo.contar(request)
                runCatching { repo.preferencias.borrar(draftKey("conteo", e)) }
                _ui.update {
                    it.copy(
                        reciboConteo = receipt,
                        conteo = it.conteo.copy(ocupado = false, bloqueado = false, request = null),
                    )
                }
                navegacion.send("conteo_recibido")
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                val failure = ex as? FalloDatos
                val incierto = failure?.incierto ?: true
                if (!incierto) runCatching { repo.preferencias.borrar(draftKey("conteo", e)) }
                _ui.update {
                    it.copy(
                        conteo =
                            it.conteo.copy(
                                ocupado = false,
                                bloqueado = incierto,
                                request = request.takeIf { incierto },
                                base = failure?.vigente ?: e,
                                mensaje = ex.message ?: "No se pudo confirmar. Reintenta.",
                            )
                    )
                }
            }
        }
    }
}
