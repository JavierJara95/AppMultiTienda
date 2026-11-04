package com.duoc.appmultitienda.operador

import com.duoc.appmultitienda.operador.model.ProductoOperador
import com.duoc.appmultitienda.operador.scanner.ProductoEscaneadoMatcher
import com.duoc.appmultitienda.operador.validation.OperadorValidaciones

import org.junit.Assert.*
import org.junit.Test


class OperadorValidacionesTest {

    // ==========================================
    // PRODUCTOS PARA LAS PRUEBAS
    // ==========================================

    private val arroz = ProductoOperador(
        codigo = "ARR-002",
        codigoBarras = "7501234500025",
        nombre = "Arroz largo 1 kg",
        categoria = "Despensa",
        stockActual = 16,
        stockMinimo = 24,
        sucursalId = "SUC-01"
    )

    private val cafe = ProductoOperador(
        codigo = "CAF-001",
        codigoBarras = "7501234500018",
        nombre = "Café molido 500 g",
        categoria = "Despensa",
        stockActual = 48,
        stockMinimo = 12,
        sucursalId = "SUC-01"
    )

    private val arrozOtraSucursal = ProductoOperador(
        codigo = "ARR-002",
        codigoBarras = "7501234500025",
        nombre = "Arroz largo 1 kg",
        categoria = "Despensa",
        stockActual = 50,
        stockMinimo = 24,
        sucursalId = "SUC-02"
    )

    private val productos = listOf(
        arroz,
        cafe,
        arrozOtraSucursal
    )


    // ==========================================
    // PRUEBA 1
    // SUCURSAL OBLIGATORIA
    // ==========================================

    @Test
    fun sucursalVaciaDebeRechazarse() {

        val resultado =
            OperadorValidaciones.validarSucursal("")

        assertFalse(resultado.valido)

        assertNotNull(resultado.mensaje)
    }


    // ==========================================
    // PRUEBA 2
    // SUCURSAL VÁLIDA
    // ==========================================

    @Test
    fun sucursalSeleccionadaDebeAceptarse() {

        val resultado =
            OperadorValidaciones.validarSucursal("SUC-01")

        assertTrue(resultado.valido)
    }


    // ==========================================
    // PRUEBA 3
    // BÚSQUEDA VACÍA
    // ==========================================

    @Test
    fun busquedaVaciaDebeRechazarse() {

        val resultado =
            OperadorValidaciones.validarBusqueda("   ")

        assertFalse(resultado.valido)
    }


    // ==========================================
    // PRUEBA 4
    // CÓDIGO DE BARRAS VÁLIDO
    // ==========================================

    @Test
    fun codigoEscaneadoValidoDebeAceptarse() {

        val resultado =
            OperadorValidaciones.validarCodigoEscaneado(
                "7501234500025"
            )

        assertTrue(resultado.valido)
    }


    // ==========================================
    // PRUEBA 5
    // CANTIDAD NEGATIVA
    // ==========================================

    @Test
    fun cantidadNegativaDebeRechazarse() {

        val resultado =
            OperadorValidaciones.validarCantidad("-5")

        assertFalse(resultado.valido)
    }


    // ==========================================
    // PRUEBA 6
    // CANTIDAD NO NUMÉRICA
    // ==========================================

    @Test
    fun cantidadConLetrasDebeRechazarse() {

        val resultado =
            OperadorValidaciones.validarCantidad("abc")

        assertFalse(resultado.valido)
    }


    // ==========================================
    // PRUEBA 7
    // ENTRADA VÁLIDA
    // ==========================================

    @Test
    fun entradaDeCincoUnidadesDebeAceptarse() {

        val resultado =
            OperadorValidaciones.validarEntrada(
                producto = cafe,
                cantidadTexto = "5",
                motivo = "Reposición de mercadería"
            )

        assertTrue(resultado.valido)
    }


    // ==========================================
    // PRUEBA 8
    // SALIDA SIN STOCK
    // ==========================================

    @Test
    fun salidaMayorAlStockDebeRechazarse() {

        val resultado =
            OperadorValidaciones.validarSalida(
                producto = arroz,
                cantidadTexto = "20",
                motivo = "Venta de productos"
            )

        assertFalse(resultado.valido)
    }


    // ==========================================
    // PRUEBA 9
    // SALIDA VÁLIDA
    // ==========================================

    @Test
    fun salidaDentroDelStockDebeAceptarse() {

        val resultado =
            OperadorValidaciones.validarSalida(
                producto = arroz,
                cantidadTexto = "5",
                motivo = "Venta de productos"
            )

        assertTrue(resultado.valido)
    }


    // ==========================================
    // PRUEBA 10
    // CONTEO CON DIFERENCIA SIN OBSERVACIÓN
    // ==========================================

