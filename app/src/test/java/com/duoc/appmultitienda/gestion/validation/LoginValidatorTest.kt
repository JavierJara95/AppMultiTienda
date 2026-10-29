package com.duoc.appmultitienda.gestion.validation

import org.junit.Assert.*
import org.junit.Test


class LoginValidatorTest {
    @Test fun `CamposVaciosMuestranErrores`() {
        val errores = LoginValidator.validar("", "")
        assertTrue(errores.hayErrores)
        assertNotNull(errores.usuario)
        assertNotNull(errores.clave)
    }

    @Test fun `CamposCompletosNoMuestranErrores`() {
        val errores = LoginValidator.validar("gerencia", "demo123")
        assertFalse(errores.hayErrores)
    }

}
