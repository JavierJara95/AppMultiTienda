package com.duoc.appmultitienda.gestion.export

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.duoc.appmultitienda.gestion.model.FilaReporte
import java.io.ByteArrayOutputStream

/** Genera bytes PDF con Android PdfDocument; no necesita Internet ni permisos amplios. */
object PdfReporteExportador {
    fun generar(filas: List<FilaReporte>, sucursal: String): ByteArray {
        val pdf = PdfDocument()
        try {
            val ancho = 595
            val alto = 842
            val margen = 36f
            val titulo = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 18f; isFakeBoldText = true }
            val cuerpo = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 10f }
            val pequeno = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9f }
            val alturaFila = 46f
            var paginaNumero = 0
            var pagina: PdfDocument.Page? = null
            var y = 0f
            fun nuevaPagina() {
                pagina?.let { pdf.finishPage(it) }
                paginaNumero++
                pagina = pdf.startPage(PdfDocument.PageInfo.Builder(ancho, alto, paginaNumero).create())
                val c = requireNotNull(pagina).canvas
                c.drawText("MultiTienda | Reporte de movimientos", margen, 45f, titulo)
                c.drawText("Ubicacion: $sucursal   |   Total de registros: ${filas.size}", margen, 67f, cuerpo)
                c.drawLine(margen, 78f, ancho - margen, 78f, cuerpo)
                y = 100f
            }
            nuevaPagina()
            if (filas.isEmpty()) {
                pagina!!.canvas.drawText("No hay movimientos para el filtro seleccionado.", margen, y, cuerpo)
            }
            filas.forEach { f ->
                if (y + alturaFila > alto - 45f) nuevaPagina()
                val c = pagina!!.canvas
                c.drawText(recortar("${f.id}  |  ${f.tipo}  |  ${f.cantidad} uds", 88), margen, y, cuerpo)
                c.drawText(recortar("${f.producto}  |  ${f.sucursalId}", 95), margen, y + 14f, pequeno)
                c.drawText(recortar(f.fechaUtc, 95), margen, y + 28f, pequeno)
                c.drawLine(margen, y + 34f, ancho - margen, y + 34f, pequeno)
                y += alturaFila
            }
            pagina?.let { pdf.finishPage(it) }
            val salida = ByteArrayOutputStream()
            pdf.writeTo(salida)
            return salida.toByteArray()
        } finally {
            pdf.close()
        }
    }

    private fun recortar(texto: String, max: Int) = if (texto.length > max) texto.take(max - 3) + "..." else texto
}
