package com.duoc.appmultitienda.gestion.model

enum class EstadoReposicion { PENDIENTE, ATENDIDA }

data class SolicitudReposicion(
    val id: String,
    val productoCodigo: String,
    val productoNombre: String,
    val sucursalId: String,
    val sucursalNombre: String,
    val cantidad: Int,
    val motivo: String,
    val solicitanteId: String,
    val fechaUtc: String,
    val estado: EstadoReposicion = EstadoReposicion.PENDIENTE
)

object ReposicionValidator {
    fun validar(cantidadTexto: String, motivo: String): String? {
        val cantidad = cantidadTexto.toIntOrNull()
        if (cantidad == null || cantidad <= 0) return "Ingresa una cantidad entera mayor que cero"
        if (motivo.trim().length !in 5..200) return "El motivo debe tener entre 5 y 200 caracteres"
        return null
    }
}

data class DashboardResumen(
    val unidadesEnAlertas: Int,
    val productosBajoMinimo: Int,
    val productosAgotados: Int,
    val alertasActivas: Int,
    val solicitudesPendientes: Int,
    val movimientosRegistrados: Int
)
