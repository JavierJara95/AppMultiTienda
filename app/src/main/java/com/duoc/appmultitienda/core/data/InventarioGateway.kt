package com.duoc.appmultitienda.core.data

import com.duoc.appmultitienda.core.model.*
import kotlinx.coroutines.flow.StateFlow

interface InventarioGateway {
    val preferencias: Preferencias
    val datos: StateFlow<Datos>

    suspend fun login(usuario: String, clave: String)

    suspend fun logout()

    suspend fun refrescar()

    suspend fun seleccionar(id: String)

    suspend fun buscar(codigo: String): Existencia

    suspend fun movimiento(request: MovimientoRequest): Movimiento

    suspend fun contar(request: ConteoRequest): Conteo

    suspend fun resolver(id: String, request: ResolverRequest): Resolucion

    suspend fun reponer(request: ReposicionRequest): Reposicion

    suspend fun cambiarSucursal(id: String, request: EstadoTiendaRequest): Sucursal

    /** Exclusivo de ADMIN. Se persiste en el Snapshot de Room existente. */
    suspend fun guardarProducto(request: CatalogoProductoRequest, esNuevo: Boolean): Producto

    suspend fun cambiarEstadoProducto(sku: String, activo: Boolean): Producto
}

