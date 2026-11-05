package com.duoc.appmultitienda.operador.validation

enum class TipoMovimiento {
    ENTRADA,
    SALIDA
}

data class ResultadoValidacionMovimiento(
    val errorCantidad: String? = null,
    val errorMotivo: String? = null
) {
    val esValido: Boolean
        get() = errorCantidad == null && errorMotivo == null
}

object MovimientoValidator {

    fun validar(
        tipo: TipoMovimiento,
        cantidadTexto: String,
        motivo: String,
        stockActual: Int
    ): ResultadoValidacionMovimiento {

        val cantidad = cantidadTexto.toIntOrNull()

        val errorCantidad = when {

            cantidadTexto.isBlank() ->
                "Debes ingresar una cantidad."

            cantidad == null ->
                "Ingresa un número entero válido."

            cantidad <= 0 ->
                "La cantidad debe ser mayor que cero."

            tipo == TipoMovimiento.SALIDA &&
                    cantidad > stockActual ->
                "La salida supera el stock disponible."

            tipo == TipoMovimiento.ENTRADA &&
                    cantidad > Int.MAX_VALUE - stockActual ->
                "La cantidad supera el límite permitido."

            else -> null
        }

        val errorMotivo = when {

            motivo.isBlank() ->
                "Debes indicar el motivo del movimiento."

            motivo.trim().length < 3 ->
                "El motivo debe tener al menos 3 caracteres."

            else -> null
        }

        return ResultadoValidacionMovimiento(
            errorCantidad = errorCantidad,
            errorMotivo = errorMotivo
        )
    }
}