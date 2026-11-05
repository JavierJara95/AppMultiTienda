package com.duoc.appmultitienda

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duoc.appmultitienda.core.data.LocalInventarioGateway
import com.duoc.appmultitienda.core.model.CatalogoProductoRequest
import com.duoc.appmultitienda.core.model.FalloDatos
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Requiere teléfono/emulador. Usa exclusivamente la base Room existente. */
@RunWith(AndroidJUnit4::class)
class CatalogoAdminInstrumentadoTest {
    @Test fun adminCreaEditaDesactivaYConservaDatosEnRoom() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val repo = LocalInventarioGateway(ctx)
        val sku = "TEST-" + System.currentTimeMillis().toString().takeLast(8)
        val barras = "78" + System.currentTimeMillis().toString().takeLast(11)
        val request = CatalogoProductoRequest(sku, barras, "Producto de prueba", "Pruebas", "ud",
            "Pasillo de pruebas", 4, listOf("SUC-01", "SUC-02"))
        repo.login("op.alameda", "demo123")
        try {
            repo.guardarProducto(request, esNuevo = true)
            fail("Operador no debe crear productos")
        } catch (e: FalloDatos) { assertEquals("ROL", e.codigo) }
        repo.logout()
        repo.login("gerencia", "demo123")
        try {
            repo.guardarProducto(request, esNuevo = true)
            fail("Gerencia original no debe crear productos")
        } catch (e: FalloDatos) { assertEquals("ROL", e.codigo) }
        repo.logout()
        repo.login("admin", "demo123")
        repo.guardarProducto(request, esNuevo = true)
        assertEquals(2, repo.datos.value.snapshot.existencias.count { it.producto.sku == sku })
        repo.guardarProducto(request.copy(nombre = "Producto actualizado", minimo = 7), esNuevo = false)
        repo.cambiarEstadoProducto(sku, activo = false)
        repo.logout()
        val reabierto = LocalInventarioGateway(ctx)
        reabierto.login("admin", "demo123")
        val registros = reabierto.datos.value.snapshot.existencias.filter { it.producto.sku == sku }
        assertEquals(2, registros.size)
        assertTrue(registros.all { it.producto.inactivo == true && it.minimo == 7 && it.cantidad == 0 })
        assertTrue(registros.all { it.producto.nombre == "Producto actualizado" })
        reabierto.cambiarEstadoProducto(sku, activo = true)
        assertTrue(reabierto.datos.value.snapshot.existencias.filter { it.producto.sku == sku }
            .all { it.producto.inactivo != true })
    }
}
