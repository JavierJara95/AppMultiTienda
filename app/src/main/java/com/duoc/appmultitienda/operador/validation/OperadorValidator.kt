package com.duoc.appmultitienda.operador.validation

object OperadorValidator {
    fun codigo(text: String): String? =
        if (text.trim().matches(Regex("[A-Za-z0-9-]{3,40}"))) null
        else "Ingresa un código de barras o referencia válida."

    fun conteo(text: String): String? =
        if (text.matches(Regex("[0-9]+")) && text.toIntOrNull()?.let { it >= 0 } == true) null
        else "Ingresa un entero mayor o igual a 0. Sin decimales."

    fun movimiento(text: String, tipo: String, saldo: Int): String? {
        val n = text.toIntOrNull()
        return when {
            !text.matches(Regex("[0-9]+")) || n == null || n <= 0 ->
                "Ingresa un entero mayor que 0."
            tipo !in listOf("ENTRADA", "SALIDA") -> "Selecciona entrada o salida."
            tipo == "SALIDA" && n > saldo -> "Supera el stock disponible: máximo $saldo uds."
            tipo == "ENTRADA" && n.toLong() + saldo > Int.MAX_VALUE ->
                "La cantidad supera el límite permitido."
            else -> null
        }
    }

    fun motivo(text: String): String? =
        if (text.trim().length in 5..200) null else "Escribe entre 5 y 200 caracteres."

    fun observacion(text: String): String? =
        if (text.length <= 200) null else "Máximo 200 caracteres."

    fun referencia(text: String): String? =
        if (text.length <= 100) null else "Máximo 100 caracteres."
}
