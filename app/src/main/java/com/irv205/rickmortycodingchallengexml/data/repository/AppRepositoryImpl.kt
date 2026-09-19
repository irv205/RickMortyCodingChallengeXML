package com.irv205.rickmortycodingchallengexml.data.repository

import android.util.Log
import com.irv205.rickmortycodingchallengexml.core.util.ResponseHandler
import com.irv205.rickmortycodingchallengexml.data.datasource.local.LocalDataSource
import com.irv205.rickmortycodingchallengexml.data.datasource.remote.RemoteDataSource
import com.irv205.rickmortycodingchallengexml.domain.model.Character
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
 *   - RemoteDataSource : fuente remota (red).
 *   - LocalDataSource  : fuente local (Room / offline).
 *
 * Estrategia OFFLINE-FIRST CON REANUDACIÓN de paginación:
 *   El ViewModel guarda solo en memoria la URL de la siguiente página, así que
 *   al cerrar la app se pierde. Por eso LOCAL también guarda esa URL
 *   (tabla page_info): es la "memoria" de cuántas páginas hemos descargado.
 *      - nextPageUrl guardado = null  -> caché COMPLETA (se llegó a la última).
 *      - nextPageUrl guardado != null -> caché PARCIAL (quedó a medias).
 *
 *   - Caché vacía        -> pedimos la página 1 a la red y guardamos.
 *   - Caché COMPLETA     -> servimos todo local (100% offline, sin red).
 *   - Caché PARCIAL      -> REANUDAMOS desde la URL guardada y acumulamos.
 *   - La red falla       -> servimos lo que haya en caché (fallback offline).
 */
class AppRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : AppRepository {

    /**
     * Método común para GUARDAR una página completa:
     *   1) Los personajes (tabla characters, con REPLACE para no duplicar).
     *   2) La URL de la siguiente página (tabla page_info): con esto la caché
     *      "recuerda" dónde se quedó aunque la app se cierre.
     * Se usa tras CADA página recibida de la red, inicial o siguiente.
     */
    private suspend fun savePage(page: CharactersPage) {
        localDataSource.saveCharacters(page.characters)
        localDataSource.saveNextPageUrl(page.nextPageUrl)
    }

