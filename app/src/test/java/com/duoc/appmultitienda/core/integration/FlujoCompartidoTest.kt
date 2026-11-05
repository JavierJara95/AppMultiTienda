package com.duoc.appmultitienda.core.integration

import com.duoc.appmultitienda.gestion.model.EstadoConteo
import com.duoc.appmultitienda.gestion.repository.FuenteGestionCompartida
import com.duoc.appmultitienda.gestion.repository.ResultadoDecision
import com.duoc.appmultitienda.operador.model.ConteoFisicoOperador
import com.duoc.appmultitienda.operador.model.ComprobanteMovimientoDemo
import com.duoc.appmultitienda.operador.validation.TipoMovimiento
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FlujoCompartidoTest {
    @Test fun conteoOperadorSeVeEnGestionYEncargadoPuedeAprobar() = runBlocking {
        val fuente = FuenteGestionCompartida()
        val solicitud = ConteoFisicoOperador(
            "", "SUC-01", "CAF-001", "Café molido 500 g", 48, 44,
            "Faltan cuatro unidades", "OPR-017", ""
        )
        val registrada = fuente.registrarConteoOperador(solicitud)
        assertEquals(EstadoConteo.PENDIENTE, registrada.estado)
        assertTrue(fuente.observarConteos().first().any { it.idConteo == registrada.idConteo })
        assertEquals(48, fuente.consultarSaldo("SUC-01", "CAF-001"))
        assertTrue(fuente.decidir(registrada.idConteo, true, "Recuento confirmado", "OPR-017") is ResultadoDecision.Error)
        assertEquals(48, fuente.consultarSaldo("SUC-01", "CAF-001"))
        assertTrue(fuente.decidir(registrada.idConteo, true, "Recuento confirmado", "ENC-004") is ResultadoDecision.Exito)
        assertEquals(44, fuente.consultarSaldo("SUC-01", "CAF-001"))
    }

    @Test fun movimientoOperadorModificaSaldoCompartido() = runBlocking {
        val fuente = FuenteGestionCompartida()
        val comprobante = ComprobanteMovimientoDemo(
            "DEMO-UNICO-001", "SUC-01", "ARR-002", "Arroz largo 1 kg",
            TipoMovimiento.ENTRADA, 6, 16, 22,
            "Recepción de proveedor", "", "OPR-017", "2026-11-04T14:00:00Z"
        )
        fuente.registrarMovimientoOperador(comprobante)
        assertEquals(22, fuente.consultarSaldo("SUC-01", "ARR-002"))
        assertTrue(fuente.observarMovimientos().first().any { it.id == comprobante.idDemostracion })
        try {
            fuente.registrarMovimientoOperador(comprobante)
            fail("No debe duplicar el movimiento")
        } catch (_: IllegalArgumentException) { }
    }
}
