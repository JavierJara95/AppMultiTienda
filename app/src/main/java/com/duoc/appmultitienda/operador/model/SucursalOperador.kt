package com.duoc.appmultitienda.operador.model

data class SucursalOperador(
    val id: String,
    val nombre: String,
    val direccion: String,
    val activa: Boolean,
    val asignada: Boolean
)