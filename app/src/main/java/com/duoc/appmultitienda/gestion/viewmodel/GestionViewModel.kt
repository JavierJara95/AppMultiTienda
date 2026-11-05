package com.duoc.appmultitienda.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.appmultitienda.core.data.InventarioGateway
import com.duoc.appmultitienda.core.model.*
import com.duoc.appmultitienda.gestion.export.*
import com.duoc.appmultitienda.gestion.validation.GestionValidator as V
import com.google.gson.Gson
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ResolucionForm(
    val conteo: Conteo? = null,
    val aprobar: Boolean = true,
    val versionRevisada: Long = 0,
    val motivo: String = "",
    val errorMotivo: String? = null,
    val ocupado: Boolean = false,
    val bloqueado: Boolean = false,
    val mensaje: String? = null,
    val conflicto: Existencia? = null,
    val request: ResolverRequest? = null,
)

data class ReposicionForm(
    val base: Existencia? = null,
    val cantidad: String = "",
    val motivo: String = "",
    val destinoId: String = "",
    val errores: Map<String, String> = emptyMap(),
    val ocupado: Boolean = false,
    val bloqueado: Boolean = false,
    val mensaje: String? = null,
    val request: ReposicionRequest? = null,
)

data class HistorialFiltro(
    val texto: String = "",
    val tipo: String = "TODOS",
    val sku: String = "TODOS",
    val desde: String = "",
    val hasta: String = "",
    val actor: String = "",
)

data class GestionUi(
    val resolucion: ResolucionForm = ResolucionForm(),
    val reposicion: ReposicionForm = ReposicionForm(),
    val reciboResolucion: Resolucion? = null,
    val reciboReposicion: Reposicion? = null,
    val historial: HistorialFiltro = HistorialFiltro(),
    val erroresHistorial: Map<String, String> = emptyMap(),
    val reporte: FiltrosReporte = FiltrosReporte(),
    val errorReporte: String? = null,
    val documento: DocumentoCsv? = null,
    val exportacion: String = "INICIAL",
    val archivoGuardado: String = "",
    val sucursalPendiente: Sucursal? = null,
    val cambioRequest: EstadoTiendaRequest? = null,
    val ocupadoSucursal: Boolean = false,
    val mensajeSucursal: String? = null,
)

