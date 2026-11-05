package com.duoc.appmultitienda.gestion.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenciaSucursalDao {
    @Query("SELECT codigoSucursal FROM preferencia_sucursal WHERE id = 1")
    fun observarCodigoSucursal(): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(preferencia: PreferenciaSucursalEntity)

    @Query("DELETE FROM preferencia_sucursal WHERE id = 1")
    suspend fun borrar()
}
