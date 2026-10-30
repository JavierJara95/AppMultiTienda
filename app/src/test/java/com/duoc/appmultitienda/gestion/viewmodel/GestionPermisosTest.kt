package com.duoc.appmultitienda.gestion.navigation

import com.duoc.appmultitienda.gestion.model.Rol
import org.junit.Assert.*
import org.junit.Test

class GestionPermisosTest {
    @Test fun operadorNoAccedeAlModuloJavier() {
        assertTrue(GestionPermisos.rutasPara(Rol.OPERADOR).isEmpty())
    }
    @Test fun encargadoPuedeRevisarDiferenciasPeroNoAdministrarSucursales() {
        assertTrue(GestionPermisos.tieneAcceso(Rol.ENCARGADO, GestionRuta.DIFERENCIAS))
        assertFalse(GestionPermisos.tieneAcceso(Rol.ENCARGADO, GestionRuta.REPORTES_SUCURSALES))
    }
    @Test fun gerenciaPuedeVerTodasLasRutas() {
        assertEquals(GestionRuta.entries.size, GestionPermisos.rutasPara(Rol.GERENCIA).size)
    }
}
