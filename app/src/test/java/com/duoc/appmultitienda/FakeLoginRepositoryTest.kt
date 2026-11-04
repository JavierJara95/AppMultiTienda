package com.duoc.appmultitienda.gestion.model

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FakeLoginRepositoryTest {
    @Test fun adminYGerenciaTienenRolesDiferentes() = runBlocking {
        val repo = FakeLoginRepository()
        assertEquals(Rol.ADMIN, repo.iniciarSesion("admin", "demo123")?.rol)
        assertEquals(Rol.GERENCIA, repo.iniciarSesion("gerencia", "demo123")?.rol)
        assertNull(repo.iniciarSesion("admin", "incorrecta"))
    }
}
