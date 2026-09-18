package com.irv205.rickmortycodingchallengexml.data.maper

import com.irv205.rickmortycodingchallengexml.data.local.CharacterEntity
import com.irv205.rickmortycodingchallengexml.data.model.CharacterDTO
import com.irv205.rickmortycodingchallengexml.data.model.CharactersResponseDTO
import com.irv205.rickmortycodingchallengexml.domain.model.Character
import com.irv205.rickmortycodingchallengexml.domain.model.CharactersPage
import com.irv205.rickmortycodingchallengexml.domain.model.Location
import com.irv205.rickmortycodingchallengexml.domain.model.Origin

/**
 * ============================================================
 * MAPPER: CONVERSIÓN ENTRE CAPAS DE LA ARQUITECTURA
 * ============================================================
 * Es una mala práctica exponer los DTO de la capa de datos (que dependen
 * del servidor) directamente en la capa de dominio / presentación. Para eso
 * existen los mappers: funciones que copian los campos de un modelo a otro.
 *
 * Dos reglas de oro en arquitectura limpia:
 *   1) La capa de dominio NO debe conocer los DTO ni las entidades de Room
 *      (solo los modelos de dominio).
 *   2) La conversión siempre va de externo (DTO / Entity) hacia interno (dominio).
 *
 * En Kotlin estas funciones se definen como "extension functions"
 * (fun NombreTipo.nombreExtension()), lo que las hace fáciles de reutilizar:
 *   val dto: CharacterDTO
 *   val dominio: Character = dto.toDomain()
 */

/**
 * Convierte un CharacterDTO (modelo de datos, de la API) en un Character
 * (modelo de dominio, el que usa la UI). Copiamos TODOS los campos del
 * personaje (ahora el dominio guarda la información completa).
 */
fun CharacterDTO.toDomain(): Character {
    return Character(
        id = this.id,
        name = this.name,
        status = this.status,
        species = this.species,
        type = this.type,
        gender = this.gender,
        origin = Origin(name = this.origin.name, url = this.origin.url),
        location = Location(name = this.location.name, url = this.location.url),
        image = this.image,
        episode = this.episode,
        url = this.url,
        created = this.created
    )
}

/**
 * Versión en lote: recibe una lista completa de DTO y convierte cada elemento
 * con el mapper individual anterior. Devuelve una lista nueva de modelos de dominio.
 */
fun List<CharacterDTO>.toDomain(): List<Character> {
    return this.map { it.toDomain() }
}

/**
 * Convierte un Character (modelo de dominio) en una CharacterEntity (tabla de
 * Room), para guardar en la BD lo que llega de la API. Inflamos origin/location
 * en columnas separadas porque Room no guarda objetos anidados directamente.
 * Lo usa LocalDataSource.saveCharacters().
 */
fun Character.toEntity(): CharacterEntity {
    return CharacterEntity(
        id = this.id,
        name = this.name,
        status = this.status,
        species = this.species,
        type = this.type,
        gender = this.gender,
        originName = this.origin.name,
        originUrl = this.origin.url,
        locationName = this.location.name,
        locationUrl = this.location.url,
        image = this.image,
        episode = this.episode,
        url = this.url,
        created = this.created
    )
}

/**
 * Convierte una CharacterEntity (fila de Room) en un Character de dominio.
 * Es el camino inverso del anterior: reconstruye los objetos anidados
 * origin/location a partir de las columnas aplanadas.
 */
fun CharacterEntity.toDomain(): Character {
    return Character(
        id = this.id,
        name = this.name,
        status = this.status,
        species = this.species,
        type = this.type,
        gender = this.gender,
        origin = Origin(name = this.originName, url = this.originUrl),
        location = Location(name = this.locationName, url = this.locationUrl),
        image = this.image,
        episode = this.episode,
        url = this.url,
        created = this.created
    )
}

/**
 * Convierte la respuesta COMPLETA de la API (CharactersResponseDTO) en el modelo
 * de dominio CharactersPage. Hace dos cosas:
 *   1) Mapea "results" (CharacterDTO -> Character).
 *   2) Conserva la info de paginación que trae "info" (InfoDTO): la URL exacta
 *      de la siguiente página (info.next), o null si no hay más. Esa URL es la
 *      que el ViewModel pedirá después; la API decide la paginación, no el cliente.
 */
fun CharactersResponseDTO.toDomain(): CharactersPage {
    return CharactersPage(
        characters = this.results.toDomain(),
        nextPageUrl = this.info.next
    )
}