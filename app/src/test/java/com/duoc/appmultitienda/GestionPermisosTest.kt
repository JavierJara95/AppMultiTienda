package com.duoc.appmultitienda.gestion.navigation

import com.duoc.appmultitienda.gestion.model.Rol
import org.junit.Assert.*
import org.junit.Test

class GestionPermisosTest {
    @Test fun gerenciaSoloTieneReportes() {
        assertEquals(listOf(GestionRuta.REPORTES), GestionPermisos.rutasPara(Rol.GERENCIA))
    }
    @Test fun gerenciaNoPuedeAdministrarNiAprobar() {
        assertFalse(GestionPermisos.tieneAcceso(Rol.GERENCIA, GestionRuta.DIFERENCIAS))
        assertFalse(GestionPermisos.tieneAcceso(Rol.GERENCIA, GestionRuta.REPORTES_SUCURSALES))
        assertFalse(GestionPermisos.tieneAcceso(Rol.GERENCIA, GestionRuta.DASHBOARD_REPOSICION))
    }
    @Test fun adminTieneAdministracionYControlesAnteriores() {
        assertTrue(GestionPermisos.tieneAcceso(Rol.ADMIN, GestionRuta.REPORTES_SUCURSALES))
        assertTrue(GestionPermisos.tieneAcceso(Rol.ADMIN, GestionRuta.DIFERENCIAS))
        assertTrue(GestionPermisos.tieneAcceso(Rol.ADMIN, GestionRuta.DASHBOARD_REPOSICION))
    }
    @Test fun encargadoConservaAccesoAnterior() {
        assertTrue(GestionPermisos.tieneAcceso(Rol.ENCARGADO, GestionRuta.DIFERENCIAS))
        assertFalse(GestionPermisos.tieneAcceso(Rol.ENCARGADO, GestionRuta.REPORTES))
    }
    @Test fun operadorNoTieneRutasDeJavier() {
        assertTrue(GestionPermisos.rutasPara(Rol.OPERADOR).isEmpty())
    }
}
