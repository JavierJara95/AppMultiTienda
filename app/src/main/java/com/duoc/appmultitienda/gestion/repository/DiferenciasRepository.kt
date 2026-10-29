package com.duoc.appmultitienda.gestion.repository

import com.duoc.appmultitienda.gestion.model.DiferenciaInventario
import com.duoc.appmultitienda.gestion.model.EstadoConteo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface DiferenciasRepository {
    fun observarConteos(): Flow<List<DiferenciaInventario>>
}

// Datos sintéticos. En la integración se reemplaza por API REST, sin alterar la pantalla.
class FakeDiferenciasRepository : DiferenciasRepository {
    private val conteos = MutableStateFlow(
        listOf(
            DiferenciaInventario("CNT-1048", "SUC-01", "Tienda Alameda", "CAF-001", "Café molido 500 g", 48, 44, "OPR-017", "2026-10-12 14:41 UTC", "Faltan cuatro unidades en estantería", EstadoConteo.PENDIENTE),
            DiferenciaInventario("CNT-1049", "SUC-01", "Tienda Alameda", "LEC-004", "Leche entera 1 L", 36, 38, "OPR-019", "2026-10-12 14:45 UTC", "Sobrante identificado durante conteo", EstadoConteo.PENDIENTE),
            DiferenciaInventario("CNT-1040", "SUC-02", "Bodega Norte", "ARR-002", "Arroz largo 1 kg", 16, 16, "OPR-021", "2026-10-11 12:10 UTC", "Conteo sin diferencias", EstadoConteo.APROBADO)
        )
    )
    override fun observarConteos(): Flow<List<DiferenciaInventario>> = conteos
}
