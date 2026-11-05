package com.duoc.appmultitienda.operador.model

import com.duoc.appmultitienda.operador.validation.TipoMovimiento

data class ComprobanteMovimientoDemo(

    val idDemostracion: String,

    val sucursalId: String,

    val codigoProducto: String,

    val nombreProducto: String,

    val tipo: TipoMovimiento,

    val cantidad: Int,

    val stockAnterior: Int,

    val stockPrevisto: Int,

    val motivo: String,

    val referencia: String,

    val operadorId: String,

    val fechaUtc: String

)