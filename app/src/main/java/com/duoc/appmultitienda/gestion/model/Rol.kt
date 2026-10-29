package com.duoc.appmultitienda.gestion.model

enum class Rol(val etiqueta: String) {
    OPERADOR("Operador"),
    ENCARGADO("Encargado"),
    GERENCIA("Gerencia")
}

data class UsuarioSesion(val identificador: String, val rol: Rol)
