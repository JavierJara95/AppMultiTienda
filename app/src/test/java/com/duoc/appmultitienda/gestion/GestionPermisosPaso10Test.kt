package com.duoc.appmultitienda.gestion.navigation

import com.duoc.appmultitienda.gestion.model.Rol
import org.junit.Assert.*
import org.junit.Test

class GestionPermisosPaso10Test {
    @Test fun gerenciaSoloReportes() = assertEquals(listOf(GestionRuta.REPORTES), GestionPermisos.rutasPara(Rol.GERENCIA))
    @Test fun encargadoTieneReposicionPeroNoDashboard() {
        assertTrue(GestionPermisos.tieneAcceso(Rol.ENCARGADO, GestionRuta.REPOSICION))
        assertFalse(GestionPermisos.tieneAcceso(Rol.ENCARGADO, GestionRuta.DASHBOARD_REPOSICION))
    }
    @Test fun adminTieneDashboard() {
        assertTrue(GestionPermisos.tieneAcceso(Rol.ADMIN, GestionRuta.DASHBOARD_REPOSICION))
    }
}
