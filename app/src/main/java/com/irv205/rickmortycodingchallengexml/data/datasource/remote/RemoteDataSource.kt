package com.irv205.rickmortycodingchallengexml.data.datasource.remote

import com.irv205.rickmortycodingchallengexml.data.maper.toDomain
import com.irv205.rickmortycodingchallengexml.data.service.ApiService
import com.irv205.rickmortycodingchallengexml.domain.model.CharactersPage
import javax.inject.Inject

/**
 * ============================================================
 * CAPA DE DATOS: FUENTE DE DATOS REMOTA (RED)
 * ============================================================
 * Un "DataSource" es el responsable de UN solo transporte de datos.
 * Esta clase es la única que habla con Retrofit ([ApiService]) y expone
 * funciones SENTIDAS para la app (una página de personajes), devolviendo ya
 * el modelo de dominio (CharactersPage) gracias al mapper.
 *
 * El repositorio NO sabe que aquí debajo hay Retrofit: solo sabe que puede
 * pedir "la primera página" o "una página por URL".
 *
 * @Inject en el constructor: Hilt le inyecta la ApiService automáticamente
 * (la fabrica NetworkModule). La clase no tiene estado, por eso no usa @Singleton.
 */
class RemoteDataSource @Inject constructor(private val service: ApiService) {

    /**
     * Pide la PRIMERA página (URL base /character) y la convierte a dominio.
     */
    suspend fun getCharacters(): CharactersPage {
        return service.getCharacters().toDomain()
    }

    /**
     * Pide la página cuya URL nos dio el servidor en "info.next".
     * @param url URL absoluta (ej: ".../api/character?page=2").
     */
    suspend fun getCharactersByUrl(url: String): CharactersPage {
        return service.getCharactersByUrl(url).toDomain()
    }
}