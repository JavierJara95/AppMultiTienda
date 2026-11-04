package com.duoc.appmultitienda.operador.repository

import com.duoc.appmultitienda.operador.model.ProductoOperador

interface ProductoOperadorRepository {

    suspend fun buscarProductos(
        sucursalId: String,
        consulta: String
    ): List<ProductoOperador>
}

class FakeProductoOperadorRepository : ProductoOperadorRepository {

    private val productos = listOf(

        ProductoOperador(
            codigo = "CAF-001",
            codigoBarras = "7501234500018",
            nombre = "Café molido 500 g",
            categoria = "Despensa",
            stockActual = 48,
            stockMinimo = 12,
            sucursalId = "SUC-01"
        ),

        ProductoOperador(
            codigo = "ARR-002",
            codigoBarras = "7501234500025",
            nombre = "Arroz largo 1 kg",
            categoria = "Despensa",
            stockActual = 16,
            stockMinimo = 24,
            sucursalId = "SUC-01"
        ),

        ProductoOperador(
            codigo = "ACE-003",
            codigoBarras = "7501234500032",
            nombre = "Aceite vegetal 1 L",
            categoria = "Despensa",
            stockActual = 0,
            stockMinimo = 8,
            sucursalId = "SUC-01"
        ),

        ProductoOperador(
            codigo = "LEC-004",
            codigoBarras = "7501234500049",
            nombre = "Leche entera 1 L",
            categoria = "Lácteos",
            stockActual = 36,
            stockMinimo = 12,
            sucursalId = "SUC-01"
        ),

        ProductoOperador(
            codigo = "GAL-005",
            codigoBarras = "7501234500056",
            nombre = "Galletas de avena 200 g",
            categoria = "Despensa",
            stockActual = 6,
            stockMinimo = 10,
            sucursalId = "SUC-01"
        ),

        ProductoOperador(
            codigo = "CAF-001",
            codigoBarras = "7501234500018",
            nombre = "Café molido 500 g",
            categoria = "Despensa",
            stockActual = 20,
            stockMinimo = 12,
            sucursalId = "SUC-02"
        ),

        ProductoOperador(
            codigo = "ARR-002",
            codigoBarras = "7501234500025",
            nombre = "Arroz largo 1 kg",
            categoria = "Despensa",
            stockActual = 50,
            stockMinimo = 24,
            sucursalId = "SUC-02"
        )
    )

    override suspend fun buscarProductos(
        sucursalId: String,
        consulta: String
    ): List<ProductoOperador> {

        val texto = consulta.trim()

        if (texto.isBlank()) {
            return emptyList()
        }

        return productos.filter { producto ->

            producto.sucursalId == sucursalId && (

                    producto.nombre.contains(
                        texto,
                        ignoreCase = true
                    ) ||

                            producto.codigo.contains(
                                texto,
                                ignoreCase = true
                            ) ||

                            producto.codigoBarras == texto
                    )
        }
    }
}