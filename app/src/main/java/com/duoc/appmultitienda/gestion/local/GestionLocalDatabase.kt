package com.duoc.appmultitienda.gestion.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PreferenciaSucursalEntity::class], version = 1, exportSchema = false)
abstract class GestionLocalDatabase : RoomDatabase() {
    abstract fun preferenciaSucursalDao(): PreferenciaSucursalDao

    companion object {
        @Volatile private var instancia: GestionLocalDatabase? = null

        fun obtener(context: Context): GestionLocalDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    GestionLocalDatabase::class.java,
                    "gestion_preferencias.db"
                ).build().also { instancia = it }
            }
    }
}
