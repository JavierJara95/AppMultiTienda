package com.duoc.appmultitienda.gestion.viewmodel

import com.duoc.appmultitienda.gestion.model.*
import org.junit.Assert.assertEquals
import org.junit.Test

class FiltrosHistorialTest {
    private val datos = listOf(
        MovimientoAuditoria("M-01", "SUC-01", "Alameda", "CAF", "Café", TipoMovimiento.ENTRADA, 3, 10, "OPR-1", "2026-10-12 10:00 UTC", "Recepción"),
        MovimientoAuditoria("M-02", "SUC-02", "Norte", "ARR", "Arroz", TipoMovimiento.SALIDA, -2, 8, "OPR-2", "2026-10-12 10:01 UTC", "Venta")
    )
    @Test fun filtraPorSucursal() {
        val ui = HistorialAlertasUiState(movimientos = datos, sucursalId = "SUC-01")
        assertEquals(listOf("M-01"), ui.movimientosVisibles.map { it.id })
    }
    @Test fun filtraPorTipoYTexto() {
        val ui = HistorialAlertasUiState(movimientos = datos, filtroMovimiento = FiltroMovimiento.SALIDA, busqueda = "arroz")
        assertEquals(listOf("M-02"), ui.movimientosVisibles.map { it.id })
    }
}
