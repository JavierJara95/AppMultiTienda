package com.duoc.appmultitienda.gestion.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Reutiliza gestion_preferencias.db; NO crea una base de datos nueva. */
@Database(
    entities = [PreferenciaSucursalEntity::class, EstadoLocalEntity::class],
    version = 3,
    exportSchema = false
)
abstract class GestionLocalDatabase : RoomDatabase() {
    abstract fun preferenciaSucursalDao(): PreferenciaSucursalDao
    abstract fun estadoLocalDao(): EstadoLocalDao

    companion object {
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `estado_multitienda` (`clave` TEXT NOT NULL, `json` TEXT NOT NULL, PRIMARY KEY(`clave`))")
            }
        }

        // Compatibilidad con la entrega adaptada anterior (Room v2).
        private val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `estado_multitienda` (`clave` TEXT NOT NULL, `json` TEXT NOT NULL, PRIMARY KEY(`clave`))")
            }
        }

        @Volatile private var instancia: GestionLocalDatabase? = null

        fun obtener(context: Context): GestionLocalDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    GestionLocalDatabase::class.java,
                    "gestion_preferencias.db"
                ).addMigrations(MIGRACION_1_2, MIGRACION_2_3).build().also { instancia = it }
            }
    }
}