    @Test
    fun conteoConDiferenciaDebeExigirObservacion() {

        val resultado =
            OperadorValidaciones.validarConteo(
                producto = arroz,
                cantidadTexto = "14",
                observacion = ""
            )

        assertFalse(resultado.valido)
    }


    // ==========================================
    // PRUEBA 11
    // CONTEO CON DIFERENCIA Y OBSERVACIÓN
    // ==========================================

    @Test
    fun conteoConObservacionDebeAceptarse() {

        val resultado =
            OperadorValidaciones.validarConteo(
                producto = arroz,
                cantidadTexto = "14",
                observacion = "Faltan dos bolsas"
            )

        assertTrue(resultado.valido)
    }


    // ==========================================
    // PRUEBA 12
    // BUSCAR PRODUCTO POR CÓDIGO INTERNO
    // ==========================================

    @Test
    fun escanerDebeEncontrarArrozPorCodigoInterno() {

        val encontrado = ProductoEscaneadoMatcher.buscar(
            codigoEscaneado = "ARR-002",
            sucursalId = "SUC-01",
            productos = productos
        )

        assertNotNull(encontrado)

        assertEquals("ARR-002", encontrado?.codigo)

        assertEquals(16, encontrado?.stockActual)
    }


    // ==========================================
    // PRUEBA 13
    // BUSCAR PRODUCTO POR CÓDIGO DE BARRAS
    // ==========================================

    @Test
    fun escanerDebeEncontrarCafePorCodigoBarras() {

        val encontrado = ProductoEscaneadoMatcher.buscar(
            codigoEscaneado = "7501234500018",
            sucursalId = "SUC-01",
            productos = productos
        )

        assertNotNull(encontrado)

        assertEquals("CAF-001", encontrado?.codigo)
    }


    // ==========================================
    // PRUEBA 14
    // NO CONFUNDIR SUCURSALES
    // ==========================================

    @Test
    fun productoDebeRespetarSucursalSeleccionada() {

        val encontrado = ProductoEscaneadoMatcher.buscar(
            codigoEscaneado = "ARR-002",
            sucursalId = "SUC-02",
            productos = productos
        )

        assertNotNull(encontrado)

        assertEquals("SUC-02", encontrado?.sucursalId)

        assertEquals(50, encontrado?.stockActual)
    }


    // ==========================================
    // PRUEBA 15
    // PRODUCTO INEXISTENTE
    // ==========================================

    @Test
    fun codigoInexistenteNoDebeEncontrarProducto() {

        val encontrado = ProductoEscaneadoMatcher.buscar(
            codigoEscaneado = "NOEXISTE999",
            sucursalId = "SUC-01",
            productos = productos
        )

        assertNull(encontrado)
    }


    // ==========================================
    // PRUEBA 16
    // COINCIDENCIA PARCIAL NO PERMITIDA
    // ==========================================

    @Test
    fun escanerNoDebeAceptarCodigoParcial() {

        val encontrado = ProductoEscaneadoMatcher.buscar(
            codigoEscaneado = "ARR",
            sucursalId = "SUC-01",
            productos = productos
        )

        assertNull(encontrado)
    }


    // ==========================================
    // PRUEBA 17
    // PRODUCTO DE OTRA SUCURSAL
    // ==========================================

    @Test
    fun productoDeOtraSucursalDebeRechazarse() {

        val resultado =
            OperadorValidaciones.validarProducto(
                producto = arrozOtraSucursal,
                sucursalId = "SUC-01"
            )

        assertFalse(resultado.valido)
    }


    // ==========================================
    // PRUEBA 18
    // CONTEO EN CERO
    // ==========================================

    @Test
    fun conteoEnCeroConObservacionDebeAceptarse() {

        val resultado =
            OperadorValidaciones.validarConteo(
                producto = arroz,
                cantidadTexto = "0",
                observacion = "No quedan unidades físicas"
            )

        assertTrue(resultado.valido)
    }


    // ==========================================
    // PRUEBA 19
    // ENTRADA CON CANTIDAD CERO
    // ==========================================

    @Test
    fun entradaConCantidadCeroDebeRechazarse() {

        val resultado =
            OperadorValidaciones.validarEntrada(
                producto = cafe,
                cantidadTexto = "0",
                motivo = "Reposición"
            )

        assertFalse(resultado.valido)
    }


    // ==========================================
    // PRUEBA 20
    // CÓDIGO ESCANEADO VACÍO
    // ==========================================

    @Test
    fun codigoEscaneadoVacioDebeRechazarse() {

        val resultado =
            OperadorValidaciones.validarCodigoEscaneado("")

        assertFalse(resultado.valido)
    }
}