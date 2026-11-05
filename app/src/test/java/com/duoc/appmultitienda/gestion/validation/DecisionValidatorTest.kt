package com.duoc.appmultitienda.gestion.validation

import org.junit.Assert.*
import org.junit.Test

class DecisionValidatorTest {
    @Test fun motivoCortoSeRechaza() {
        assertNotNull(DecisionValidator.errorMotivo("No"))
    }
    @Test fun motivoValidoSeAcepta() {
        assertNull(DecisionValidator.errorMotivo("Merma verificada en recuento"))
    }
    @Test fun motivoLargoSeRechaza() {
        assertNotNull(DecisionValidator.errorMotivo("x".repeat(201)))
    }
}
