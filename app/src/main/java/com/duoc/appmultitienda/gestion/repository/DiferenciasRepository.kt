package com.duoc.appmultitienda.gestion.repository

import com.duoc.appmultitienda.gestion.model.DiferenciaInventario
import com.duoc.appmultitienda.gestion.model.EstadoConteo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface DiferenciasRepository {
    fun observarConteos(): Flow<List<DiferenciaInventario>>
    suspend fun decidir(idConteo: String, aprobar: Boolean, motivo: String, actorId: String): ResultadoDecision
}

sealed interface ResultadoDecision {
    data class Exito(val idConteo: String, val estado: EstadoConteo, val saldoFinal: Int) : ResultadoDecision
    data class Conflicto(val saldoVigente: Int) : ResultadoDecision
    data class Error(val mensaje: String) : ResultadoDecision
}

/** Datos sintéticos solo para la prueba individual de Javier. No es el inventario oficial. */
class FakeDiferenciasRepository : DiferenciasRepository {
    private val mutex = Mutex()
    private val conteos = MutableStateFlow(
        listOf(
            DiferenciaInventario("CNT-1048", "SUC-01", "Tienda Alameda", "CAF-001", "Café molido 500 g", 48, 44, "OPR-017", "2026-10-12 14:41 UTC", "Faltan cuatro unidades en estantería", EstadoConteo.PENDIENTE),
            DiferenciaInventario("CNT-1049", "SUC-01", "Tienda Alameda", "LEC-004", "Leche entera 1 L", 36, 38, "OPR-019", "2026-10-12 14:45 UTC", "Sobrante identificado durante conteo", EstadoConteo.PENDIENTE),
            DiferenciaInventario("CNT-1040", "SUC-02", "Bodega Norte", "ARR-002", "Arroz largo 1 kg", 16, 16, "OPR-021", "2026-10-11 12:10 UTC", "Conteo sin diferencias", EstadoConteo.APROBADO)
        )
    )
    private val saldos = mutableMapOf(
        ("SUC-01" to "CAF-001") to 48,
        ("SUC-01" to "LEC-004") to 36,
        ("SUC-02" to "ARR-002") to 16
    )
    private val auditoria = mutableListOf<String>()

    override fun observarConteos(): Flow<List<DiferenciaInventario>> = conteos.asStateFlow()

    override suspend fun decidir(idConteo: String, aprobar: Boolean, motivo: String, actorId: String): ResultadoDecision = mutex.withLock {
        if (actorId.isBlank() || !actorId.startsWith("ENC-")) return@withLock ResultadoDecision.Error("El perfil no está autorizado")
        if (motivo.trim().length !in 5..200) return@withLock ResultadoDecision.Error("El motivo debe contener entre 5 y 200 caracteres")
        val conteo = conteos.value.firstOrNull { it.idConteo == idConteo }
            ?: return@withLock ResultadoDecision.Error("Conteo inexistente")
        if (conteo.estado != EstadoConteo.PENDIENTE) return@withLock ResultadoDecision.Error("El conteo ya fue resuelto")
        val llave = conteo.idSucursal to conteo.codigoProducto
        val saldoVigente = saldos[llave] ?: return@withLock ResultadoDecision.Error("Producto sin saldo vigente")
        if (aprobar && saldoVigente != conteo.stockOficial) return@withLock ResultadoDecision.Conflicto(saldoVigente)
        val nuevoEstado = if (aprobar) EstadoConteo.APROBADO else EstadoConteo.RECHAZADO
        val saldoFinal = if (aprobar) conteo.stockContado else saldoVigente
        // El cambio en el simulador y el estado se realizan juntos bajo Mutex.
        if (aprobar) saldos[llave] = saldoFinal
        auditoria += "$idConteo|$nuevoEstado|${motivo.trim()}|$actorId|$saldoVigente|$saldoFinal"
        conteos.value = conteos.value.map { if (it.idConteo == idConteo) it.copy(estado = nuevoEstado) else it }
        ResultadoDecision.Exito(idConteo, nuevoEstado, saldoFinal)
    }

    /** Solo para reproducir el conflicto de actualización en una prueba local. */
    suspend fun simularCambioExterno(idSucursal: String, codigoProducto: String, nuevoSaldo: Int) = mutex.withLock {
        require(nuevoSaldo >= 0)
        saldos[idSucursal to codigoProducto] = nuevoSaldo
    }
    fun auditoriaPrueba(): List<String> = auditoria.toList()
}
