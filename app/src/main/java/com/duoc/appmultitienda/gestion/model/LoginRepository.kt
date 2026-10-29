package com.duoc.appmultitienda.gestion.model

interface LoginRepository {
    suspend fun iniciarSesion(usuario: String, clave: String): UsuarioSesion?
}

/** Repositorio exclusivamente de demostración. No usar contraseñas embebidas en producción. */
class FakeLoginRepository : LoginRepository {
    private val cuentas = mapOf(
        "op.alameda" to Pair("demo123", UsuarioSesion("OPR-017", Rol.OPERADOR)),
        "enc.alameda" to Pair("demo123", UsuarioSesion("ENC-004", Rol.ENCARGADO)),
        "gerencia" to Pair("demo123", UsuarioSesion("GER-001", Rol.GERENCIA))
    )

    override suspend fun iniciarSesion(usuario: String, clave: String): UsuarioSesion? {
        val cuenta = cuentas[usuario.trim().lowercase()] ?: return null
        return cuenta.second.takeIf { cuenta.first == clave }
    }
}
