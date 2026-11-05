package com.duoc.appmultitienda.gestion.model

enum class TipoMovimiento { ENTRADA, SALIDA, AJUSTE }
enum class EstadoStock { NORMAL, BAJO, QUIEBRE }

data class MovimientoAuditoria(
    val id: String,
    val sucursalId: String,
    val sucursalNombre: String,
    val productoCodigo: String,
    val productoNombre: String,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val saldoFinal: Int,
    val actorId: String,
    val fechaUtc: String,
    val motivo: String
) {
    init { require(cantidad != 0); require(saldoFinal >= 0) }
}

data class AlertaStock(
    val productoCodigo: String,
    val productoNombre: String,
    val sucursalId: String,
    val sucursalNombre: String,
    val stockActual: Int,
    val stockMinimo: Int
) {
    init { require(stockActual >= 0 && stockMinimo >= 0) }
    val estado: EstadoStock get() = when {
        stockActual == 0 -> EstadoStock.QUIEBRE
        stockActual <= stockMinimo -> EstadoStock.BAJO
        else -> EstadoStock.NORMAL
    }
}
