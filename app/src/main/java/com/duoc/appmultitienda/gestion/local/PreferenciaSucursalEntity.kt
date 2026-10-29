package com.duoc.appmultitienda.gestion.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preferencia_sucursal")
data class PreferenciaSucursalEntity(
    @PrimaryKey val id: Int = 1,
    val codigoSucursal: String
)
