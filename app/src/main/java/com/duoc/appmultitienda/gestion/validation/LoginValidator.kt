package com.duoc.appmultitienda.gestion.validation

data class LoginErrors(val usuario: String? = null, val clave: String? = null) {
    val hayErrores: Boolean get() = usuario != null || clave != null
}

object LoginValidator {
    fun validar(usuario: String, clave: String): LoginErrors = LoginErrors(
        usuario = if (usuario.isBlank()) "Ingresa tu usuario" else null,
        clave = if (clave.isBlank()) "Ingresa tu contraseña" else null
    )
}
