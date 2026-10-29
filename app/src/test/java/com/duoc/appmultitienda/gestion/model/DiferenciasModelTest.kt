package com.duoc.appmultitienda.gestion.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DiferenciasModelTest {
    @Test fun diferenciaNegativaCuandoFaltanUnidades() {
        val conteo = DiferenciaInventario("CNT-1", "SUC-1", "Tienda", "P-1", "Café", 48, 44, "OPR-1", "UTC", "Prueba", EstadoConteo.PENDIENTE)
        assertEquals(-4, conteo.diferencia)
    }
    @Test fun diferenciaPositivaCuandoSobranUnidades() {
        val conteo = DiferenciaInventario("CNT-2", "SUC-1", "Tienda", "P-2", "Leche", 36, 38, "OPR-1", "UTC", "Prueba", EstadoConteo.PENDIENTE)
        assertEquals(2, conteo.diferencia)
    }
}
