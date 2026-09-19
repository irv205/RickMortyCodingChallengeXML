package com.irv205.rickmortycodingchallengexml.data.service

import com.irv205.rickmortycodingchallengexml.data.model.CharacterDTO
import com.irv205.rickmortycodingchallengexml.data.model.CharactersResponseDTO
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

/**
 * ============================================================
 * CAPA DE DATOS: DEFINICIÓN DE LA API CON RETROFIT
 * ============================================================
 * Retrofit es una librería que convierte los endpoint HTTP en simples
 * funciones de Kotlin. Basta con declarar una interfaz con anotaciones
 * y Retrofit genera internamente (en tiempo de ejecución) la implementación
 * que hace las peticiones HTTP reales.
 *
 * La URL base ya viene configurada en NetworkModule:
 *   baseUrl = "https://rickandmortyapi.com/api/"
 */
interface ApiService {

    /**
     * @GET("character"): indica que esta función hará una petición HTTP GET.
     * La URL completa se forma concatenando la baseUrl con el path:
     *   https://rickandmortyapi.com/api/character
     *
     * "suspend": marca la función como de suspensión (corrutina de Kotlin).
     * Significa que la llamada se ejecuta de forma ASÍNCRONA, sin bloquear
     * el hilo de la UI. Cuando el servidor responda, la corrutina se reanuda
     * automáticamente en el hilo de donde fue lanzada.
     *
     * ¡IMPORTANTE para un estudiante! El tipo de retorno NO puede ser una lista
     * directa (por ejemplo List<CharacterDTO>) porque Retrofit no sabe cómo
     * crear un "CallAdapter" para un tipo desnudo. Se debe devolver:
     *   - Call<T>          (estilo clásico, con los métodos enqueue/execute)
     *   - Response<T>      (si quieres ver cortar el status HTTP)
     *   - suspend fun ...: T  (estilo moderno con corrutinas, lo usamos aquí)
     *
     * El tipo T devuelto (CharactersResponseDTO) debe coincidir con la estructura
     * del JSON que responde el servidor, y se deserializa con el converter
     * Moshi que registramos en NetworkModule.
     *
     * Dos métodos para la PAGINACIÓN dirigida por el servidor:
     *   1) getCharacters(): la llamada INICIAL (URL base /character).
     *   2) getCharactersByUrl(...): pide la URL exacta de la siguiente página
     *      (el campo "info.next" que devuelve la API). Retrofit permite dar la
     *      URL COMPLETA con @Url en vez de solo el path.
     */
    @GET("character")
    suspend fun getCharacters(): CharactersResponseDTO

    /**
     * @Url url: recibe una URL absoluta (p. ej. "https://rickandmortyapi.com/api/character?page=2").
     * Es exactamente lo que el servidor manda en "info.next", así el cliente no
     * construye ni recuerda la URL de la siguiente página.
     */
    @GET
    suspend fun getCharactersByUrl(@Url url: String): CharactersResponseDTO

    /**
     * Detalle de UN personaje: /character/{id} -> p. ej. /character/1.
     * @Path("id") sustituye "{id}" dentro de la URL por el valor que pasemos.
     * Devuelve el DTO de un solo personaje (sin "info" ni lista).
     */
    @GET("character/{id}")
    suspend fun getCharacterById(@Path("id") id: Int): CharacterDTO
}