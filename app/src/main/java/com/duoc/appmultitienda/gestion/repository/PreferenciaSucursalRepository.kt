package com.duoc.appmultitienda.gestion.repository

import com.duoc.appmultitienda.gestion.local.PreferenciaSucursalDao
import com.duoc.appmultitienda.gestion.local.PreferenciaSucursalEntity
import kotlinx.coroutines.flow.Flow

class PreferenciaSucursalRepository(private val dao: PreferenciaSucursalDao) {
    val sucursalGuardada: Flow<String?> = dao.observarCodigoSucursal()

    suspend fun guardar(codigo: String) {
        require(codigo.isNotBlank()) { "Selecciona una sucursal" }
        dao.guardar(PreferenciaSucursalEntity(codigoSucursal = codigo.trim()))
    }

    suspend fun borrar() = dao.borrar()
}
