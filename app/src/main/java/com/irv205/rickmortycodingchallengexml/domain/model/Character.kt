package com.irv205.rickmortycodingchallengexml.domain.model

/**
 * ============================================================
 * CAPA DE DOMINIO: MODELO DE NEGOCIO
 * ============================================================
 * Esta capa es "el corazón" de la app y NO depende de ninguna librería externa
 * (ni Retrofit, ni Moshi, ni Room, ni Android). Solo contiene las reglas de negocio
 * y los modelos que la aplicación entiende.
 *
 * Character es la representación completa de un personaje de Rick & Morty con
 * TODOS los campos que trae la API (id, species, origin, location, episodes...),
 * porque el modo offline los guarda en Room y los usaremos.
 * Se crea en Mapper.kt a partir del CharacterDTO que trae la API
 * (o del CharacterEntity que trae la BD), y es el objeto que viaja hasta el
 * ViewModel y la Vista.
 *
 * IMPORTANTE: "data class" es una clase especial de Kotlin que genera
 * automáticamente métodos útiles: equals(), hashCode(), toString(),
 * copy() y los getters/setters. Perfecta para puras portadoras de datos.
 */
data class Character(
    val id: Int,             // Identificador único del personaje (clave primaria en BD)
    val name: String,        // Nombre del personaje (ej: "Rick Sanchez")
    val status: String,      // Estado actual (ej: "Alive", "Dead", "unknown")
    val species: String,     // Especie (ej: "Human", "Alien")
    val type: String,        // Subtipo o "type" del personaje (puede ser "")
    val gender: String,      // Género (ej: "Male", "Female", "unknown")
    val origin: Origin,      // Lugar de origen (nombre + url)
    val location: Location,  // Ubicación actual (nombre + url)
    val image: String,       // URL de la imagen del personaje
    val episode: List<String>, // URLs de los episodios donde aparece
    val url: String,         // URL del personaje en la API
    val created: String      // Fecha de creación del registro en la API
)

/**
 * Lugar de origen del personaje. Igual que en la API,
 * solo importa el nombre y su URL.
 */
data class Origin(
    val name: String,
    val url: String
)

/**
 * Ubicación actual del personaje.
 * En la API origin y location son objetos distintos, por eso se modelan aparte.
 */
data class Location(
    val name: String,
    val url: String
)