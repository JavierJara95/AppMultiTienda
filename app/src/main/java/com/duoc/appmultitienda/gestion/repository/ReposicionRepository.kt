package com.duoc.appmultitienda.gestion.repository

import com.duoc.appmultitienda.gestion.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface ReposicionRepository {
    fun observarSolicitudes(): Flow<List<SolicitudReposicion>>
    suspend fun solicitar(alerta: AlertaStock, cantidad: Int, motivo: String, actorId: String): Result<SolicitudReposicion>
}

/** Repositorio en memoria para pruebas del módulo de Javier. No realiza compras ni modifica stock. */
class FakeReposicionRepository : ReposicionRepository {
    private val lock = Mutex()
    private val solicitudes = MutableStateFlow<List<SolicitudReposicion>>(emptyList())
    override fun observarSolicitudes(): Flow<List<SolicitudReposicion>> = solicitudes.asStateFlow()

    override suspend fun solicitar(alerta: AlertaStock, cantidad: Int, motivo: String, actorId: String): Result<SolicitudReposicion> = lock.withLock {
        val error = ReposicionValidator.validar(cantidad.toString(), motivo)
        if (error != null) return@withLock Result.failure(IllegalArgumentException(error))
        if (alerta.estado == EstadoStock.NORMAL) return@withLock Result.failure(IllegalStateException("El producto no necesita reposición"))
        val nueva = SolicitudReposicion(
            id = "REP-${(solicitudes.value.size + 1).toString().padStart(3, '0')}",
            productoCodigo = alerta.productoCodigo,
            productoNombre = alerta.productoNombre,
            sucursalId = alerta.sucursalId,
            sucursalNombre = alerta.sucursalNombre,
            cantidad = cantidad,
            motivo = motivo.trim(),
            solicitanteId = actorId,
            fechaUtc = java.time.Instant.now().toString()
        )
        solicitudes.value = solicitudes.value + nueva
        Result.success(nueva)
    }
}
