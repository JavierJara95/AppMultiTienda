package com.duoc.appmultitienda.core.model

data class Usuario(val id: String, val nombre: String, val rol: String, val tiendas: List<String>)

data class Sesion(val token: String, val usuario: Usuario)

data class LoginRequest(val usuario: String, val clave: String)

data class Sucursal(
    val id: String,
    val nombre: String,
    val direccion: String,
    val activa: Boolean,
    val encargadoId: String,
    val actualizadaUtc: String,
    val permitida: Boolean,
)

data class Producto(
    val sku: String,
    val codigoBarras: String,
    val nombre: String,
    val categoria: String,
    val unidad: String,
    val ubicacion: String,
    /** null o false = disponible; true = desactivado (compatible con JSON de Room anterior). */
    val inactivo: Boolean? = null,
)

/** Alta/edición administrativa. Los saldos se modifican únicamente mediante movimientos o ajustes aprobados. */
data class CatalogoProductoRequest(
    val sku: String,
    val codigoBarras: String,
    val nombre: String,
    val categoria: String,
    val unidad: String,
    val ubicacion: String,
    val minimo: Int,
    val sucursalesIds: List<String>,
)

data class Existencia(
    val tiendaId: String,
    val producto: Producto,
    val cantidad: Int,
    val minimo: Int,
    val version: Long,
)

data class Movimiento(
    val id: String,
    val tiendaId: String,
    val sku: String,
    val tipo: String,
    val cantidad: Int,
    val previo: Int,
    val nuevoSaldo: Int,
    val motivo: String,
    val referencia: String,
    val actorId: String,
    val instanteUtc: String,
    val conteoId: String,
)

data class Conteo(
    val id: String,
    val tiendaId: String,
    val sku: String,
    val esperado: Int,
    val contado: Int,
    val observacion: String,
    val actorId: String,
    val instanteUtc: String,
    val estado: String,
    val version: Long,
    val motivoResolucion: String,
    val resolutorId: String,
    val resueltoUtc: String,
    val decisionId: String,
) {
    val diferencia: Int
        get() = contado - esperado
}

data class Reposicion(
    val id: String,
    val tiendaId: String,
    val sku: String,
    val cantidad: Int,
    val motivo: String,
    val destinoId: String,
    val actorId: String,
    val instanteUtc: String,
    val estado: String,
)

data class Evento(
    val id: String,
    val tiendaId: String,
    val sku: String,
    val tipo: String,
    val titulo: String,
    val cantidad: Int,
    val previo: Int,
    val nuevoSaldo: Int,
    val motivo: String,
    val referencia: String,
    val actorId: String,
    val instanteUtc: String,
    val resultado: String,
)

data class Snapshot(
    val sucursales: List<Sucursal> = emptyList(),
    val existencias: List<Existencia> = emptyList(),
    val movimientos: List<Movimiento> = emptyList(),
    val conteos: List<Conteo> = emptyList(),
    val reposiciones: List<Reposicion> = emptyList(),
    val eventos: List<Evento> = emptyList(),
    val actualizadoUtc: String = "",
)

data class MovimientoRequest(
    val tiendaId: String,
    val sku: String,
    val tipo: String,
    val cantidad: Int,
    val motivo: String,
    val referencia: String,
    val version: Long,
    val idempotencyKey: String,
)

data class ConteoRequest(
    val tiendaId: String,
    val sku: String,
    val contado: Int,
    val observacion: String,
    val version: Long,
    val idempotencyKey: String,
)

data class ResolverRequest(
    val aprobar: Boolean,
    val motivo: String,
    val version: Long,
    val idempotencyKey: String,
)

data class ReposicionRequest(
    val tiendaId: String,
    val sku: String,
    val cantidad: Int,
    val motivo: String,
    val destinoId: String,
    val idempotencyKey: String,
)

data class EstadoTiendaRequest(val activa: Boolean, val idempotencyKey: String)

data class Resolucion(val conteo: Conteo, val existencia: Existencia, val evento: Evento)

data class ApiError(val code: String, val mensaje: String, val vigente: Existencia?)

data class Datos(
    val usuario: Usuario? = null,
    val snapshot: Snapshot = Snapshot(),
    val tiendaId: String? = null,
    val cargando: Boolean = false,
    val error: String? = null,
) {
    val tienda: Sucursal?
        get() = snapshot.sucursales.firstOrNull { it.id == tiendaId }

    val existenciasTienda: List<Existencia>
        get() = snapshot.existencias.filter { it.tiendaId == tiendaId }

    val activas: List<Sucursal>
        get() = snapshot.sucursales.filter { it.activa && it.permitida }
}

/** Único tipo de error de datos. Un fallo de red en POST puede dejar una respuesta desconocida. */
class FalloDatos(
    message: String,
    val codigo: String = "RED",
    val status: Int = 0,
    val vigente: Existencia? = null,
    val incierto: Boolean = false,
) : Exception(message)

fun Int.conSigno(): String = if (this > 0) "+$this" else toString()

fun estadoStock(stock: Int, minimo: Int): String =
    when {
        stock == 0 -> "Quiebre"
        stock <= minimo -> "Stock bajo"
        else -> "Disponible"
    }

fun String.rolVisible(): String =
    when (this) {
        "OPERADOR" -> "Operador"
        "ENCARGADO" -> "Encargado"
        "ADMIN" -> "Admin"
        "GERENCIA" -> "Gerencia"
        else -> "Sin rol"
    }
