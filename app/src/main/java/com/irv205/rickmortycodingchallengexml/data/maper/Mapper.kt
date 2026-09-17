package com.irv205.rickmortycodingchallengexml.data.maper

import com.irv205.rickmortycodingchallengexml.data.model.CharacterDTO
import com.irv205.rickmortycodingchallengexml.data.model.CharactersResponseDTO
import com.irv205.rickmortycodingchallengexml.domain.model.Character
import com.irv205.rickmortycodingchallengexml.domain.model.CharactersPage

/**
 * ============================================================
 * MAPPER: CONVERSIÓN ENTRE CAPAS DE LA ARQUITECTURA
 * ============================================================
 * Es una mala práctica exponer los DTO de la capa de datos (que dependen
 * del servidor) directamente en la capa de dominio / presentación. Para eso
 * existen los mappers: funciones que copian los campos de un modelo a otro.
 *
 * Dos reglas de oro en arquitectura limpia:
 *   1) La capa de dominio NO debe conocer los DTO (solo los modelos de dominio).
 *   2) La conversión siempre va de externo (DTO) hacia interno (dominio).
 *
 * En Kotlin estas funciones se definen como "extension functions"
 * (fun NombreTipo.nombreExtension()), lo que las hace fáciles de reutilizar:
 *   val dto: CharacterDTO
 *   val dominio: Character = dto.toDomain()
 */

/**
 * Convierte un CharacterDTO (modelo de datos, de la API) en un Character
 * (modelo de dominio, el que usa la UI). Solo copiamos los campos que la
 * interfaz necesita; si algún campo del DTO no se usa, no se copia.
 */
fun CharacterDTO.toDomain(): Character {
    return Character(
        id = this.id,
        name = this.name,
        status = this.status,
        image = this.image
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