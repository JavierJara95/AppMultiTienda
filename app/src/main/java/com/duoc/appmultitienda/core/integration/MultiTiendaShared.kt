package com.duoc.appmultitienda.core.integration

import com.duoc.appmultitienda.gestion.repository.FuenteGestionCompartida
import com.duoc.appmultitienda.operador.model.*
import com.duoc.appmultitienda.operador.repository.*
import kotlinx.coroutines.flow.first

/** Una única fuente EN MEMORIA compartida entre ambos módulos, solo para demostración. */
object MultiTiendaShared {
    val fuente: FuenteGestionCompartida by lazy { FuenteGestionCompartida() }
}

class ProductoOperadorCompartido : ProductoOperadorRepository {
    private val catalogo = FakeProductoOperadorRepository()
    override suspend fun buscarProductos(sucursalId: String, consulta: String): List<ProductoOperador> =
        catalogo.buscarProductos(sucursalId, consulta).map { producto ->
            val saldo = MultiTiendaShared.fuente.consultarSaldo(producto.sucursalId, producto.codigo)
            if (saldo != null) producto.copy(stockActual = saldo) else producto
        }
}

class ConteosOperadorCompartidos : ConteoFisicoRepository {
    override suspend fun registrarConteo(conteo: ConteoFisicoOperador): ConteoFisicoOperador {
        val guardado = MultiTiendaShared.fuente.registrarConteoOperador(conteo)
        return conteo.copy(
            id = guardado.idConteo, fechaUtc = guardado.fechaUtc,
            estado = EstadoConteoFisico.PENDIENTE_APROBACION
        )
    }
    override suspend fun obtenerConteosPendientes(sucursalId: String): List<ConteoFisicoOperador> =
        MultiTiendaShared.fuente.observarConteos().let { flow ->
            flow.first().filter {
                it.idSucursal == sucursalId &&
                it.estado == com.duoc.appmultitienda.gestion.model.EstadoConteo.PENDIENTE
            }.map {
                ConteoFisicoOperador(it.idConteo, it.idSucursal, it.codigoProducto,
                    it.nombreProducto, it.stockOficial, it.stockContado, it.observacion,
                    it.operadorId, it.fechaUtc, EstadoConteoFisico.PENDIENTE_APROBACION)
            }
        }
}

class MovimientosOperadorCompartidos : ConfirmacionMovimientoDemoRepository {
    private val validador = FakeConfirmacionMovimientoDemoRepository()
    override suspend fun simularConfirmacion(
        producto: ProductoOperador,
        tipo: com.duoc.appmultitienda.operador.validation.TipoMovimiento,
        cantidad: Int, motivo: String, referencia: String, operadorId: String
    ): ComprobanteMovimientoDemo {
        val comprobante = validador.simularConfirmacion(
            producto, tipo, cantidad, motivo, referencia, operadorId
        )
        MultiTiendaShared.fuente.registrarMovimientoOperador(comprobante)
        return comprobante
    }
}
