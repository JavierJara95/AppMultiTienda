package com.duoc.appmultitienda.operador.repository

import com.duoc.appmultitienda.operador.model.SucursalOperador

interface SucursalOperadorRepository {

    suspend fun obtenerSucursales(): List<SucursalOperador>
}

class FakeSucursalOperadorRepository :
    SucursalOperadorRepository {

    override suspend fun obtenerSucursales(): List<SucursalOperador> {

        return listOf(

            SucursalOperador(
                id = "SUC-01",
                nombre = "Tienda Alameda",
                direccion = "Av. Alameda 120",
                activa = true,
                asignada = true
            ),

            SucursalOperador(
                id = "SUC-02",
                nombre = "Bodega Norte",
                direccion = "Parque Industrial 45",
                activa = true,
                asignada = true
            ),

            SucursalOperador(
                id = "SUC-03",
                nombre = "Tienda Mirador",
                direccion = "Calle Mirador 86",
                activa = false,
                asignada = true
            )
        )
    }
}