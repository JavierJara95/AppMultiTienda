package com.duoc.appmultitienda.operador.validation

import com.duoc.appmultitienda.operador.model.ProductoOperador

// =====================================================
// RESULTADO DE UNA VALIDACIÓN
// =====================================================

data class ValidacionOperadorResultado(

    val valido: Boolean,

    val mensaje: String? = null
)


// =====================================================
// VALIDACIONES GENERALES DEL OPERADOR
// =====================================================

object OperadorValidaciones {

    // -------------------------------------------------
    // VALIDAR SUCURSAL
    // -------------------------------------------------

    fun validarSucursal(
        sucursalId: String?
    ): ValidacionOperadorResultado {

        if (sucursalId.isNullOrBlank()) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "Debes seleccionar una sucursal."
            )
        }

        return ValidacionOperadorResultado(
            valido = true
        )
    }


    // -------------------------------------------------
    // VALIDAR BÚSQUEDA
    // -------------------------------------------------

    fun validarBusqueda(
        consulta: String
    ): ValidacionOperadorResultado {

        val texto = consulta.trim()

        if (texto.isBlank()) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "Ingresa un código o nombre de producto."
            )
        }

        if (texto.length > 100) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "La búsqueda no puede superar 100 caracteres."
            )
        }

        return ValidacionOperadorResultado(
            valido = true
        )
    }


    // -------------------------------------------------
    // VALIDAR CÓDIGO ESCANEADO
    // -------------------------------------------------

    fun validarCodigoEscaneado(
        codigo: String
    ): ValidacionOperadorResultado {

        val texto = codigo.trim()

        if (texto.isBlank()) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "No se reconoció un código de barras."
            )
        }

        if (texto.length > 100) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "El código escaneado es demasiado largo."
            )
        }

        return ValidacionOperadorResultado(
            valido = true
        )
    }


    // -------------------------------------------------
    // VALIDAR PRODUCTO
    // -------------------------------------------------

    fun validarProducto(
        producto: ProductoOperador?,
        sucursalId: String
    ): ValidacionOperadorResultado {

        if (producto == null) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "Debes seleccionar un producto."
            )
        }

        if (producto.sucursalId != sucursalId) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "El producto no pertenece a la sucursal activa."
            )
        }

        if (producto.stockActual < 0) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "El stock registrado es incorrecto."
            )
        }

        return ValidacionOperadorResultado(
            valido = true
        )
    }


    // -------------------------------------------------
    // VALIDAR CANTIDAD
    // -------------------------------------------------

    fun validarCantidad(
        cantidadTexto: String,
        permitirCero: Boolean = false
    ): ValidacionOperadorResultado {

        if (cantidadTexto.isBlank()) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "Debes ingresar una cantidad."
            )
        }

        val cantidad = cantidadTexto.toIntOrNull()

        if (cantidad == null) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "Ingresa un número entero válido."
            )
        }

        if (cantidad < 0) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "La cantidad no puede ser negativa."
            )
        }

        if (!permitirCero && cantidad == 0) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "La cantidad debe ser mayor que cero."
            )
        }

        return ValidacionOperadorResultado(
            valido = true
        )
    }


    // -------------------------------------------------
    // VALIDAR ENTRADA DE INVENTARIO
    // -------------------------------------------------

    fun validarEntrada(
        producto: ProductoOperador,
        cantidadTexto: String,
        motivo: String
    ): ValidacionOperadorResultado {

        val resultadoCantidad = validarCantidad(
            cantidadTexto = cantidadTexto
        )

        if (!resultadoCantidad.valido) {
            return resultadoCantidad
        }

        if (motivo.trim().length < 3) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "Debes ingresar un motivo de al menos 3 caracteres."
            )
        }

        val cantidad = cantidadTexto.toInt()

        if (producto.stockActual < 0) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "El stock del producto es incorrecto."
            )
        }

        if (cantidad > Int.MAX_VALUE - producto.stockActual) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "La cantidad supera el límite permitido."
            )
        }

        return ValidacionOperadorResultado(
            valido = true
        )
    }


    // -------------------------------------------------
    // VALIDAR SALIDA DE INVENTARIO
    // -------------------------------------------------

    fun validarSalida(
        producto: ProductoOperador,
        cantidadTexto: String,
        motivo: String
    ): ValidacionOperadorResultado {

        val resultadoCantidad = validarCantidad(
            cantidadTexto = cantidadTexto
        )

        if (!resultadoCantidad.valido) {
            return resultadoCantidad
        }

        if (motivo.trim().length < 3) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "Debes ingresar un motivo de al menos 3 caracteres."
            )
        }

        val cantidad = cantidadTexto.toInt()

        if (producto.stockActual < 0) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "El stock del producto es incorrecto."
            )
        }

        if (cantidad > producto.stockActual) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "No existe stock suficiente para realizar la salida."
            )
        }

        return ValidacionOperadorResultado(
            valido = true
        )
    }


    // -------------------------------------------------
    // VALIDAR CONTEO FÍSICO
    // -------------------------------------------------

    fun validarConteo(
        producto: ProductoOperador,
        cantidadTexto: String,
        observacion: String
    ): ValidacionOperadorResultado {

        val resultadoCantidad = validarCantidad(
            cantidadTexto = cantidadTexto,
            permitirCero = true
        )

        if (!resultadoCantidad.valido) {
            return resultadoCantidad
        }

        val cantidad = cantidadTexto.toInt()

        if (producto.stockActual < 0) {

            return ValidacionOperadorResultado(
                valido = false,
                mensaje = "El stock registrado es incorrecto."
            )
        }

        if (cantidad != producto.stockActual) {

            if (observacion.trim().length < 5) {

                return ValidacionOperadorResultado(
                    valido = false,
                    mensaje = "Debes explicar la diferencia con al menos 5 caracteres."
                )
            }
        }

        return ValidacionOperadorResultado(
            valido = true
        )
    }
}