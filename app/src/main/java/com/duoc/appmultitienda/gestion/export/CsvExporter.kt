package com.duoc.appmultitienda.gestion.export

import com.duoc.appmultitienda.core.model.*
import com.duoc.appmultitienda.gestion.validation.GestionValidator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

data class FiltrosReporte(
    val tipo: String = "Movimientos",
    val tiendaId: String = "TODAS",
    val desde: String = LocalDate.now(ZoneOffset.UTC).toString(),
    val hasta: String = LocalDate.now(ZoneOffset.UTC).toString(),
    val movimiento: String = "TODOS",
)

data class DocumentoCsv(
    val nombre: String,
    val contenido: String,
    val registros: Int,
    val tipo: String,
    val ubicacion: String,
    val periodo: String,
)

interface CsvWriter {
    suspend fun guardar(uri: String, documento: DocumentoCsv): String
}

/** RFC 4180, UTF-8 con BOM para Excel, escape de comillas y protección de texto de celdas. */
object CsvExporter {
    private fun celda(valor: Any?): String {
        var text = valor?.toString().orEmpty()
        if (
            valor !is Number &&
                text.trimStart().firstOrNull() in listOf('=', '+', '-', '@', '\t', '\r')
        )
            text = "'$text"
        return "\"${text.replace("\"", "\"\"")}\""
    }

    private fun enPeriodo(utc: String, f: FiltrosReporte): Boolean =
        runCatching {
                val d = Instant.parse(utc).atOffset(ZoneOffset.UTC).toLocalDate()
                d >= LocalDate.parse(f.desde) && d <= LocalDate.parse(f.hasta)
            }
            .getOrDefault(false)

    fun movimientos(s: Snapshot, f: FiltrosReporte): List<Movimiento> =
        s.movimientos.filter {
            (f.tiendaId == "TODAS" || it.tiendaId == f.tiendaId) &&
                (f.movimiento == "TODOS" || it.tipo == f.movimiento) &&
                enPeriodo(it.instanteUtc, f)
        }

    fun conteos(s: Snapshot, f: FiltrosReporte): List<Conteo> =
        s.conteos.filter {
            (f.tiendaId == "TODAS" || it.tiendaId == f.tiendaId) && enPeriodo(it.instanteUtc, f)
        }

    fun stock(s: Snapshot, f: FiltrosReporte): List<Existencia> =
        s.existencias.filter { f.tiendaId == "TODAS" || it.tiendaId == f.tiendaId }

    fun generar(s: Snapshot, f: FiltrosReporte): DocumentoCsv {
        require(GestionValidator.fechas(f.desde, f.hasta) == null) { "Periodo inválido." }
        val filas: List<List<Any?>> =
            when (f.tipo) {
                "Stock" ->
                    listOf(
                        listOf(
                            "tienda",
                            "sku",
                            "producto",
                            "stock",
                            "minimo",
                            "estado",
                            "version",
                            "consultado_utc",
                        )
                    ) +
                        stock(s, f).map {
                            listOf(
                                it.tiendaId,
                                it.producto.sku,
                                it.producto.nombre,
                                it.cantidad,
                                it.minimo,
                                estadoStock(it.cantidad, it.minimo),
                                it.version,
                                s.actualizadoUtc,
                            )
                        }
                "Diferencias" ->
                    listOf(
                        listOf(
                            "id",
                            "tienda",
                            "sku",
                            "esperado",
                            "contado",
                            "diferencia",
                            "estado",
                            "actor",
                            "utc",
                            "observacion",
                            "motivo_resolucion",
                            "resolutor",
                            "resuelto_utc",
                            "decision",
                        )
                    ) +
                        conteos(s, f).map {
                            listOf(
                                it.id,
                                it.tiendaId,
                                it.sku,
                                it.esperado,
                                it.contado,
                                it.diferencia,
                                it.estado,
                                it.actorId,
                                it.instanteUtc,
                                it.observacion,
                                it.motivoResolucion,
                                it.resolutorId,
                                it.resueltoUtc,
                                it.decisionId,
                            )
                        }
                else ->
                    listOf(
                        listOf(
                            "id",
                            "tienda",
                            "sku",
                            "tipo",
                            "cantidad",
                            "previo",
                            "saldo_final",
                            "motivo",
                            "referencia",
                            "actor",
                            "utc",
                            "conteo",
                        )
                    ) +
                        movimientos(s, f).map {
                            listOf(
                                it.id,
                                it.tiendaId,
                                it.sku,
                                it.tipo,
                                it.cantidad,
                                it.previo,
                                it.nuevoSaldo,
                                it.motivo,
                                it.referencia,
                                it.actorId,
                                it.instanteUtc,
                                it.conteoId,
                            )
                        }
            }
        val ubicacion =
            s.sucursales.firstOrNull { it.id == f.tiendaId }?.nombre ?: "Todas las sucursales"
        val nombre =
            "multitienda_${f.tiendaId.lowercase()}_${f.tipo.lowercase()}_${f.hasta.replace("-", "")}.csv"
        return DocumentoCsv(
            nombre,
            "\uFEFF" +
                filas.joinToString("\r\n", postfix = "\r\n") { row ->
                    row.joinToString(",") { celda(it) }
                },
            filas.size - 1,
            f.tipo,
            ubicacion,
            "${f.desde} a ${f.hasta} UTC",
        )
    }
}
