package com.duoc.appmultitienda.gestion.validation

object GestionValidator {
    fun motivo(text: String): String? =
        if (text.trim().length in 5..200) null else "Escribe entre 5 y 200 caracteres."

    fun cantidad(text: String): String? =
        if (text.matches(Regex("[0-9]+")) && text.toIntOrNull()?.let { it > 0 } == true) null
        else "Ingresa un entero mayor que 0."

    fun fechas(desde: String, hasta: String): String? =
        try {
            if (java.time.LocalDate.parse(desde) > java.time.LocalDate.parse(hasta))
                "La fecha inicial debe ser anterior o igual a la final."
            else null
        } catch (_: java.time.format.DateTimeParseException) {
            "Usa fechas válidas con formato AAAA-MM-DD."
        }

    fun login(usuario: String, clave: String): Map<String, String> = buildMap {
        if (usuario.isBlank()) put("usuario", "Ingresa tu usuario.")
        if (clave.isBlank()) put("clave", "Ingresa tu contraseña.")
    }

    fun fechaOpcional(text: String): String? =
        if (text.isBlank()) null
        else
            try {
                java.time.LocalDate.parse(text)
                null
            } catch (_: java.time.format.DateTimeParseException) {
                "Usa una fecha válida AAAA-MM-DD."
            }
}
