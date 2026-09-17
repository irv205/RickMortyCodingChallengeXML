package com.irv205.rickmortycodingchallengexml.domain.model

/**
 * ============================================================
 * CAPA DE DOMINIO: PÁGINA DE PERSONAJES
 * ============================================================
 * Envuelve lo que devuelve la API por página (un "CharactersResponseDTO")
 * traducido al modelo de dominio. Conserva la info de paginación que trae
 * InfoDTO sin depender de los DTO de la capa de datos.
 *
 * PAGINACIÓN DIRIGIDA POR EL SERVIDOR:
 *   - nextPageUrl es el campo "info.next" tal cual lo manda la API: la URL
 *     exacta de la siguiente página (o null si no hay más). El ViewModel NO
 *     calcula la siguiente página (nada de currentPage + 1): solo guarda esta
 *     URL y la pide cuando el usuario llega al final. La API es la única
 *     fuente de verdad de la paginación.
 */
data class CharactersPage(
    val characters: List<Character>, // Personajes de ESTA página
    val nextPageUrl: String?         // URL de la siguiente página (null si es la última)
)