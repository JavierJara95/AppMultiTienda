package com.duoc.appmultitienda.gestion.repository

import com.duoc.appmultitienda.gestion.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface HistorialAlertasRepository {
    fun observarMovimientos(): Flow<List<MovimientoAuditoria>>
    fun observarAlertas(): Flow<List<AlertaStock>>
}

/** Datos de demostración aislados. No implican sincronización con Esteban ni con la API. */
class FakeHistorialAlertasRepository : HistorialAlertasRepository {
    private val movimientos = MutableStateFlow(listOf(
        MovimientoAuditoria("AJ-2048", "SUC-01", "Tienda Alameda", "CAF-001", "Café molido 500 g", TipoMovimiento.AJUSTE, -4, 44, "ENC-004", "2026-10-12 14:52 UTC", "Merma verificada en recuento"),
        MovimientoAuditoria("MOV-2039", "SUC-01", "Tienda Alameda", "ARR-002", "Arroz largo 1 kg", TipoMovimiento.ENTRADA, 6, 22, "OPR-017", "2026-10-12 14:30 UTC", "Recepción de proveedor"),
        MovimientoAuditoria("MOV-2038", "SUC-01", "Tienda Alameda", "ACE-003", "Aceite vegetal 1 L", TipoMovimiento.SALIDA, -8, 0, "OPR-019", "2026-10-12 14:20 UTC", "Venta mostrador"),
        MovimientoAuditoria("MOV-2037", "SUC-02", "Bodega Norte", "GAL-005", "Galletas 200 g", TipoMovimiento.ENTRADA, 10, 35, "OPR-021", "2026-10-11 10:15 UTC", "Recepción de proveedor")
    ))
    private val alertas = MutableStateFlow(listOf(
        AlertaStock("ACE-003", "Aceite vegetal 1 L", "SUC-01", "Tienda Alameda", 0, 8),
        AlertaStock("ARR-002", "Arroz largo 1 kg", "SUC-01", "Tienda Alameda", 22, 24),
        AlertaStock("GAL-005", "Galletas de avena 200 g", "SUC-01", "Tienda Alameda", 6, 10),
        AlertaStock("CAF-001", "Café molido 500 g", "SUC-02", "Bodega Norte", 40, 12)
    ))
    override fun observarMovimientos(): Flow<List<MovimientoAuditoria>> = movimientos.asStateFlow()
    override fun observarAlertas(): Flow<List<AlertaStock>> = alertas.asStateFlow()
}
