package com.duoc.appmultitienda.gestion.navigation

import com.duoc.appmultitienda.gestion.model.Rol

/** Rutas permitidas en la interfaz de demostración; el backend deberá validar los permisos. */
enum class GestionRuta(val etiqueta: String) {
    DIFERENCIAS("Diferencias"),
    HISTORIAL_ALERTAS("Historial y alertas"),
    DASHBOARD_REPOSICION("Dashboard y reposición"),
    REPORTES_SUCURSALES("Reportes y sucursales")
}

object GestionPermisos {
    fun rutasPara(rol: Rol): List<GestionRuta> = when (rol) {
        Rol.OPERADOR -> emptyList() // módulo del operador corresponde a Esteban
        Rol.ENCARGADO -> listOf(GestionRuta.DIFERENCIAS, GestionRuta.HISTORIAL_ALERTAS)
        Rol.GERENCIA -> GestionRuta.entries
    }

    fun tieneAcceso(rol: Rol, ruta: GestionRuta): Boolean = ruta in rutasPara(rol)
}
