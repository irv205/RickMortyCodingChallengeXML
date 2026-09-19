package com.irv205.rickmortycodingchallengexml.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * ============================================================
 * CAPA DE DATOS: BASE DE DATOS ROOM
 * ============================================================
 * @Database es la anotación que define la BD. Recibe:
 *   - entities: las tablas que contiene:
 *       * "characters" (CharacterEntity) -> los personajes en caché.
 *       * "page_info"  (PageInfoEntity)   -> URL de la siguiente página
 *                                            (estado de la paginación).
 *   - version: número de esquema. Subimos de 1 a 2 porque añadimos la tabla
 *              page_info. Los usuarios con la BD v1 necesitan MIGRARSE, no
 *              perder sus datos: para eso existe MIGRATION_1_2.
 *   - exportSchema = false: NO exportamos el esquema JSON a disco
 *               (opción recomendada para este challenge; en producción se
 *               exporta para versionar migraciones).
 *
 * @TypeConverters(Converters::class): registra los convertidores de tipos
 * (ver Converters.kt) para que Room sepa guardar las List<String>.
 *
 * Es "abstract class": Room genera la implementación real al compilar
 * (con un sufijo _Impl).
 */
@Database(
    entities = [CharacterEntity::class, PageInfoEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Cada tabla se accede mediante su DAO.
     * Room implementa este getter automáticamente.
     */
    abstract fun characterDao(): CharacterDao

    companion object {

        /**
         * MIGRACIÓN v1 -> v2: crea la tabla page_info.
         * Es un objeto Migration que implementa "qué SQL ejecutar" para dejar
         * el esquema viejo igual que el nuevo. En este caso solo es un CREATE
         * TABLE: no tocamos la tabla characters que ya existía (por eso los
         * personajes guardados sobreviven a la actualización).
         * Room VALIDA la migración al arrancar comparando el esquema esperado.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `page_info` (" +
                        "`id` INTEGER NOT NULL PRIMARY KEY, " +
                        "`nextPageUrl` TEXT)"
                )
            }
        }
    }
}