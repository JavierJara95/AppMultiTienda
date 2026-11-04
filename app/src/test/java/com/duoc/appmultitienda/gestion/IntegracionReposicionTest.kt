package com.duoc.appmultitienda.gestion.repository

import com.duoc.appmultitienda.gestion.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class IntegracionReposicionTest {
    @Test fun solicitudApareceEnFuenteCompartidaSinModificarStock() = runBlocking {
        val fuente = FuenteGestionCompartida()
        val alerta = fuente.observarAlertas().first().first { it.productoCodigo == "ACE-003" }
        val antes = fuente.observarMovimientos().first().size
        val resultado = fuente.solicitar(alerta, 24, "Quiebre de stock", "ENC-004")
        assertTrue(resultado.isSuccess)
        assertEquals(1, fuente.observarSolicitudes().first().size)
        assertEquals(antes, fuente.observarMovimientos().first().size)
        assertEquals(0, fuente.observarAlertas().first().first { it.productoCodigo == "ACE-003" }.stockActual)
    }

    @Test fun operadorNoPuedeSolicitar() = runBlocking {
        val fuente = FuenteGestionCompartida()
        val alerta = fuente.observarAlertas().first().first { it.productoCodigo == "ACE-003" }
        assertTrue(fuente.solicitar(alerta, 10, "Reponer aceite", "OPR-017").isFailure)
    }

    @Test fun noSeAceptaMotivoInvalido() = runBlocking {
        val fuente = FuenteGestionCompartida()
        val alerta = fuente.observarAlertas().first().first { it.productoCodigo == "ACE-003" }
        assertTrue(fuente.solicitar(alerta, 10, "No", "ADM-001").isFailure)
        assertTrue(fuente.observarSolicitudes().first().isEmpty())
    }

    @Test fun adminPuedeSolicitarConIdsDistintos() = runBlocking {
        val fuente = FuenteGestionCompartida()
        val alerta = fuente.observarAlertas().first().first { it.productoCodigo == "ACE-003" }
        val a = fuente.solicitar(alerta, 12, "Reposición urgente", "ADM-001").getOrThrow()
        val b = fuente.solicitar(alerta, 15, "Reposición adicional", "ADM-001").getOrThrow()
        assertEquals("REP-001", a.id)
        assertEquals("REP-002", b.id)
    }
}
