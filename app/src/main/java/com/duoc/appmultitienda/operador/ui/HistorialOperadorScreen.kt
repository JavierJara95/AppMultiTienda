package com.duoc.appmultitienda.operador.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.duoc.appmultitienda.operador.model.ComprobanteMovimientoDemo
import com.duoc.appmultitienda.operador.validation.TipoMovimiento

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


// =====================================================
// COLORES MULTITIENDA
// =====================================================

private val AzulHistorial = Color(0xFF061D30)
private val AzulBotonHistorial = Color(0xFF0C4869)
private val GrisHistorial = Color(0xFF55708E)
private val FondoHistorial = Color(0xFFF8FAFC)
private val VerdeHistorial = Color(0xFF008752)
private val RojoHistorial = Color(0xFFB42318)


// =====================================================
// ALMACENAMIENTO TEMPORAL
// =====================================================

object HistorialOperadorStore {

    private val _movimientos =
        MutableStateFlow<List<ComprobanteMovimientoDemo>>(
            emptyList()
        )

    val movimientos = _movimientos.asStateFlow()

    // Registrar una operación sin duplicarla.
    @Synchronized
    fun registrar(comprobante: ComprobanteMovimientoDemo) {

        val existe = _movimientos.value.any {
            it.idDemostracion == comprobante.idDemostracion
        }

        if (existe) {
            return
        }

        _movimientos.value =
            listOf(comprobante) + _movimientos.value
    }

    // Consultar movimientos de una sucursal.
    fun obtenerPorSucursal(
        sucursalId: String
    ): List<ComprobanteMovimientoDemo> {

        return _movimientos.value.filter {
            it.sucursalId == sucursalId
        }
    }

    // Limpiar registros de demostración.
    @Synchronized
    fun limpiar() {
        _movimientos.value = emptyList()
    }
}


// =====================================================
// PANTALLA HISTORIAL
// =====================================================

@Composable
fun HistorialOperadorScreen(

    sucursalId: String,

    nombreSucursal: String,

    onVolver: () -> Unit

) {

    val movimientos by
    HistorialOperadorStore.movimientos.collectAsStateWithLifecycle()

    var filtro by remember {
        mutableStateOf("TODOS")
    }

    val movimientosSucursal = movimientos.filter {
        it.sucursalId == sucursalId
    }

    val movimientosFiltrados = movimientosSucursal.filter {

        when (filtro) {

            "ENTRADAS" -> {
                it.tipo == TipoMovimiento.ENTRADA
            }

            "SALIDAS" -> {
                it.tipo == TipoMovimiento.SALIDA
            }

            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoHistorial)
    ) {

        // ============================================
        // ENCABEZADO
        // ============================================

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulHistorial)
                .padding(20.dp)
        ) {

            TextButton(
                onClick = onVolver
            ) {
                Text(
                    text = "‹ Volver",
                    color = Color.White
                )
            }

            Text(
                text = "Historial de movimientos",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = nombreSucursal,
                color = Color(0xFFBCD5E4)
            )
        }

        // ============================================
        // CONTENIDO
        // ============================================

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Text(
                text = "Movimientos registrados",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AzulHistorial
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Registros temporales de demostración.",
                color = GrisHistorial,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(16.dp))

            // ========================================
            // RESUMEN
            // ========================================

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "Total de movimientos",
                        color = GrisHistorial
                    )

                    Text(
                        text = "${movimientosSucursal.size}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulHistorial
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Entradas: ${
                            movimientosSucursal.count {
                                it.tipo == TipoMovimiento.ENTRADA
                            }
                        }",
                        color = VerdeHistorial
                    )

                    Text(
                        text = "Salidas: ${
                            movimientosSucursal.count {
                                it.tipo == TipoMovimiento.SALIDA
                            }
                        }",
                        color = RojoHistorial
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ========================================
            // FILTROS
            // ========================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                listOf(
                    "TODOS",
                    "ENTRADAS",
                    "SALIDAS"
                ).forEach { opcion ->

                    FilterChip(
                        selected = filtro == opcion,
                        onClick = {
                            filtro = opcion
                        },
                        label = {
                            Text(
                                text = opcion,
                                fontSize = 11.sp
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ========================================
            // LISTA DE MOVIMIENTOS
            // ========================================

            if (movimientosFiltrados.isEmpty()) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {

                    Text(
                        text = "No existen movimientos " +
                                "para el filtro seleccionado.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        color = GrisHistorial,
                        textAlign = TextAlign.Center
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        items = movimientosFiltrados,
                        key = { it.idDemostracion }
                    ) { movimiento ->

                        TarjetaMovimientoHistorial(
                            movimiento = movimiento
                        )
                    }
                }
            }
        }
    }
}


// =====================================================
// TARJETA DE MOVIMIENTO
// =====================================================

@Composable
private fun TarjetaMovimientoHistorial(

    movimiento: ComprobanteMovimientoDemo

) {

    val esEntrada =
        movimiento.tipo == TipoMovimiento.ENTRADA

    val colorMovimiento =
        if (esEntrada) VerdeHistorial else RojoHistorial

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            Color(0xFFDCE5ED)
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = if (esEntrada) {
                    "ENTRADA DE INVENTARIO"
                } else {
                    "SALIDA DE INVENTARIO"
                },
                color = colorMovimiento,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Text(
                text = movimiento.nombreProducto,
                color = AzulHistorial,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Text(
                text = "Código: ${movimiento.codigoProducto}",
                color = GrisHistorial
            )

            HorizontalDivider()

            Text(
                text = "Cantidad: ${movimiento.cantidad} unidades",
                color = AzulHistorial,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Stock anterior: ${movimiento.stockAnterior}",
                color = GrisHistorial
            )

            Text(
                text = "Stock previsto: ${movimiento.stockPrevisto}",
                color = GrisHistorial
            )

            Text(
                text = "Motivo: ${movimiento.motivo}",
                color = GrisHistorial
            )

            if (movimiento.referencia.isNotBlank()) {

                Text(
                    text = "Referencia: ${movimiento.referencia}",
                    color = GrisHistorial
                )
            }

            HorizontalDivider()

            Text(
                text = "Operador: ${movimiento.operadorId}",
                fontSize = 12.sp,
                color = GrisHistorial
            )

            Text(
                text = "Fecha UTC: ${movimiento.fechaUtc}",
                fontSize = 12.sp,
                color = GrisHistorial
            )

            Text(
                text = "Comprobante: ${movimiento.idDemostracion}",
                fontSize = 12.sp,
                color = AzulBotonHistorial
            )

            Text(
                text = "Simulación: no modifica el inventario oficial.",
                fontSize = 11.sp,
                color = GrisHistorial
            )
        }
    }
}