package com.duoc.appmultitienda.gestion.navigation

import com.duoc.appmultitienda.gestion.model.Rol

/** Control de navegación local para el MVP académico, no autenticación de producción. */
enum class GestionRuta(val etiqueta: String) {
    DIFERENCIAS("Diferencias"),
    HISTORIAL_ALERTAS("Historial y alertas"),
    DASHBOARD_REPOSICION("Dashboard y reposición"),
    REPORTES("Reportes"),
    REPORTES_SUCURSALES("Administrar sucursales y reportes")
}

object GestionPermisos {
    fun rutasPara(rol: Rol): List<GestionRuta> = when (rol) {
        Rol.OPERADOR -> emptyList() // Módulo de Esteban
        Rol.ENCARGADO -> listOf(GestionRuta.DIFERENCIAS, GestionRuta.HISTORIAL_ALERTAS)
        Rol.GERENCIA -> listOf(GestionRuta.REPORTES)
        Rol.ADMIN -> GestionRuta.entries.filter { it != GestionRuta.REPORTES }
    }

    fun tieneAcceso(rol: Rol, ruta: GestionRuta): Boolean = ruta in rutasPara(rol)
}
