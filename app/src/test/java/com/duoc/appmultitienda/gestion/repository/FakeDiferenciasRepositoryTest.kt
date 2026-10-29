package com.duoc.appmultitienda.gestion.repository

import com.duoc.appmultitienda.gestion.model.EstadoConteo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FakeDiferenciasRepositoryTest {
    @Test fun aprobarActualizaEstadoYNoPermiteSegundaDecision() = runBlocking {
        val repository = FakeDiferenciasRepository()
        val respuesta = repository.decidir("CNT-1048", true, "Merma verificada", "ENC-004")
        assertTrue(respuesta is ResultadoDecision.Exito)
        assertEquals(44, (respuesta as ResultadoDecision.Exito).saldoFinal)
        assertEquals(EstadoConteo.APROBADO, repository.observarConteos().first().first { it.idConteo == "CNT-1048" }.estado)
        assertTrue(repository.decidir("CNT-1048", false, "Conteo duplicado", "ENC-004") is ResultadoDecision.Error)
    }
    @Test fun rechazarNoActualizaSaldo() = runBlocking {
        val repository = FakeDiferenciasRepository()
        val respuesta = repository.decidir("CNT-1048", false, "Recuento pendiente", "ENC-004")
        assertEquals(48, (respuesta as ResultadoDecision.Exito).saldoFinal)
        assertEquals(EstadoConteo.RECHAZADO, repository.observarConteos().first().first { it.idConteo == "CNT-1048" }.estado)
    }
    @Test fun saldoCambiadoBloqueaAprobacion() = runBlocking {
        val repository = FakeDiferenciasRepository()
        repository.simularCambioExterno("SUC-01", "CAF-001", 46)
        assertTrue(repository.decidir("CNT-1048", true, "Merma verificada", "ENC-004") is ResultadoDecision.Conflicto)
    }
    @Test fun operadorNoPuedeAprobar() = runBlocking {
        val repository = FakeDiferenciasRepository()
        assertTrue(repository.decidir("CNT-1048", true, "Merma verificada", "OPR-017") is ResultadoDecision.Error)
    }
}
