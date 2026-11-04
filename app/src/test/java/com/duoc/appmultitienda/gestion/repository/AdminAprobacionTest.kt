package com.duoc.appmultitienda.gestion.repository

import com.duoc.appmultitienda.gestion.model.EstadoConteo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AdminAprobacionTest {
    @Test fun adminApruebaYGeneraMovimiento() = runBlocking {
        val fuente = FuenteGestionCompartida()
        val resultado = fuente.decidir("CNT-1048", true, "Merma verificada", "ADM-001")
        assertTrue(resultado is ResultadoDecision.Exito)
        val conteo = fuente.observarConteos().first().first { it.idConteo == "CNT-1048" }
        assertEquals(EstadoConteo.APROBADO, conteo.estado)
        val movimiento = fuente.observarMovimientos().first().first { it.id == "AJ-1048" }
        assertEquals("ADM-001", movimiento.actorId)
        assertEquals(-4, movimiento.cantidad)
        assertEquals(44, movimiento.saldoFinal)
    }

    @Test fun operadorNoAprueba() = runBlocking {
        val fuente = FuenteGestionCompartida()
        val resultado = fuente.decidir("CNT-1048", true, "Merma verificada", "OPR-017")
        assertTrue(resultado is ResultadoDecision.Error)
    }
}
