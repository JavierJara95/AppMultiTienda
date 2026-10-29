package com.duoc.appmultitienda.gestion.model

enum class EstadoConteo { PENDIENTE, APROBADO, RECHAZADO }

data class DiferenciaInventario(
    val idConteo: String,
    val idSucursal: String,
    val nombreSucursal: String,
    val codigoProducto: String,
    val nombreProducto: String,
    val stockOficial: Int,
    val stockContado: Int,
    val operadorId: String,
    val fechaUtc: String,
    val observacion: String,
    val estado: EstadoConteo
) {
    val diferencia: Int get() = stockContado - stockOficial
}
