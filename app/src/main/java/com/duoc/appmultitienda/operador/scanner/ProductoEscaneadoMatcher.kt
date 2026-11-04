package com.duoc.appmultitienda.operador.scanner

import com.duoc.appmultitienda.operador.model.ProductoOperador

/**
 * Busca un producto utilizando el código reconocido
 * por el escáner de MultiTienda.
 *
 * Solamente acepta coincidencias exactas
 * dentro de la sucursal seleccionada.
 */
object ProductoEscaneadoMatcher {

    fun buscar(
        codigoEscaneado: String,
        sucursalId: String,
        productos: List<ProductoOperador>
    ): ProductoOperador? {

        // Limpiar código recibido
        val codigoLimpio = codigoEscaneado.trim()

        // Validar información necesaria
        if (codigoLimpio.isBlank()) {
            return null
        }

        if (sucursalId.isBlank()) {
            return null
        }

        // Buscar producto en la sucursal correcta
        return productos.firstOrNull { producto ->

            val mismaSucursal =
                producto.sucursalId == sucursalId

            val coincideCodigoInterno =
                producto.codigo.equals(
                    codigoLimpio,
                    ignoreCase = true
                )

            val coincideCodigoBarras =
                producto.codigoBarras.trim() == codigoLimpio

            mismaSucursal &&
                    (coincideCodigoInterno || coincideCodigoBarras)
        }
    }
}