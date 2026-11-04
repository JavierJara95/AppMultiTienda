package com.duoc.appmultitienda.gestion.repository

import com.duoc.appmultitienda.gestion.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

/** Demostración EN MEMORIA: una fuente coherente para diferencias, auditoría y CSV.
 * No sustituye la API REST, ni persiste decisiones después de reiniciar el proceso.
 */
class FuenteGestionCompartida : DiferenciasRepository, HistorialAlertasRepository, ReportesSucursalesRepository {
    private val mutex = Mutex()
    private val conteos = MutableStateFlow(listOf(
        DiferenciaInventario("CNT-1048", "SUC-01", "Tienda Alameda", "CAF-001", "Café molido 500 g", 48, 44, "OPR-017", "2026-10-12 14:41 UTC", "Faltan cuatro unidades", EstadoConteo.PENDIENTE),
        DiferenciaInventario("CNT-1049", "SUC-01", "Tienda Alameda", "LEC-004", "Leche entera 1 L", 36, 38, "OPR-019", "2026-10-12 14:45 UTC", "Sobrante de dos unidades", EstadoConteo.PENDIENTE),
        DiferenciaInventario("CNT-1040", "SUC-02", "Bodega Norte", "ARR-002", "Arroz largo 1 kg", 16, 16, "OPR-021", "2026-10-11 12:10 UTC", "Sin diferencias", EstadoConteo.APROBADO)
    ))
    private val saldos = mutableMapOf(("SUC-01" to "CAF-001") to 48, ("SUC-01" to "LEC-004") to 36)
    private val movimientos = MutableStateFlow(listOf(
        MovimientoAuditoria("MOV-2039", "SUC-01", "Tienda Alameda", "ARR-002", "Arroz largo 1 kg", TipoMovimiento.ENTRADA, 6, 22, "OPR-017", "2026-10-12T14:30:00Z", "Recepción de proveedor"),
        MovimientoAuditoria("MOV-2038", "SUC-01", "Tienda Alameda", "ACE-003", "Aceite vegetal 1 L", TipoMovimiento.SALIDA, -8, 0, "OPR-019", "2026-10-12T14:20:00Z", "Venta mostrador"),
        MovimientoAuditoria("MOV-2037", "SUC-02", "Bodega Norte", "GAL-005", "Galletas 200 g", TipoMovimiento.ENTRADA, 10, 35, "OPR-021", "2026-10-11T10:15:00Z", "Recepción de proveedor")
    ))
    private val alertas = MutableStateFlow(listOf(
        AlertaStock("CAF-001", "Café molido 500 g", "SUC-01", "Tienda Alameda", 48, 12),
        AlertaStock("ACE-003", "Aceite vegetal 1 L", "SUC-01", "Tienda Alameda", 0, 8),
        AlertaStock("ARR-002", "Arroz largo 1 kg", "SUC-01", "Tienda Alameda", 22, 24),
        AlertaStock("GAL-005", "Galletas de avena 200 g", "SUC-01", "Tienda Alameda", 6, 10),
        AlertaStock("LEC-004", "Leche entera 1 L", "SUC-01", "Tienda Alameda", 36, 12)
    ))
    private val _sucursales = MutableStateFlow(listOf(
        SucursalGestion("SUC-01", "Tienda Alameda", "Av. Alameda 120", true),
        SucursalGestion("SUC-02", "Bodega Norte", "Parque Industrial 45", true),
        SucursalGestion("SUC-03", "Tienda Mirador", "Calle Mirador 86", false)
    ))
    override val sucursales: StateFlow<List<SucursalGestion>> = _sucursales
    override fun observarConteos(): Flow<List<DiferenciaInventario>> = conteos
    override fun observarMovimientos(): Flow<List<MovimientoAuditoria>> = movimientos
    override fun observarAlertas(): Flow<List<AlertaStock>> = alertas

    override suspend fun decidir(idConteo: String, aprobar: Boolean, motivo: String, actorId: String): ResultadoDecision = mutex.withLock {
        if (!(actorId.startsWith("ENC-") || actorId.startsWith("ADM-"))) return@withLock ResultadoDecision.Error("Solo Encargado o Admin pueden decidir")
        if (motivo.trim().length !in 5..200) return@withLock ResultadoDecision.Error("Motivo entre 5 y 200 caracteres")
        val conteo = conteos.value.firstOrNull { it.idConteo == idConteo } ?: return@withLock ResultadoDecision.Error("Conteo inexistente")
        if (conteo.estado != EstadoConteo.PENDIENTE) return@withLock ResultadoDecision.Error("Conteo ya resuelto")
        val llave = conteo.idSucursal to conteo.codigoProducto
        val saldoVigente = saldos[llave] ?: return@withLock ResultadoDecision.Error("Sin stock vigente")
        if (aprobar && saldoVigente != conteo.stockOficial) return@withLock ResultadoDecision.Conflicto(saldoVigente)
        val nuevoSaldo = if (aprobar) conteo.stockContado else saldoVigente
        val nuevoEstado = if (aprobar) EstadoConteo.APROBADO else EstadoConteo.RECHAZADO
        if (aprobar) {
            saldos[llave] = nuevoSaldo
            val id = "AJ-${idConteo.removePrefix("CNT-")}"
            movimientos.value = listOf(MovimientoAuditoria(
                id, conteo.idSucursal, conteo.nombreSucursal, conteo.codigoProducto, conteo.nombreProducto,
                TipoMovimiento.AJUSTE, nuevoSaldo - saldoVigente, nuevoSaldo,
                actorId, Instant.now().toString(), motivo.trim()
            )) .filter { it.cantidad != 0 } + movimientos.value
            alertas.value = alertas.value.map {
                if (it.sucursalId == conteo.idSucursal && it.productoCodigo == conteo.codigoProducto)
                    it.copy(stockActual = nuevoSaldo) else it
            }
        }
        conteos.value = conteos.value.map { if (it.idConteo == idConteo) it.copy(estado = nuevoEstado) else it }
        ResultadoDecision.Exito(idConteo, nuevoEstado, nuevoSaldo)
    }

    override suspend fun cambiarEstado(id: String, activa: Boolean): Boolean = mutex.withLock {
        if (_sucursales.value.none { it.id == id }) return@withLock false
        _sucursales.value = _sucursales.value.map { if (it.id == id) it.copy(activa = activa) else it }
        true
    }
    override suspend fun obtenerMovimientos(sucursalId: String?): List<FilaReporte> =
        movimientos.value.filter { sucursalId == null || it.sucursalId == sucursalId }
            .map { FilaReporte(it.id, it.sucursalId, it.productoNombre, it.tipo.name, it.cantidad, it.fechaUtc) }

    /** Solo permite reproducir conflictos durante pruebas unitarias. */
    suspend fun simularCambioExterno(sucursalId: String, productoId: String, nuevoSaldo: Int) = mutex.withLock {
        require(nuevoSaldo >= 0)
        saldos[sucursalId to productoId] = nuevoSaldo
    }
}
