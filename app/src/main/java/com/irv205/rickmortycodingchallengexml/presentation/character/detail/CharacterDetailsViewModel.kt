package com.irv205.rickmortycodingchallengexml.presentation.character.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irv205.rickmortycodingchallengexml.core.util.ResponseHandler
import com.irv205.rickmortycodingchallengexml.domain.model.Character
import com.irv205.rickmortycodingchallengexml.domain.usecase.GetCharacterByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ============================================================
 * CAPA DE PRESENTACIÓN: VIEWMODEL DEL DETALLE (MVVM)
 * ============================================================
 * Mismo patrón que CharacterListViewModel pero para la pantalla de detalle.

 * NO habla con el repositorio directo: usa el caso de uso del dominio
 * GetCharacterByIdUseCase (la acción de negocio "obtener personaje por id").
 *
 * Expone a la View (CharacterDetailsFragment):
 *   - character  : LiveData<Character?> -> el personaje cuando llega.
 *   - isLoading  : LiveData<Boolean>    -> para mostrar/ocultar el progreso.
 *   - error      : LiveData<String?>    -> mensaje si algo falla.
 *
 * @HiltViewModel: Hilt instancia el ViewModel e inyecta el caso de uso.
 */
@HiltViewModel
class CharacterDetailsViewModel @Inject constructor(
    private val getCharacterByIdUseCase: GetCharacterByIdUseCase
) : ViewModel() {

    private val _character = MutableLiveData<Character>()
    val character: LiveData<Character> get() = _character

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    /**
     * Pide el detalle del personaje al UseCase (misma técnica que la lista).
     * @param id Identificador del personaje recibido por navegación.
     */
    fun getCharacterById(id: Int) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = getCharacterByIdUseCase(id)) {
                is ResponseHandler.Error<*> -> {
                    _isLoading.postValue(false)
                    _error.postValue(response.message)
                }

                is ResponseHandler.Success -> {
                    _character.postValue(response.data)
                    _isLoading.postValue(false)
                }
            }
        }
    }
}