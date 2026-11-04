package com.duoc.appmultitienda.operador.repository

import com.duoc.appmultitienda.operador.model.ComprobanteMovimientoDemo
import com.duoc.appmultitienda.operador.model.ProductoOperador
import com.duoc.appmultitienda.operador.validation.MovimientoValidator
import com.duoc.appmultitienda.operador.validation.TipoMovimiento

import kotlinx.coroutines.delay

import java.time.Instant
import java.util.UUID

interface ConfirmacionMovimientoDemoRepository {

    suspend fun simularConfirmacion(
        producto: ProductoOperador,
        tipo: TipoMovimiento,
        cantidad: Int,
        motivo: String,
        referencia: String,
        operadorId: String
    ): ComprobanteMovimientoDemo
}


class FakeConfirmacionMovimientoDemoRepository :
    ConfirmacionMovimientoDemoRepository {

    override suspend fun simularConfirmacion(
        producto: ProductoOperador,
        tipo: TipoMovimiento,
        cantidad: Int,
        motivo: String,
        referencia: String,
        operadorId: String
    ): ComprobanteMovimientoDemo {

        // Simulamos el tiempo de respuesta de un servidor.
        delay(800L)

        // Validamos los datos antes de generar el comprobante.
        val resultado = MovimientoValidator.validar(
            tipo = tipo,
            cantidadTexto = cantidad.toString(),
            motivo = motivo,
            stockActual = producto.stockActual
        )

        if (!resultado.esValido) {
            throw IllegalArgumentException(
                resultado.errorCantidad
                    ?: resultado.errorMotivo
                    ?: "Movimiento inválido."
            )
        }

        // Calculamos cómo quedaría el stock.
        val stockPrevisto = when (tipo) {

            TipoMovimiento.ENTRADA ->
                producto.stockActual + cantidad

            TipoMovimiento.SALIDA ->
                producto.stockActual - cantidad
        }

        // Creamos un identificador ficticio para la demostración.
        val identificador = "DEMO-" +
                UUID.randomUUID()
                    .toString()
                    .take(8)
                    .uppercase()

        // Obtenemos la fecha actual en UTC.
        val fechaActualUtc = Instant.now().toString()

        // Devolvemos el comprobante simulado.
        return ComprobanteMovimientoDemo(

            idDemostracion = identificador,

            sucursalId = producto.sucursalId,

            codigoProducto = producto.codigo,

            nombreProducto = producto.nombre,

            tipo = tipo,

            cantidad = cantidad,

            stockAnterior = producto.stockActual,

            stockPrevisto = stockPrevisto,

            motivo = motivo.trim(),

            referencia = referencia.trim(),

            operadorId = operadorId,

            fechaUtc = fechaActualUtc
        )
    }
}