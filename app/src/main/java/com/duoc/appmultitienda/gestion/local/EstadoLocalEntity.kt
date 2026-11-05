package com.duoc.appmultitienda.gestion.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/** Estado serializado dentro de la BASE ROOM EXISTENTE gestion_preferencias.db. */
@Entity(tableName = "estado_multitienda")
data class EstadoLocalEntity(@PrimaryKey val clave: String, val json: String)

@Dao
interface EstadoLocalDao {
    @Query("SELECT json FROM estado_multitienda WHERE clave = :clave LIMIT 1")
    suspend fun leer(clave: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(valor: EstadoLocalEntity)

    @Query("DELETE FROM estado_multitienda WHERE clave = :clave")
    suspend fun borrar(clave: String)
}
