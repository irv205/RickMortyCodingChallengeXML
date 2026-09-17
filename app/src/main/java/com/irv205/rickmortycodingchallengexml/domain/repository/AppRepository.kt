package com.irv205.rickmortycodingchallengexml.domain.repository

import com.irv205.rickmortycodingchallengexml.core.util.ResponseHandler
import com.irv205.rickmortycodingchallengexml.domain.model.Character
import com.irv205.rickmortycodingchallengexml.domain.model.CharactersPage

/**
 * ============================================================
 * CAPA DE DOMINIO: CONTRATO DEL REPOSITORIO
 * ============================================================
 * Una interfaz en Kotlin define UN CONTRATO: "qué métodos expone un objeto",
 * sin decir TODO el cómo se implementan internamente.
 *
 * Aquí declaramos la puerta de entrada a los datos desde el dominio.
 * La capa de presentación (ViewModel) depende de ESTA interfaz, no de la
 * implementación concreta (AppRepositoryImpl). Esto se conoce como
 * "depender de abstracciones, no de implementaciones" y es la base
 * del patrón Repository + Inyección de Dependencias.
 *
 * Quién la implementa: AppRepositoryImpl (en data/repository).
 * Quién la inyecta:  Hilt, mediante RepositoryModule con @Binds.
 *
 * "suspend fun": método asíncrono que se ejecuta en una corrutina,
 * devolviendo ResponseHandler<CharactersPage> (éxito o error). Devolvemos
 * CharactersPage (y no solo List<Character>) para conservar la info de
 * paginación que trae la API.
 *
 * El parámetro "nextPageUrl" es la URL de la siguiente página que mandó el
 * servidor en "info.next". null indica la llamada inicial (primera página).
 * La capa de presentación no calcula números de página: solo reenvía esta URL.
 */
interface AppRepository {
    suspend fun getCharacters(nextPageUrl: String?): ResponseHandler<CharactersPage>
}