class GestionViewModel(private val repo: InventarioGateway, private val writer: CsvWriter) :
    ViewModel() {
    private val _ui = MutableStateFlow(GestionUi())
    val ui = _ui.asStateFlow()
    private val nav = Channel<String>(Channel.BUFFERED)
    val eventos = nav.receiveAsFlow()
    private val gson = Gson()
    private val actor
        get() = repo.datos.value.usuario?.id.orEmpty()

    init {
        viewModelScope.launch {
            repo.preferencias.leer("historial:$actor")?.let { saved ->
                runCatching { gson.fromJson(saved, HistorialFiltro::class.java) }
                    .getOrNull()
                    ?.let { f -> historial(f) }
            }
            repo.preferencias.leer("reporte:$actor")?.let { saved ->
                runCatching { gson.fromJson(saved, FiltrosReporte::class.java) }
                    .getOrNull()
                    ?.let { f -> _ui.update { it.copy(reporte = f) } }
            }
        }
    }

    fun historial(filtro: HistorialFiltro) {
        val errores =
            buildMap<String, String> {
                V.fechaOpcional(filtro.desde)?.let { put("desde", it) }
                V.fechaOpcional(filtro.hasta)?.let { put("hasta", it) }
                if (filtro.desde.isNotBlank() && filtro.hasta.isNotBlank() && isEmpty())
                    V.fechas(filtro.desde, filtro.hasta)?.let { put("hasta", it) }
            }
        _ui.update { it.copy(historial = filtro, erroresHistorial = errores) }
        viewModelScope.launch {
            repo.preferencias.escribir("historial:$actor", gson.toJson(filtro))
        }
    }

    fun reporte(filtro: FiltrosReporte) {
        if (_ui.value.exportacion in listOf("PREPARANDO", "ELEGIR", "SELECTOR", "GUARDANDO")) return
        _ui.update {
            it.copy(
                reporte = filtro,
                errorReporte = V.fechas(filtro.desde, filtro.hasta),
                exportacion = "INICIAL",
            )
        }
        viewModelScope.launch { repo.preferencias.escribir("reporte:$actor", gson.toJson(filtro)) }
    }

    fun revisar(c: Conteo, aprobar: Boolean) =
        viewModelScope.launch {
            val pending =
                repo.preferencias.leer("resolver:$actor:${c.id}")?.let {
                    runCatching { gson.fromJson(it, ResolverRequest::class.java) }.getOrNull()
                }
            _ui.update {
                it.copy(
                    resolucion =
                        pending?.let { r ->
                            ResolucionForm(
                                c,
                                r.aprobar,
                                r.version,
                                r.motivo,
                                bloqueado = true,
                                request = r,
                                mensaje =
                                    "Reintenta la decisión pendiente para recuperar su resultado.",
                            )
                        } ?: ResolucionForm(c, aprobar, c.version)
                )
            }
            nav.send("resolver")
        }

    fun motivo(value: String) {
        val f = _ui.value.resolucion
        if (f.ocupado || f.bloqueado) return
        _ui.update {
            it.copy(
                resolucion = f.copy(motivo = value, errorMotivo = V.motivo(value), mensaje = null)
            )
        }
    }

    fun resolver() {
        val f = _ui.value.resolucion
        val c = f.conteo ?: return
        if (f.ocupado) return
        val error = V.motivo(f.motivo)
        if (error != null) {
            _ui.update { it.copy(resolucion = f.copy(errorMotivo = error)) }
            return
        }
        val req =
            f.request
                ?: ResolverRequest(
                    f.aprobar,
                    f.motivo.trim(),
                    f.versionRevisada,
                    UUID.randomUUID().toString(),
                )
        _ui.update {
            it.copy(
                resolucion = f.copy(ocupado = true, bloqueado = true, mensaje = null, request = req)
            )
        }
        viewModelScope.launch {
            val key = "resolver:$actor:${c.id}"
            try {
                repo.preferencias.escribir(key, gson.toJson(req))
                val result = repo.resolver(c.id, req)
                runCatching { repo.preferencias.borrar(key) }
                _ui.update {
                    it.copy(
                        reciboResolucion = result,
                        resolucion =
                            it.resolucion.copy(ocupado = false, bloqueado = false, request = null),
                    )
                }
                nav.send("resolucion_recibida")
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                val fallo = ex as? FalloDatos
                val incierto = fallo?.incierto ?: true
                if (!incierto) runCatching { repo.preferencias.borrar(key) }
                _ui.update {
                    it.copy(
                        resolucion =
                            it.resolucion.copy(
                                ocupado = false,
                                bloqueado = incierto,
                                request = req.takeIf { incierto },
                                mensaje = ex.message,
                                conflicto = fallo?.vigente,
                            )
                    )
                }
                if (fallo?.vigente != null) nav.send("conflicto")
            }
        }
    }

    fun revisarSaldoNuevo() {
        if (_ui.value.resolucion.ocupado) return
        _ui.update { it.copy(resolucion = it.resolucion.copy(ocupado = true)) }
        viewModelScope.launch {
            try {
                repo.refrescar()
                val f = _ui.value.resolucion
                val c = f.conteo ?: return@launch
                val actual =
                    repo.datos.value.snapshot.existencias.first {
                        it.tiendaId == c.tiendaId && it.producto.sku == c.sku
                    }
                val conteoActual = repo.datos.value.snapshot.conteos.first { it.id == c.id }
                if (conteoActual.estado != "PENDIENTE")
                    throw FalloDatos("Otro encargado ya resolvió este conteo. Vuelve a la bandeja.")
                _ui.update {
                    it.copy(
                        resolucion =
                            f.copy(
                                versionRevisada = actual.version,
                                conflicto = null,
                                request = null,
                                bloqueado = false,
                                ocupado = false,
                                mensaje =
                                    "Saldo vigente consultado: ${actual.cantidad} uds. Revisa el ajuste antes de aprobar.",
                            )
                    )
                }
                nav.send("resolver")
            } catch (e: FalloDatos) {
                _ui.update {
                    it.copy(resolucion = it.resolucion.copy(ocupado = false, mensaje = e.message))
                }
            }
        }
    }

    fun abrirReposicion(e: Existencia) =
        viewModelScope.launch {
            val pending =
                repo.preferencias.leer("reponer:$actor:${e.tiendaId}:${e.producto.sku}")?.let {
                    runCatching { gson.fromJson(it, ReposicionRequest::class.java) }.getOrNull()
                }
            val destino =
                repo.datos.value.snapshot.sucursales
                    .firstOrNull { it.activa && it.id != e.tiendaId }
                    ?.id
                    .orEmpty()
            _ui.update {
                it.copy(
                    reposicion =
                        pending?.let { r ->
                            ReposicionForm(
                                e,
                                r.cantidad.toString(),
                                r.motivo,
                                r.destinoId,
                                bloqueado = true,
                                request = r,
                                mensaje =
                                    "Solicitud pendiente de confirmación. Reintenta con los mismos datos.",
                            )
                        } ?: ReposicionForm(e, destinoId = destino)
                )
            }
            nav.send("reposicion")
        }

    fun editarReposicion(campo: String, value: String) {
        val f = _ui.value.reposicion
        if (f.ocupado || f.bloqueado) return
        val n =
            when (campo) {
                "cantidad" -> f.copy(cantidad = value)
                "motivo" -> f.copy(motivo = value)
                else -> f.copy(destinoId = value)
            }
        _ui.update {
            it.copy(reposicion = n.copy(errores = erroresReposicion(n, false), mensaje = null))
        }
    }

    private fun erroresReposicion(f: ReposicionForm, todo: Boolean): Map<String, String> =
        buildMap {
            if (todo || f.cantidad.isNotEmpty()) V.cantidad(f.cantidad)?.let { put("cantidad", it) }
            if (todo || f.motivo.isNotEmpty()) V.motivo(f.motivo)?.let { put("motivo", it) }
            if (f.destinoId.isBlank()) put("destino", "Selecciona una ubicación sugerida.")
        }

    fun reponer() {
        val f = _ui.value.reposicion
        val e = f.base ?: return
        if (f.ocupado) return
        val errors = erroresReposicion(f, true)
        if (errors.isNotEmpty()) {
            _ui.update { it.copy(reposicion = f.copy(errores = errors)) }
            return
        }
        val req =
            f.request
                ?: ReposicionRequest(
                    e.tiendaId,
                    e.producto.sku,
                    f.cantidad.toInt(),
                    f.motivo.trim(),
                    f.destinoId,
                    UUID.randomUUID().toString(),
                )
        _ui.update {
            it.copy(
                reposicion = f.copy(ocupado = true, bloqueado = true, request = req, mensaje = null)
            )
        }
        viewModelScope.launch {
            val key = "reponer:$actor:${e.tiendaId}:${e.producto.sku}"
            try {
                repo.preferencias.escribir(key, gson.toJson(req))
                val recibo = repo.reponer(req)
                runCatching { repo.preferencias.borrar(key) }
                _ui.update {
                    it.copy(
                        reciboReposicion = recibo,
                        reposicion =
                            it.reposicion.copy(ocupado = false, bloqueado = false, request = null),
                    )
                }
                nav.send("reposicion_recibida")
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                val incierto = (ex as? FalloDatos)?.incierto ?: true
                if (!incierto) runCatching { repo.preferencias.borrar(key) }
                _ui.update {
                    it.copy(
                        reposicion =
                            it.reposicion.copy(
                                ocupado = false,
                                bloqueado = incierto,
                                request = req.takeIf { incierto },
                                mensaje = ex.message,
                            )
                    )
                }
            }
        }
    }

    fun prepararCsv() {
        if (_ui.value.exportacion in listOf("PREPARANDO", "ELEGIR", "SELECTOR", "GUARDANDO")) return
        val f = _ui.value.reporte
        val error = V.fechas(f.desde, f.hasta)
        if (error != null) {
            _ui.update { it.copy(errorReporte = error) }
            return
        }
        _ui.update { it.copy(exportacion = "PREPARANDO", errorReporte = null) }
        viewModelScope.launch {
            try {
                repo.refrescar()
                val document = CsvExporter.generar(repo.datos.value.snapshot, f)
                _ui.update { it.copy(documento = document, exportacion = "ELEGIR") }
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                _ui.update {
                    it.copy(
                        errorReporte = ex.message ?: "No se pudo preparar el reporte.",
                        exportacion = "ERROR",
                    )
                }
            }
        }
    }

    fun falloSelector(mensaje: String) {
        _ui.update { it.copy(exportacion = "ERROR", errorReporte = mensaje) }
    }

    fun selectorAbierto() {
        _ui.update { it.copy(exportacion = "SELECTOR") }
    }

    fun resultadoUri(uri: String?) {
        if (uri == null) {
            _ui.update { it.copy(exportacion = "CANCELADO") }
            viewModelScope.launch { nav.send("exportacion_resultado") }
            return
        }
        val doc =
            _ui.value.documento
                ?: run {
                    _ui.update {
                        it.copy(
                            errorReporte = "Genera el reporte nuevamente antes de guardar.",
                            exportacion = "ERROR",
                        )
                    }
                    return
                }
        _ui.update { it.copy(exportacion = "GUARDANDO") }
        viewModelScope.launch {
            try {
                val nombre = writer.guardar(uri, doc)
                _ui.update { it.copy(exportacion = "EXITO", archivoGuardado = nombre) }
                nav.send("exportacion_resultado")
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                _ui.update {
                    it.copy(
                        exportacion = "ERROR",
                        errorReporte = ex.message ?: "No se pudo guardar el archivo.",
                    )
                }
            }
        }
    }

    fun proponerSucursal(s: Sucursal) {
        if (_ui.value.ocupadoSucursal) return
        viewModelScope.launch {
            val request =
                repo.preferencias.leer("sucursal:$actor:${s.id}")?.let {
                    runCatching { gson.fromJson(it, EstadoTiendaRequest::class.java) }.getOrNull()
                } ?: EstadoTiendaRequest(!s.activa, UUID.randomUUID().toString())
            _ui.update {
                it.copy(sucursalPendiente = s, cambioRequest = request, mensajeSucursal = null)
            }
        }
    }

    fun cerrarSucursal() {
        if (!_ui.value.ocupadoSucursal) _ui.update { it.copy(sucursalPendiente = null) }
    }

    fun cambiarSucursal() {
        val s = _ui.value.sucursalPendiente ?: return
        val r = _ui.value.cambioRequest ?: return
        if (_ui.value.ocupadoSucursal) return
        _ui.update { it.copy(ocupadoSucursal = true, mensajeSucursal = null) }
        viewModelScope.launch {
            val key = "sucursal:$actor:${s.id}"
            try {
                repo.preferencias.escribir(key, gson.toJson(r))
                repo.cambiarSucursal(s.id, r)
                runCatching { repo.preferencias.borrar(key) }
                _ui.update {
                    it.copy(sucursalPendiente = null, ocupadoSucursal = false, cambioRequest = null)
                }
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                if ((ex as? FalloDatos)?.incierto == false)
                    runCatching { repo.preferencias.borrar(key) }
                _ui.update { it.copy(ocupadoSucursal = false, mensajeSucursal = ex.message) }
            }
        }
    }
}
