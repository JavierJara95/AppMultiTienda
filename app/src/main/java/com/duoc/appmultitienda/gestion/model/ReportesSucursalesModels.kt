package com.duoc.appmultitienda.gestion.model

data class SucursalGestion(val id: String, val nombre: String, val direccion: String, val activa: Boolean)
data class FilaReporte(val id: String, val sucursalId: String, val producto: String, val tipo: String, val cantidad: Int, val fechaUtc: String)

object CsvExportador {
    private fun campo(valor: String): String = "\"" + valor.replace("\"", "\"\"") + "\""
    fun generar(filas: List<FilaReporte>): String {
        val encabezado = "id,sucursal,producto,tipo,cantidad,fecha_utc"
        val lineas = filas.joinToString("\r\n") { fila ->
            listOf(fila.id, fila.sucursalId, fila.producto, fila.tipo, fila.cantidad.toString(), fila.fechaUtc)
                .joinToString(",") { campo(it) }
        }
        return "\uFEFF" + encabezado + if (lineas.isEmpty()) "\r\n" else "\r\n" + lineas + "\r\n"
    }
}
