package com.irv205.rickmortycodingchallengexml.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

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
     * INSERT OR REPLACE de una lista completa de personajes.
     * Se usa al recibir cada página de la API para dejar la BD al día.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)
}