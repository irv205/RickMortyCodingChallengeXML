package com.irv205.rickmortycodingchallengexml.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irv205.rickmortycodingchallengexml.core.util.ResponseHandler
import com.irv205.rickmortycodingchallengexml.domain.model.Character
import com.irv205.rickmortycodingchallengexml.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ============================================================
 * CAPA DE PRESENTACIÓN: VIEWMODEL (ARQUITECTURA MVVM)
 * ============================================================
 * MVVM (Model-View-ViewModel): el ViewModel está en el medio.
 *   - View     (MainActivity): dibuja la pantalla, no tiene lógica.
 *   - ViewModel (esta clase):  obtiene y prepara los datos para la View,
 *                              sobrevive a rotaciones, NO conoce Views.
 *   - Model    (data <-> domain): los datos reales vía repositorio.
 *
 * @HiltViewModel: marca que Hilt debe encargarse de instanciar este ViewModel.
 * Para que funcione, el constructor necesita @Inject (lo tiene abajo) y la
 * Activity que lo use debe estar anotada con @AndroidEntryPoint.
 *
 * LiveData: contenedor "observable". La Vista se SUSCRIBE a él y se entera
 * cuando cambia el valor, sin que el ViewModel tenga que saber de la Vista.
 *   - MutableLiveData: se puede MODIFICAR el valor (solo el ViewModel lo hace).
 *   - LiveData (público, sin Mutable): la Vista solo puede OBSERVARLO.
 *   Por eso usamos dos: uno privado mutable (_characterList) y uno público
 *   inmutable (characterList) que expone la versión solo-lectura.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    // Data class Character de la capa de dominio, lista completa de personajes.
    private val _characterList = MutableLiveData<List<Character>>()
    val characterList: LiveData<List<Character>> get() = _characterList

    // Mensaje de error para informar al usuario cuando la petición falle.
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    /**
     * init = bloque inicializador. Se ejecuta automáticamente al CREAR el ViewModel,
     * antes de que la Activity muestre nada. Perfecto para lanzar la petición inicial.
     */
    init {
        getCharacters()
    }

    /**
     * Lanza la petición de personajes en segundo plano.
     *
     * viewModelScope.launch(Dispatchers.IO): lanza una corrutina asociada al ciclo
     * de vida del ViewModel (si el ViewModel se destruye, la corrutina se cancela sola).
     *   - Dispatchers.IO: hilo de trabajo para operaciones de red/lectura.
     *     Así NO bloqueamos el hilo principal (main) que dibuja la interfaz.
     *
     * repository.getCharacters() es suspend y devuelve ResponseHandler:
     *   - Success: guardamos los datos en _characterList (la Vista se actualizará sola).
     *   - Error:   guardamos el mensaje de error en _error (la Vista lo mostrará).
     *
     * "when" con la sealed class obliga a tratar ambos casos (el compilador lo exige).
     */
    fun getCharacters() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = repository.getCharacters()) {
                is ResponseHandler.Error<*> -> {
                    _error.postValue(response.message)
                }

                is ResponseHandler.Success -> {
                    response.data.let { character ->
                        _characterList.postValue(character)
                        // Solo depuración: ver los datos en Logcat de Android Studio.
                        Log.e("PERSONAJES======", character.toString())
                    }
                }
            }
        }
    }
}