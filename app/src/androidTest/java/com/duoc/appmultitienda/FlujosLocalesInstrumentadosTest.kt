package com.duoc.appmultitienda

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duoc.appmultitienda.core.data.LocalInventarioGateway
import com.duoc.appmultitienda.core.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Estas pruebas requieren un emulador o teléfono. No implican una API remota. */
@RunWith(AndroidJUnit4::class)
class FlujosLocalesInstrumentadosTest {
    @Test fun cuatroRoles_Permisos_Conteo_Persistencia_Informes() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val repo = LocalInventarioGateway(ctx)
        repo.login("op.alameda", "demo123")
        assertEquals("OPERADOR", repo.datos.value.usuario?.rol)
        repo.seleccionar("SUC-01")
        val item = repo.buscar("ARR-002")
        val contado = (item.cantidad - 1).coerceAtLeast(0)
        val conteo = repo.contar(ConteoRequest("SUC-01", "ARR-002", contado,
            "Verificación física instrumentada", item.version, UUID.randomUUID().toString()))
        assertEquals("PENDIENTE", conteo.estado)
        assertEquals(item.cantidad, repo.buscar("ARR-002").cantidad)
        try {
            repo.resolver(conteo.id, ResolverRequest(true,"No autorizado",item.version,UUID.randomUUID().toString()))
            fail("Operador no debe aprobar")
        } catch (e: FalloDatos) { assertEquals("ROL", e.codigo) }
        repo.logout()

        repo.login("enc.alameda", "demo123")
        assertEquals("ENCARGADO", repo.datos.value.usuario?.rol)
        repo.seleccionar("SUC-01")
        val res = repo.resolver(conteo.id, ResolverRequest(true,"Diferencia aprobada por el encargado",item.version, UUID.randomUUID().toString()))
        assertEquals("APROBADO", res.conteo.estado)
        assertEquals(contado, repo.buscar("ARR-002").cantidad)
        repo.logout()

        repo.login("gerencia", "demo123")
        assertEquals("GERENCIA", repo.datos.value.usuario?.rol)
        assertTrue(repo.datos.value.snapshot.movimientos.any { it.conteoId == conteo.id })
        try {
            repo.cambiarSucursal("SUC-03", EstadoTiendaRequest(true, UUID.randomUUID().toString()))
            fail("Gerencia original no administra sucursales")
        } catch (e: FalloDatos) { assertEquals("ROL", e.codigo) }
        repo.logout()

        repo.login("admin", "demo123")
        assertEquals("ADMIN", repo.datos.value.usuario?.rol)
        val activa = repo.cambiarSucursal("SUC-03", EstadoTiendaRequest(true, UUID.randomUUID().toString()))
        assertTrue(activa.activa)
        repo.logout()

        // Segunda instancia: consulta lo guardado en la MISMA base Room existente.
        val repoReabierto = LocalInventarioGateway(ctx)
        repoReabierto.login("admin", "demo123")
        assertTrue(repoReabierto.datos.value.snapshot.sucursales.any { it.id == "SUC-03" && it.activa })
        assertEquals(contado, repoReabierto.datos.value.snapshot.existencias.first {
            it.tiendaId == "SUC-01" && it.producto.sku == "ARR-002"
        }.cantidad)
    }
}
