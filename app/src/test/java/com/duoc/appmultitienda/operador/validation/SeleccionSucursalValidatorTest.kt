package com.duoc.appmultitienda.operador.validation

import com.duoc.appmultitienda.operador.model.SucursalOperador
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SeleccionSucursalValidatorTest {

    @Test
    fun tiendaActivaYAsignadaEsValida() {

        val sucursal = SucursalOperador(
            id = "SUC-01",
            nombre = "Tienda Alameda",
            direccion = "Av. Alameda 120",
            activa = true,
            asignada = true
        )

        val resultado = SeleccionSucursalValidator.validar(sucursal)

        assertNull(resultado)
    }

    @Test
    fun tiendaInactivaNoEsValida() {

        val sucursal = SucursalOperador(
            id = "SUC-03",
            nombre = "Tienda Mirador",
            direccion = "Calle Mirador 86",
            activa = false,
            asignada = true
        )

        val resultado = SeleccionSucursalValidator.validar(sucursal)

        assertEquals(
            "La tienda seleccionada está inactiva.",
            resultado
        )
    }

    @Test
    fun tiendaNoAsignadaNoEsValida() {

        val sucursal = SucursalOperador(
            id = "SUC-04",
            nombre = "Tienda Central",
            direccion = "Dirección de prueba",
            activa = true,
            asignada = false
        )

        val resultado = SeleccionSucursalValidator.validar(sucursal)

        assertEquals(
            "No tienes autorización para esta tienda.",
            resultado
        )
    }

    @Test
    fun seleccionVaciaNoEsValida() {

        val resultado = SeleccionSucursalValidator.validar(null)

        assertEquals(
            "Debes seleccionar una tienda.",
            resultado
        )
    }

    @Test
    fun tiendaValidaNoDevuelveError() {

        val sucursal = SucursalOperador(
            id = "SUC-02",
            nombre = "Bodega Norte",
            direccion = "Parque Industrial 45",
            activa = true,
            asignada = true
        )

        val resultado = SeleccionSucursalValidator.validar(sucursal)

        assertNull(resultado)
    }
}