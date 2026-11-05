package com.duoc.appmultitienda.gestion.model

import com.duoc.appmultitienda.gestion.viewmodel.construirResumen
import org.junit.Assert.*
import org.junit.Test

class ReposicionDashboardTest {
    @Test fun cantidadInvalida() = assertNotNull(ReposicionValidator.validar("0", "Reponer mañana"))
    @Test fun motivoCorto() = assertNotNull(ReposicionValidator.validar("5", "No"))
    @Test fun datosValidos() = assertNull(ReposicionValidator.validar("24", "Quiebre en la tienda"))
    @Test fun resumenFiltrado() {
        val alertas = listOf(
            AlertaStock("A", "Aceite", "SUC-01", "Alameda", 0, 8),
            AlertaStock("B", "Arroz", "SUC-01", "Alameda", 3, 5),
            AlertaStock("C", "Cafe", "SUC-02", "Norte", 20, 10)
        )
        val resumen = construirResumen(alertas, emptyList(), emptyList(), "SUC-01")
        assertEquals(2, resumen.alertasActivas)
        assertEquals(1, resumen.productosAgotados)
        assertEquals(1, resumen.productosBajoMinimo)
    }
}
