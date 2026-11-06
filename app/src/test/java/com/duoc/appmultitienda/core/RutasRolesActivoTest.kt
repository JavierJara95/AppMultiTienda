package com.duoc.appmultitienda.core

import com.duoc.appmultitienda.core.navigation.Rutas
import org.junit.Assert.*
import org.junit.Test

class RutasRolesActivoTest {
    @Test fun soloAdminGestionaProductos() {
        assertTrue(Rutas.permite("ADMIN", "catalogo"))
        assertFalse(Rutas.permite("OPERADOR", "catalogo"))
        assertFalse(Rutas.permite("ENCARGADO", "catalogo"))
        assertFalse(Rutas.permite("GERENCIA", "catalogo"))
    }

    @Test fun soloEncargadoApruebaDiferencias() {
        assertTrue(Rutas.permite("ENCARGADO", "resolver"))
        assertFalse(Rutas.permite("OPERADOR", "resolver"))
    }

    @Test fun operadorPuedeHacerConteos() {
        assertTrue(Rutas.permite("OPERADOR", "conteo"))
        assertTrue(Rutas.permite("ENCARGADO", "conteo"))
        assertFalse(Rutas.permite("ADMIN", "conteo"))
    }
}
