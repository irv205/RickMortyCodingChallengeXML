package com.irv205.rickmortycodingchallengexml.domain.usecase

import com.irv205.rickmortycodingchallengexml.core.util.ResponseHandler
import com.irv205.rickmortycodingchallengexml.domain.model.Character
import com.irv205.rickmortycodingchallengexml.domain.repository.AppRepository
import javax.inject.Inject

/**
 * ============================================================
 * CAPA DE DOMINIO: CASO DE USO "OBTENER UN PERSONAJE POR ID"
 * ============================================================
 * Hermano de GetCharactersUseCase: representa UNA acción de negocio única:
 * "traer el detalle de un personaje concreto". El CharacterDetailsViewModel
 * dependerá de este caso de uso, no del repositorio directo.
 *
 * @param id Identificador del personaje que llega por navegación.
 */
class GetCharacterByIdUseCase @Inject constructor(
    private val repository: AppRepository
) {

    /**
     * Ejecuta el caso de uso llamando al repositorio (que decide si leer de
     * la caché local o de la red, según la estrategia offline-first).
     */
    suspend operator fun invoke(id: Int): ResponseHandler<Character> {
        return repository.getCharacterById(id = id)
    }
}