package com.duoc.appmultitienda.core.navigation

/** Las mismas restricciones se comprueban de nuevo en Spring Boot. */
object Rutas {
    val gestion =
        setOf(
            "diferencias",
            "resolver",
            "conflicto",
            "resolucion_recibida",
            "reposicion",
            "reposicion_recibida",
        )
    val gerencia = setOf("dashboard", "reportes", "exportacion_resultado", "sucursales", "catalogo")
    val operacion = setOf("movimiento", "revision", "comprobante", "conteo", "conteo_recibido")

    fun permite(rol: String, ruta: String): Boolean =
        when {
            ruta in gerencia -> rol == "ADMIN"
            ruta in gestion -> rol == "ENCARGADO"
            ruta in operacion -> rol in setOf("OPERADOR", "ENCARGADO")
            ruta in
                setOf("escaner", "manual", "manual_denegado", "producto", "inicio", "ubicacion") ->
                rol in setOf("OPERADOR", "ENCARGADO")
            ruta in setOf("historial", "alertas", "perfil") -> true
            else -> false
        }

    val titulos =
        mapOf(
            "ubicacion" to "Tu ubicación",
            "inicio" to "Hola, Operador",
            "escaner" to "Escanear producto",
            "manual" to "Ingreso manual",
            "manual_denegado" to "Ingreso manual",
            "producto" to "Producto",
            "movimiento" to "Registrar movimiento",
            "revision" to "Revisar movimiento",
            "comprobante" to "Comprobante",
            "conteo" to "Conteo físico",
            "conteo_recibido" to "Conteo físico",
            "diferencias" to "Diferencias",
            "resolver" to "Revisar diferencia",
            "conflicto" to "Revisar de nuevo",
            "resolucion_recibida" to "Resultado de revisión",
            "historial" to "Historial",
            "alertas" to "Alertas de stock",
            "reposicion" to "Solicitud de reposición",
            "reposicion_recibida" to "Solicitud de reposición",
            "dashboard" to "Dashboard",
            "reportes" to "Exportar reporte",
            "exportacion_resultado" to "Exportar reporte",
            "sucursales" to "Sucursales",
            "catalogo" to "Gestión de productos",
            "perfil" to "Perfil",
        )
}
