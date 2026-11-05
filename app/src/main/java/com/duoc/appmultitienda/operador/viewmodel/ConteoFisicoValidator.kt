package com.duoc.appmultitienda.operador.validation


data class ResultadoValidacionConteo(

    val errorCantidad: String? = null,

    val errorObservacion: String? = null

) {

    val esValido: Boolean
        get() = errorCantidad == null &&
                errorObservacion == null
}


object ConteoFisicoValidator {

    fun validar(

        cantidadTexto: String,

        stockRegistrado: Int,

        observacion: String

    ): ResultadoValidacionConteo {

        // Convertimos el texto ingresado en un número.
        val cantidad = cantidadTexto.toIntOrNull()

        // Comprobamos si la cantidad es correcta.
        val errorCantidad = when {

            cantidadTexto.isBlank() ->
                "Debes ingresar la cantidad contada."

            cantidad == null ->
                "Ingresa un número entero válido."

            cantidad < 0 ->
                "La cantidad no puede ser negativa."

            else -> null
        }

        // Comprobamos si existe una diferencia.
        val existeDiferencia =
            cantidad != null &&
                    cantidad >= 0 &&
                    cantidad != stockRegistrado

        // Revisamos la observación.
        val errorObservacion = when {

            existeDiferencia && observacion.isBlank() ->
                "Debes explicar la diferencia encontrada."

            existeDiferencia && observacion.trim().length < 5 ->
                "La observación debe tener al menos 5 caracteres."

            else -> null
        }

        // Devolvemos el resultado.
        return ResultadoValidacionConteo(

            errorCantidad = errorCantidad,

            errorObservacion = errorObservacion
        )
    }
}