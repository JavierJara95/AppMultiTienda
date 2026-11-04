package com.duoc.appmultitienda.operador.model

enum class EstadoConteoFisico {
    BORRADOR,
    PENDIENTE_APROBACION,
    APROBADO,
    RECHAZADO
}

data class ConteoFisicoOperador(

    val id: String,

    val sucursalId: String,

    val codigoProducto: String,

    val nombreProducto: String,

    val stockRegistrado: Int,

    val cantidadContada: Int,

    val observacion: String,

    val operadorId: String,

    val fechaUtc: String,

    val estado: EstadoConteoFisico = EstadoConteoFisico.BORRADOR

) {

    val diferencia: Int
        get() = cantidadContada - stockRegistrado

    val tieneDiferencia: Boolean
        get() = diferencia != 0
}