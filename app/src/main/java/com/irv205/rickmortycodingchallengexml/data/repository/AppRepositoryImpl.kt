package com.irv205.rickmortycodingchallengexml.data.repository

import android.util.Log
import com.irv205.rickmortycodingchallengexml.core.util.ResponseHandler
import com.irv205.rickmortycodingchallengexml.data.datasource.local.LocalDataSource
import com.irv205.rickmortycodingchallengexml.data.datasource.remote.RemoteDataSource
import com.irv205.rickmortycodingchallengexml.domain.model.CharactersPage
import com.irv205.rickmortycodingchallengexml.domain.repository.AppRepository
import javax.inject.Inject

/**
 * ============================================================
 * CAPA DE DATOS: IMPLEMENTACIÓN DEL REPOSITORIO (ORQUESTADOR)
 * ============================================================
 * El patrón Repository es la "frontera" entre los datos y el resto de la app.
 * La app (ViewModel) NO sabe de dónde vienen los datos: solo conoce la interfaz
 * [AppRepository].
 *
 * El repositorio NO habla con Retrofit ni con Room directamente: eso es cosa de
 * los DATA SOURCES. Aquí solo se ORQUESTA la estrategia (la "política"):
 *   - RemoteDataSource : fuente remota (red). Volver a "RemoteDataSource".
 *   - LocalDataSource  : fuente local (Room / offline).
 *
 * Estrategia OFFLINE-FIRST (primero lo local) implementada aquí:
 *   - Primera página: revisamos la BD local. Si hay datos, los devolvemos
 *     (funcionamos sin Internet). Si está vacía, pedimos a la API y guardamos.
 *   - Páginas siguientes: siempre a la red y se acumulan en la BD local.
 */
class AppRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : AppRepository {

    /**
     * Implementación concreta del método definido en la interfaz AppRepository.
     *
     * "override suspend": confirma que es la versión implementada de un método
     * "suspend" de la interfaz, por lo que puede llamar a las fuentes de forma
     * asíncrona (con corrutinas, fuera del hilo principal).
     *
     * Flujo de la función (solo orquestación, sin detalles de red ni de BD):
     *   1) nextPageUrl == null (llamada INICIAL):
     *        - Consultamos LocalDataSource: si hay personajes, los devolvemos
     *          con nextPageUrl = null (esos datos locales no tienen más páginas).
     *        - Si está vacía, pedimos la página 1 a RemoteDataSource y la
     *          guardamos en LocalDataSource.
     *   2) nextPageUrl != null (SCROLL a la página siguiente):
     *        - Pedimos esa página a RemoteDataSource y la acumulamos en local.
     *   3) Todo el resultado se envuelve en ResponseHandler.Success o .Error.
     */
    override suspend fun getCharacters(nextPageUrl: String?): ResponseHandler<CharactersPage> {
        return try {
            val charactersPage = if (nextPageUrl == null) {
                // MODO OFFLINE: primero revisamos si ya tenemos datos guardados.
                val cached = localDataSource.getCharacters()
                if (cached.isNotEmpty()) {
                    // Hay datos locales: los usamos (no tocamos la red).
                    CharactersPage(
                        characters = cached,
                        nextPageUrl = null
                    )
                } else {
                    // BD vacía: pedimos a la red y guardamos la página recibida.
                    val page = remoteDataSource.getCharacters()
                    localDataSource.saveCharacters(page.characters)
                    page
                }
            } else {
                // Página siguiente: siempre a la red; acumulamos en la BD local.
                val page = remoteDataSource.getCharactersByUrl(nextPageUrl)
                localDataSource.saveCharacters(page.characters)
                page
            }
            ResponseHandler.Success(charactersPage)
        } catch (e: Exception) {
            // Error: lo registramos y lo propagamos envuelto.
            // Devolvemos un Error determinista, no una excepción.
            Log.e("ERROR", e.message.toString())
            ResponseHandler.Error(message = e.message ?: "")
        }
    }
}