package com.duoc.appmultitienda.gestion.repository

import com.duoc.appmultitienda.gestion.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ReportesSucursalesRepository {
    val sucursales: StateFlow<List<SucursalGestion>>
    suspend fun cambiarEstado(id: String, activa: Boolean): Boolean
    suspend fun obtenerMovimientos(sucursalId: String?): List<FilaReporte>
}

class FakeReportesSucursalesRepository : ReportesSucursalesRepository {
    private val _sucursales = MutableStateFlow(listOf(
        SucursalGestion("SUC-01", "Tienda Alameda", "Av. Alameda 120", true),
        SucursalGestion("SUC-02", "Bodega Norte", "Parque Industrial 45", true),
        SucursalGestion("SUC-03", "Tienda Mirador", "Calle Mirador 86", false)
    ))
    override val sucursales: StateFlow<List<SucursalGestion>> = _sucursales
    private val movimientos = listOf(
        FilaReporte("MOV-2039", "SUC-01", "Arroz largo 1 kg", "ENTRADA", 6, "2026-10-12T14:30:00Z"),
        FilaReporte("MOV-2038", "SUC-01", "Aceite vegetal 1 L", "SALIDA", -8, "2026-10-12T14:20:00Z"),
        FilaReporte("AJ-2048", "SUC-01", "Cafe molido 500 g", "AJUSTE", -4, "2026-10-12T14:52:00Z"),
        FilaReporte("MOV-2040", "SUC-02", "Leche entera 1 L", "ENTRADA", 12, "2026-10-12T15:00:00Z")
    )
    override suspend fun cambiarEstado(id: String, activa: Boolean): Boolean {
        if (_sucursales.value.none { it.id == id }) return false
        _sucursales.value = _sucursales.value.map { if (it.id == id) it.copy(activa = activa) else it }
        return true
    }
    override suspend fun obtenerMovimientos(sucursalId: String?) = movimientos.filter { sucursalId == null || it.sucursalId == sucursalId }
}
