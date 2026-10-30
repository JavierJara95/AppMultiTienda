package com.duoc.appmultitienda.gestion.model

import org.junit.Assert.*
import org.junit.Test

class ReportesSucursalesTest {
    @Test fun csvIncluyeEncabezadoYDatos() {
        val csv = CsvExportador.generar(listOf(FilaReporte("MOV-1", "SUC-01", "Arroz", "ENTRADA", 5, "2026-10-12T10:00:00Z")))
        assertTrue(csv.contains("id,sucursal,producto,tipo,cantidad,fecha_utc"))
        assertTrue(csv.contains("\"Arroz\""))
    }
    @Test fun csvEscapaComillasYComas() {
        val csv = CsvExportador.generar(listOf(FilaReporte("MOV-1", "SUC-01", "Café, \"premium\"", "SALIDA", -1, "UTC")))
        assertTrue(csv.contains("\"Café, \"\"premium\"\"\""))
    }
    @Test fun csvVacioMantieneCabecera() {
        val csv = CsvExportador.generar(emptyList())
        assertTrue(csv.contains("fecha_utc"))
    }
}
