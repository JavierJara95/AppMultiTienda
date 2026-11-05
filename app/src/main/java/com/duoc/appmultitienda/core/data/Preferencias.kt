package com.duoc.appmultitienda.core.data

interface Preferencias {
    suspend fun leer(clave: String): String?
    suspend fun escribir(clave: String, valor: String)
    suspend fun borrar(clave: String)
}
