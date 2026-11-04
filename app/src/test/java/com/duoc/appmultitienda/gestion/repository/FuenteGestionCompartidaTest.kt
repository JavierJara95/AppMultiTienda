package com.duoc.appmultitienda.gestion.repository

import com.duoc.appmultitienda.gestion.model.EstadoConteo
import com.duoc.appmultitienda.gestion.model.TipoMovimiento
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FuenteGestionCompartidaTest {
    @Test fun aprobarActualizaConteoHistorialYReporte() = runBlocking {
        val fuente = FuenteGestionCompartida()
        val resultado = fuente.decidir("CNT-1048", true, "Merma verificada", "ENC-004")
        assertTrue(resultado is ResultadoDecision.Exito)
        assertEquals(EstadoConteo.APROBADO, fuente.observarConteos().first().first { it.idConteo == "CNT-1048" }.estado)
        val ajuste = fuente.observarMovimientos().first().first { it.id == "AJ-1048" }
        assertEquals(TipoMovimiento.AJUSTE, ajuste.tipo)
        assertEquals(-4, ajuste.cantidad)
        assertEquals(44, ajuste.saldoFinal)
        assertTrue(fuente.obtenerMovimientos("SUC-01").any { it.id == "AJ-1048" })
        assertEquals(44, fuente.observarAlertas().first().first { it.productoCodigo == "CAF-001" }.stockActual)
    }
    @Test fun rechazarNoGeneraAjuste() = runBlocking {
        val fuente = FuenteGestionCompartida()
        val resultado = fuente.decidir("CNT-1048", false, "Recuento duplicado", "ENC-004")
        assertTrue(resultado is ResultadoDecision.Exito)
        assertFalse(fuente.obtenerMovimientos(null).any { it.id == "AJ-1048" })
    }
    @Test fun bloqueaSaldoDesactualizado() = runBlocking {
        val fuente = FuenteGestionCompartida()
        fuente.simularCambioExterno("SUC-01", "CAF-001", 46)
        assertTrue(fuente.decidir("CNT-1048", true, "Merma confirmada", "ENC-004") is ResultadoDecision.Conflicto)
    }
    @Test fun impideRolNoAutorizado() = runBlocking {
        val fuente = FuenteGestionCompartida()
        assertTrue(fuente.decidir("CNT-1048", true, "Merma confirmada", "OPR-017") is ResultadoDecision.Error)
    }
}
