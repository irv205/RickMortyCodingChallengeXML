package com.irv205.rickmortycodingchallengexml.domain.model

/**
 * ============================================================
 * CAPA DE DOMINIO: MODELO DE NEGOCIO
 * ============================================================
 * Esta capa es "el corazón" de la app y NO depende de ninguna librería externa
 * (ni Retrofit, ni Moshi, ni Android). Solo contiene las reglas de negocio
 * y los modelos que la aplicación entiende.
 *
 * Character es la representación de un personaje de Rick & Morty únicamente
 * con los campos que nuestra interfaz necesita (name, status, image).
 * Se crea en Mapper.kt a partir del CharacterDTO que trae la API,
 * y es el objeto que viaja hasta el ViewModel y la Vista.
 *
 * IMPORTANTE: "data class" es una clase especial de Kotlin que genera
 * automáticamente métodos útiles: equals(), hashCode(), toString(),
 * copy() y los getters/setters. Perfecta para puras portadoras de datos.
 */
data class Character(
    val id: Int,        // Identificador único del personaje en la API
    val name: String,   // Nombre del personaje (ej: "Rick Sanchez")
    val status: String, // Estado actual (ej: "Alive")
    val image: String   // URL de la imagen del personaje
)