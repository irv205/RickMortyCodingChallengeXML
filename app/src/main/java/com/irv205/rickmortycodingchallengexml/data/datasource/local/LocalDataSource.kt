package com.irv205.rickmortycodingchallengexml.data.datasource.local

import com.irv205.rickmortycodingchallengexml.data.local.CharacterDao
import com.irv205.rickmortycodingchallengexml.data.maper.toDomain
import com.irv205.rickmortycodingchallengexml.data.maper.toEntity
import com.irv205.rickmortycodingchallengexml.domain.model.Character
import javax.inject.Inject

/**
 * ============================================================
 * CAPA DE DATOS: FUENTE DE DATOS LOCAL (ROOM / OFFLINE)
 * ============================================================
 * Análoga a RemoteDataSource pero para la BD local. Es la única clase que
 * habla con Room ([CharacterDao]); el repositorio no sabe que aquí debajo
 * hay una tabla SQLite.
 *
 * Trabaja con el modelo de dominio (Character) y se encarga de convertir a
 * CharacterEntity con el mapper antes de tocar la BD.
 *
 * @Inject en el constructor: Hilt le inyecta el CharacterDao automáticamente
 * (lo fabrica DataModule). La clase no tiene estado, por eso no usa @Singleton.
 */
class LocalDataSource @Inject constructor(private val dao: CharacterDao) {

    /**
     * Devuelve TODOS los personajes guardados (ordenados por id).
     * Es la lectura del MOD OFFLINE.
     */
    suspend fun getCharacters(): List<Character> {
        return dao.getCharacters().map { it.toDomain() }
    }

    /**
     * Guarda una lista de personajes en la BD. Con REPLACE, si un personaje
     * ya existe se actualiza y nunca se duplica.
     */
    suspend fun saveCharacters(characters: List<Character>) {
        dao.insertAll(characters.map { it.toEntity() })
    }
}