    /**
     * Implementación concreta del método definido en la interfaz AppRepository.
     *
     * "override suspend": confirma que es la versión implementada de un método
     * "suspend" de la interfaz, por lo que puede llamar a las fuentes de forma
     * asíncrona (con corrutinas, fuera del hilo principal).
     *
     * Flujo (solo orquestación, sin detalles de red ni de BD):
     *   1) nextPageUrl == null (llamada INICIAL):
     *        - Consultamos la caché LOCAL para decidir:
     *            a) Caché VACÍA            -> página 1 de la red y guardamos.
     *            b) Caché COMPLETA
     *               (next guardado null)   -> servimos todo local, sin red,
     *                                        nextPageUrl = null (no hay más).
     *            c) Caché PARCIAL
     *               (next guardado x)      -> reanudamos con la URL x, juntamos
     *                                        lo local + lo nuevo y guardamos.
     *            d) Caché SIN metadatos
     *               (no existe page_info)  -> caché de una versión anterior:
     *                                        estado desconocido => pedimos la
     *                                        página 1 a la red (se regenera todo).
     *   2) nextPageUrl != null (SCROLL a una página siguiente):
     *        - Pedimos esa página a la red y la guardamos (personajes + URL).
     *   3) Si la red falla y es la llamada inicial, servimos la caché como
     *      FALLBACK OFFLINE (con su nextPageUrl guardado para poder seguir
     *      cuando vuelva la conexión).
     *   4) Todo se envuelve en ResponseHandler.Success o .Error.
     */
    override suspend fun getCharacters(nextPageUrl: String?): ResponseHandler<CharactersPage> {
        return try {
            val charactersPage = if (nextPageUrl == null) {
                // ----- LLAMADA INICIAL: decidimos según el estado de la caché. -----
                val cached = localDataSource.getCharacters()
                if (cached.isEmpty()) {
                    // Caché vacía: no hay nada local -> pedimos la página 1.
                    val page = remoteDataSource.getCharacters()
                    savePage(page)
                    page
                } else {
                    // Hay caché: ¿se descargó TODO, quedó a medias, o NO existen
                    // metadatos? La fila page_info lo dice:
                    //   - null                  -> sin metadatos (estado desconocido)
                    //   - nextPageUrl == null   -> caché COMPLETA
                    //   - nextPageUrl != null   -> caché PARCIAL (dónde reanudar)
                    val storedInfo = localDataSource.getStoredPageInfo()
                    when {
                        // CASO d) SIN METADATOS: la caché viene de una versión anterior (antes de
                        // existir page_info). No sabemos cuánto hay descargado
                        // ni dónde se quedó. PELIGRO: si pedimos la página 1 y
                        // guardamos su nextPageUrl ("...?page=2") sin tocar los
                        // personajes viejos, dejaríamos ESTADOS DIVERGENTES:
                        // page_info diría "reanuda en la página 2" mientras la
                        // tabla characters aún conserva los antiguos. Al reanudar
                        // se mezclarían personajes YA guardados -> duplicados.
                        // Por eso BORRAMOS la caché y reconstruimos desde la
                        // página 1: personajes y page_info vuelven a ser
                        // coherentes. Solo ocurre una vez (migración).
                        storedInfo == null -> {
                            localDataSource.clearCache()
                            val page = remoteDataSource.getCharacters()
                            savePage(page)
                            page
                        }

                        // CASO b) COMPLETA: la última página guardada fue la
                        // última del servidor (info.next era null). Servimos
                        // 100% local, sin tocar la red. nextPageUrl = null
                        // porque de verdad no quedan más páginas.
                        storedInfo.nextPageUrl == null -> {
                            CharactersPage(characters = cached, nextPageUrl = null)
                        }

                        // CASO c) PARCIAL: reanudamos desde la URL guardada.
                        // Juntamos lo ya descargado (cached) con lo nuevo para
                        // que el ViewModel muestre TODA la lista acumulada.
                        // distinctBy { it.id }: red de seguridad. Si por estados
                        // viejos/divergentes la página trae personajes que ya
                        // estaban en caché, se descartan los repetidos (se
                        // conserva la primera aparición, o sea el orden real).
                        else -> {
                            val page = remoteDataSource.getCharactersByUrl(storedInfo.nextPageUrl)
                            val merged = CharactersPage(
                                characters = (cached + page.characters).distinctBy { it.id },
                                nextPageUrl = page.nextPageUrl
                            )
                            savePage(merged)
                            merged
                        }
                    }
                }
            } else {
                // ----- PÁGINA SIGUIENTE (scroll): siempre a la red y guardamos. -----
                val page = remoteDataSource.getCharactersByUrl(nextPageUrl)
                savePage(page)
                page
            }
            ResponseHandler.Success(charactersPage)
        } catch (e: Exception) {
            // FALLBACK OFFLINE: solo en la llamada inicial. Si la red falla pero
            // tenemos caché, la servimos de todos modos (modo sin conexión).
            // Le pasamos el "nextPageUrl" guardado (si existe): así la lista
            // podrá intentar seguir cuando vuelva la conexión. Si no hay
            // metadatos, nextPageUrl es null y la caché queda sin más páginas.
            if (nextPageUrl == null) {
                val cached = localDataSource.getCharacters()
                if (cached.isNotEmpty()) {
                    return ResponseHandler.Success(
                        CharactersPage(
                            characters = cached,
                            nextPageUrl = localDataSource.getStoredPageInfo()?.nextPageUrl
                        )
                    )
                }
            }
            // Sin caché o no es la inicial: error determinista, no excepción.
            Log.e("ERROR", e.message.toString())
            ResponseHandler.Error(message = e.message ?: "")
        }
    }

    /**
     * Implementación del detalle de un personaje (misma filosofía offline-first).
     *
     * Flujo:
     *   1) Buscamos en LocalDataSource (Room). Si el personaje ya está en la
     *      caché, lo devolvemos SIN tocar la red (modo offline).
     *   2) Si no está guardado, lo pedimos a RemoteDataSource y lo guardamos
     *      en local para futuras visitas (así la caché se autoalimenta).
     *   3) Cualquier problema se envuelve en ResponseHandler.Error.
     */
    override suspend fun getCharacterById(id: Int): ResponseHandler<Character> {
        return try {
            // 1) Primero lo local: si ya lo tenemos guardado, cero red.
            val character = localDataSource.getCharacterById(id)
            if (character != null) {
                ResponseHandler.Success(character)
            } else {
                // 2) No estaba en la caché: lo pedimos a la red y lo guardamos.
                val remote = remoteDataSource.getCharacterById(id)
                localDataSource.saveCharacters(listOf(remote))
                ResponseHandler.Success(remote)
            }
        } catch (e: Exception) {
            Log.e("ERROR", e.message.toString())
            ResponseHandler.Error(message = e.message ?: "")
        }
    }
}