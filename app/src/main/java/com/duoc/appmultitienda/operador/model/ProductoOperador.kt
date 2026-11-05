package com.duoc.appmultitienda.operador.model

data class ProductoOperador(
    val codigo: String,
    val codigoBarras: String,
    val nombre: String,
    val categoria: String,
    val stockActual: Int,
    val stockMinimo: Int,
    val sucursalId: String
)