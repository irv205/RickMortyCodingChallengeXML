package com.irv205.rickmortycodingchallengexml.data.repository

import android.util.Log
import com.irv205.rickmortycodingchallengexml.core.util.ResponseHandler
import com.irv205.rickmortycodingchallengexml.data.maper.toDomain
import com.irv205.rickmortycodingchallengexml.data.service.ApiService
import com.irv205.rickmortycodingchallengexml.domain.model.CharactersPage
import com.irv205.rickmortycodingchallengexml.domain.repository.AppRepository
import javax.inject.Inject

/**
 * ============================================================
 * CAPA DE DATOS: IMPLEMENTACIÓN DEL REPOSITORIO
 * ============================================================
 * El patrón Repository es la "frontera" entre los datos y el resto de la app.
 * La app (ViewModel) NO sabe de dónde vienen los datos (red, base local, cache...):
 * solo conoce la interfaz [AppRepository]. Aquí decidimos que los datos
 * vienen de la API mediante Retrofit ([ApiService]).
 *
 * Ventaja: si mañana cambiamos la API por una base de datos local, solo
 * cambiaremos ESTA clase y el resto de la app no se entera.
 */
class AppRepositoryImpl @Inject constructor(private val service: ApiService) : AppRepository {

    /**
     * Implementación concreta del método definido en la interfaz AppRepository.
     *
     * "override suspend": confirma que es la versión implementada de un método
     * "suspend" de la interfaz, por lo que puede llamar a la API de forma asíncrona.
     *
     * Flujo de la función:
     *   1) try/catch: envolvemos la llamada de red para capturar cualquier error
     *      (sin conexión, servidor caído, JSON inesperado, etc.).
     *   2) Cómo usamos corrutinas ("suspend"), la llamada se hace en un hilo
     *      de trabajo y NO bloquea la interfaz de usuario.
     *   3) Si llega una nextPageUrl, pedimos ESA página con getCharactersByUrl(@Url);
     *      si es null, es la llamada inicial y pedimos la URL base /character.
     *   4) Mapeamos la respuesta completa con CharactersResponseDTO.toDomain():
     *      convierte "results" a listas de Character y conserva la URL de la
     *      siguiente página (info.next) que trae InfoDTO.
     *   5) Devolvemos el resultado envuelto en ResponseHandler.Success o .Error
     *      para que la capa de presentación decida cómo reaccionar sin romperse.
     */
    override suspend fun getCharacters(nextPageUrl: String?): ResponseHandler<CharactersPage> {
        return try {
            // Éxito: pedimos la URL exacta del servidor (o la inicial si es null)
            // y convertimos la respuesta al modelo de dominio + paginación.
            val response = if (nextPageUrl == null) {
                service.getCharacters()
            } else {
                service.getCharactersByUrl(nextPageUrl)
            }
            ResponseHandler.Success(response.toDomain())
        } catch (e: Exception) {
            // Error: registramos el mensaje en Logcat (se ve en Android Studio) y
            // lo propagamos envuelto. Devolvemos un Error determinista, no una excepción.
            Log.e("ERROR", e.message.toString())
            ResponseHandler.Error(message = e.message ?: "")
        }
    }
}