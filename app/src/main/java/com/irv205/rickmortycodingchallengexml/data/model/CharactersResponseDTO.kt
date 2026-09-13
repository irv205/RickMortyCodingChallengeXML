package com.irv205.rickmortycodingchallengexml.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * ============================================================
 * CAPA DE DATOS: MODELOS DTO (Data Transfer Object)
 * ============================================================
 * Las clases de esta carpeta representan EXACTAMENTE la forma en que
 * el servidor de Rick & Morty nos devuelve el JSON. Se llaman DTO
 * porque su único trabajo es "transportar" los datos desde la red
 * hacia nuestra app.
 *
 * La API responde algo así:
 * {
 *   "info":  { "count": 826, "pages": 42, "next": "..." , "prev": null },
 *   "results": [ { "id": 1, "name": "Rick Sanchez", "image": "..." }, ... ]
 * }
 *
 * Moshi (la librería que elegimos en NetworkModule para convertir JSON en objetos)
 * necesita conocer estas clases para poder deserializar la respuesta.
 */

/**
 * Representa la respuesta completa de la API al pedir los personajes (endpoint /character).
 * - "info": metadatos de la paginación (cuántos personajes hay, cuántas páginas, etc.)
 * - "results": la lista real de personajes que llega del servidor.
 *
 * Nota: Moshi puede crear el adapter de esta clase automáticamente con KotlinJsonAdapterFactory
 * (reflexión). Por eso no necesita la anotación @JsonClass(generateAdapter = true).
 */
data class CharactersResponseDTO(
    val info: InfoDTO,
    val results: List<CharacterDTO>
)

/**
 * Metadatos de la paginación de la API.
 * Los campos son nullable (con "? = null") porque la primera página trae "prev" vacío
 * y la última trae "next" vacío. Usar null por defecto evita que falle la deserialización
 * cuando el servidor manda "null" en ese campo.
 */
data class InfoDTO(
    val count: Int? = null,   // Total de personajes en toda la API
    val pages: Int? = null,   // Cantidad total de páginas disponibles
    val next: String? = null, // URL de la siguiente página (null si no hay más)
    val prev: String? = null  // URL de la página anterior (null si es la primera)
)

/**
 * Representa UN personaje tal y como llega del servidor.
 *
 * @JsonClass(generateAdapter = true): le dice a Moshi que genere el código de deserialización
 * en tiempo de compilación (más rápido y seguro que la reflexión). Es la forma recomendada
 * cuando la clase es fija y conocida.
 *
 * @Json(name = "..."): enlaza la propiedad Kotlin con el nombre exacto del campo JSON.
 * Ejemplo: el campo "image" del JSON se guarda en la propiedad "image".
 * Si el nombre JSON y el de Kotlin coinciden, la anotación es opcional, pero así
 * el código queda autocontenido y a prueba de cambios de nombres.
 *
 * La mayoría de campos son no-nullable: asumimos que la API siempre los envía.
 */
@JsonClass(generateAdapter = true)
data class CharacterDTO(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "status") val status: String, // "Alive", "Dead" o "unknown"
    @Json(name = "species") val species: String, // "Human", "Alien", etc.
    @Json(name = "type") val type: String,
    @Json(name = "gender") val gender: String,
    @Json(name = "origin") val origin: OriginDTO,     // Lugar de origen del personaje
    @Json(name = "location") val location: LocationDTO, // Ubicación actual
    @Json(name = "image") val image: String,         // URL de la imagen del personaje
    @Json(name = "episode") val episode: List<String>, // URLs de los episodios donde aparece
    @Json(name = "url") val url: String,             // URL del personaje en la API
    @Json(name = "created") val created: String      // Fecha de creación en la API
)

/**
 * Origen del personaje: nombre del lugar y su URL en la API.
 */
@JsonClass(generateAdapter = true)
data class OriginDTO(
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String
)

/**
 * Ubicación actual del personaje.
 * Aunque aquí parece igual que OriginDTO, en la API son objetos distintos
 * con información propia, por eso se modelan por separado.
 */
@JsonClass(generateAdapter = true)
data class LocationDTO(
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String
)