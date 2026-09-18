package com.irv205.rickmortycodingchallengexml.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * ============================================================
 * CAPA DE DATOS: BASE DE DATOS ROOM
 * ============================================================
 * @Database es la anotación que define la BD. Recibe:
 *   - entities: las tablas que contiene (aquí solo "characters").
 *   - version:  número de esquema. Si mañana cambias las tablas, subes a 2
 *               y se generarán migraciones.
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
    entities = [CharacterEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Cada tabla se accede mediante su DAO.
     * Room implementa este getter automáticamente.
     */
    abstract fun characterDao(): CharacterDao
}