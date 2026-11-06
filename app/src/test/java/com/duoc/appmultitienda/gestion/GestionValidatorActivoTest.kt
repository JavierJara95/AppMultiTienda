package com.duoc.appmultitienda.gestion

import com.duoc.appmultitienda.gestion.validation.GestionValidator
import org.junit.Assert.*
import org.junit.Test

class GestionValidatorActivoTest {
    @Test fun loginObligaUsuarioYClave() {
        val errores = GestionValidator.login("", "")
        assertTrue(errores.containsKey("usuario"))
        assertTrue(errores.containsKey("clave"))
        assertTrue(GestionValidator.login("admin", "demo123").isEmpty())
    }

    @Test fun cantidadDebeSerEnteroPositivo() {
        assertNull(GestionValidator.cantidad("5"))
        assertNotNull(GestionValidator.cantidad("0"))
        assertNotNull(GestionValidator.cantidad("3.5"))
    }

    @Test fun fechasEnOrden() {
        assertNull(GestionValidator.fechas("2026-01-01", "2026-02-01"))
        assertNotNull(GestionValidator.fechas("2026-02-01", "2026-01-01"))
    }
}
