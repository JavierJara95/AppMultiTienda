package com.duoc.appmultitienda.operador

import com.duoc.appmultitienda.operador.validation.OperadorValidator
import org.junit.Assert.*
import org.junit.Test

class OperadorValidatorActivoTest {
    @Test fun codigoValidoYVacio() {
        assertNull(OperadorValidator.codigo("CAF-001"))
        assertNotNull(OperadorValidator.codigo(" "))
    }

    @Test fun conteoAceptaCeroPeroNoNegativos() {
        assertNull(OperadorValidator.conteo("0"))
        assertNotNull(OperadorValidator.conteo("-1"))
        assertNotNull(OperadorValidator.conteo("abc"))
    }

    @Test fun movimientoRechazaSalidasSinStock() {
        assertNotNull(OperadorValidator.movimiento("20", "SALIDA", 16))
        assertNull(OperadorValidator.movimiento("6", "SALIDA", 16))
        assertNotNull(OperadorValidator.movimiento("0", "ENTRADA", 16))
    }

    @Test fun motivoDebeTenerLongitudValida() {
        assertNotNull(OperadorValidator.motivo("abc"))
        assertNull(OperadorValidator.motivo("Ingreso de mercadería"))
    }
}
