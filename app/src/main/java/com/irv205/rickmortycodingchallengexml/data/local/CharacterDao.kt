package com.irv205.rickmortycodingchallengexml.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert

/**
 * ============================================================
 * CAPA DE DATOS: DAO (DATA ACCESS OBJECT) DE PERSONAJES
 * ============================================================
 * El DAO es el intermediario entre tu código Kotlin y la BD. Declaras las
 * consultas como funciones y Room genera la implementación real en la compilación.
 *
 * - getCharacters(): trae TODOS los personajes guardados, ordenados por id.
 *   Es la consulta del modo OFFLINE: si hay datos locales, la app los muestra.
 * - insertAll(): guarda una lista de personajes. Con OnConflictStrategy.REPLACE
 *   decimos: "si ya existe un personaje con el mismo id, reemplázalo".
 *   Así las páginas que vamos cargando se acumulan sin duplicados.
 *
 * Son "suspend" para funcionar con corrutinas (Room las ejecuta fuera del
 * hilo principal sin bloquear la UI).
 */
@Dao
interface CharacterDao {

    /**
     * SELECT * FROM characters ORDER BY id ASC
     * Devuelve todos los personajes guardados de menor a mayor id
     * (es decir, en el mismo orden que llegaron de la API).
     */
    @Query("SELECT * FROM characters ORDER BY id ASC")
    suspend fun getCharacters(): List<CharacterEntity>

    /**
     * SELECT * FROM characters WHERE id = :id
     * Busca UN personaje por su id. Devuelve null si no está guardado.
     * Es la consulta del detalle en modo offline: si está en la BD, no hace
     * falta llamar a la red.
     */
    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: Int): CharacterEntity?

    /**
     * INSERT OR REPLACE de una lista completa de personajes.
     * Se usa al recibir cada página de la API para dejar la BD al día.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)

    /**
     * SELECT * FROM page_info WHERE id = 1
     * Devuelve la fila COMPLETA de estado de paginación, o TODO null si la fila
     * NO existe (nunca hemos guardado). Es OBLIGATORIO distinguir los casos:
     *   - fila == null         -> NO hay metadatos (caché de una versión
     *                             anterior): no sabemos si quedan páginas.
     *   - fila.nextPageUrl == null -> caché COMPLETA (llegamos a la última).
     *   - fila.nextPageUrl != null -> caché PARCIAL (dónde reanudar).
     */
    @Query("SELECT * FROM page_info WHERE id = 1")
    suspend fun getPageInfo(): PageInfoEntity?

    /**
     * UPSERT (INSERT OR UPDATE) del estado de paginación.
     * @Upsert: si la fila con id = 1 ya existe la ACTUALIZA; si no, la CREA.
     * Se llama después de cada página descargada, guardando la nueva "nextPageUrl".
     */
    @Upsert
    suspend fun upsertPageInfo(pageInfo: PageInfoEntity)

    /**
     * Borra TODOS los personajes. Se usa en el caso "sin metadatos" para
     * reconstruir la caché DESDE CERO y que no quede divergente (personajes
     * viejos + page_info nuevo = fuentes de la verdad distintas).
     */
    @Query("DELETE FROM characters")
    suspend fun clearCharacters()

    /**
     * Borra el estado de paginación (usado junto a clearCharacters).
     */
    @Query("DELETE FROM page_info")
    suspend fun clearPageInfo()
}