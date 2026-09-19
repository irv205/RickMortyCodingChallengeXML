package com.irv205.rickmortycodingchallengexml.domain.usecase

import com.irv205.rickmortycodingchallengexml.core.util.ResponseHandler
import com.irv205.rickmortycodingchallengexml.domain.model.CharactersPage
import com.irv205.rickmortycodingchallengexml.domain.repository.AppRepository
import javax.inject.Inject

/**
 * ============================================================
 * CAPA DE DOMINIO: CASO DE USO "OBTENER PERSONAJES"
 * ============================================================
 * Un UseCase (o caso de uso) representa UNA acción de negocio de la app:
 * aquí, "obtener personajes (una página)". El ViewModel ya no habla con el
 * repositorio directamente; habla con este UseCase. Es UNA forma de mantener
 * el ViewModel fino y de darle nombre al QUÉ de la operación.
 *
 * ¿Por qué es mejor que el ViewModel llame al repo directo?
 *   - El ViewModel solo "orquesta la UI"; la lógica de QUÉ hay que hacer
 *     (obtener una página) vive en el dominio con un nombre explícito.
 *   - Mañana, si esta acción necesita pasos extra (validar, medir tiempo,
 *     registrar analytics...), se agregan AQUÍ sin tocar el ViewModel.
 *
 * Patrón "operator fun invoke": hace que el UseCase se pueda llamar como una
 * función (getCharactersUseCase(url)). Es el estilo Kotlin estándar.
 *
 * Depende de la abstracción AppRepository (dominio), no de implementaciones
 * de data. @Inject en el constructor: Hilt le inyecta el repositorio.
 */
class GetCharactersUseCase @Inject constructor(
    private val repository: AppRepository
) {

    /**
     * Ejecuta el caso de uso.
     * @param nextPageUrl URL de la página a pedir. null = primera página
     *                    (el repositorio decide entre caché local y red).
     * @return ResponseHandler<CharactersPage>: éxito con la página y los
     *         personajes, o un error determinista.
     */
    suspend operator fun invoke(nextPageUrl: String?): ResponseHandler<CharactersPage> {
        return repository.getCharacters(nextPageUrl = nextPageUrl)
    }
}