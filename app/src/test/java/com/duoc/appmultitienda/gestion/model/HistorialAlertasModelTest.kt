package com.duoc.appmultitienda.gestion.model

import org.junit.Assert.*
import org.junit.Test

class HistorialAlertasModelTest {
    @Test fun stockCeroEsQuiebre() {
        val a = AlertaStock("P-1", "Producto", "SUC-01", "Alameda", 0, 8)
        assertEquals(EstadoStock.QUIEBRE, a.estado)
    }
    @Test fun stockIgualAlMinimoEsBajo() {
        val a = AlertaStock("P-1", "Producto", "SUC-01", "Alameda", 8, 8)
        assertEquals(EstadoStock.BAJO, a.estado)
    }
    @Test fun stockSobreMinimoEsNormal() {
        val a = AlertaStock("P-1", "Producto", "SUC-01", "Alameda", 9, 8)
        assertEquals(EstadoStock.NORMAL, a.estado)
    }
    @Test fun evitaSaldoNegativo() {
        try {
            AlertaStock("P-1", "Producto", "SUC-01", "Alameda", -1, 8)
            fail("Se esperaba IllegalArgumentException")
        } catch (e: IllegalArgumentException) { assertTrue(true) }
    }
}
