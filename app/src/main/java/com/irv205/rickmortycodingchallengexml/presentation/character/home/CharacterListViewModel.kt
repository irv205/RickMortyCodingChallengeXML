package com.irv205.rickmortycodingchallengexml.presentation.character.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irv205.rickmortycodingchallengexml.core.util.ResponseHandler
import com.irv205.rickmortycodingchallengexml.domain.model.Character
import com.irv205.rickmortycodingchallengexml.domain.usecase.GetCharactersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ============================================================
 * CAPA DE PRESENTACIÓN: VIEWMODEL (ARQUITECTURA MVVM)
 * ============================================================
 * MVVM (Model-View-ViewModel): el ViewModel está en el medio.
 *   - View      (CharacterListFragment): dibuja la pantalla, no tiene lógica.
 *   - ViewModel (esta clase): obtiene y prepara los datos para la View,
 *                             sobrevive a rotaciones, NO conoce Views.
 *   - Model     (data <-> domain): los datos reales vía casos de uso.
 *
 * El ViewModel NO llama al repositorio directo: usa el caso de uso de dominio
 * GetCharactersUseCase (una acción de negocio con nombre explícito).
 *
 * @HiltViewModel: marca que Hilt debe encargarse de instanciar este ViewModel.
 * Para que funcione, el constructor necesita @Inject (lo tiene abajo) y el
 * fragment/Activity que lo use debe estar anotado con @AndroidEntryPoint.
 *
 * LiveData: contenedor "observable". La Vista se SUSCRIBE a él y se entera
 * cuando cambia el valor, sin que el ViewModel tenga que saber de la Vista.
 *   - MutableLiveData: se puede MODIFICAR el valor (solo el ViewModel lo hace).
 *   - LiveData (público, sin Mutable): la Vista solo puede OBSERVARLO.
 *   Por eso usamos dos: uno privado mutable (_characterList) y uno público
 *   inmutable (characterList) que expone la versión solo-lectura.
 *
 * PAGINACIÓN DIRIGIDA POR EL SERVIDOR: la API devuelve en cada respuesta la
 * URL exacta de la siguiente página ("info.next"). Este ViewModel guarda:
 *   - nextPageUrl : la próxima URL a pedir (null = no hay más páginas).
 *   - isLoading   : true mientras hay una petición en vuelo. Sirve de GUARD:
 *                    evita lanzar dos peticiones a la vez cuando el usuario
 *                    hace scroll rápido (sin este guard duplicaríamos datos).
 * NO lleva contador de páginas: la API es la única fuente de verdad de la
 * paginación.
 */
@HiltViewModel
class CharacterListViewModel @Inject constructor(
    private val getCharactersUseCase: GetCharactersUseCase
) : ViewModel() {

    // Data class Character de la capa de dominio, lista acumulada de personajes.
    private val _characterList = MutableLiveData<List<Character>>()
    val characterList: LiveData<List<Character>> get() = _characterList

    // Flag de carga: true mientras se está pidiendo una página (inicial o siguiente).
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Mensaje de error para informar al usuario cuando la petición falle.
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Estado interno de la paginación (no se expone a la Vista).
    // URL de la siguiente página a pedir. null = no hay más páginas (la API lo manda).
    private var nextPageUrl: String? = null

    /**
     * init = bloque inicializador. Se ejecuta automáticamente al CREAR el ViewModel,
     * antes de que la Activity muestre nada. Perfecto para lanzar la petición inicial.
     */
    init {
        getCharacters()
    }

    /**
     * Carga la PRIMERA página (o recarga desde cero).
     *
     * - El guard "if (_isLoading.value == true) return" que aplica loadPage() evita
     *   que se dispare dos veces si esta función se vuelve a llamar con una
     *   petición en vuelo (la llamada inicial sale del init del ViewModel).
     * - Con nextPageUrl = null le decimos al repositorio "llamada inicial":
     *   pedirá la URL base /character.
     * - Termina reutilizando loadPage(...) con append = false (reemplaza la lista).
     */
    fun getCharacters() {
        loadPage(url = null, append = false)
    }

    /**
     * Reacción al scroll de la Vista. La Vista SOLO reenvía los dos números
     * (último item visible y total); toda la LÓGICA vive aquí:
     *   - ¿estamos en el final de la lista? (último item visible == total - 1)
     *   - ¿quedan más páginas?  (nextPageUrl != null; la manda el servidor)
     * El guard de "no repetir peticiones en vuelo" (isLoading) lo aplica loadPage().
     * Solo si se cumplen todas, pedimos la siguiente página (nextPageUrl).
     */
    fun onListScrolled(lastVisibleItemPosition: Int, totalItemCount: Int) {
        val isAtEndOfList = totalItemCount > 0 && lastVisibleItemPosition >= totalItemCount - 1
        val nextUrl = nextPageUrl
        if (isAtEndOfList && nextUrl != null) {
            loadPage(url = nextUrl, append = true)
        }
    }

    /**
     * Método común que usan getCharacters() y onListScrolled(): pedir UNA página
     * a la API y actualizar el estado. La única diferencia entre ambos es:
     *   - append = false -> REEMPLAZA la lista (carga inicial / reset).
     *   - append = true  -> ACUMULA la lista actual + los personajes nuevos.
     *
     * viewModelScope.launch(Dispatchers.IO) lanza una corrutina asociada al ciclo
     * de vida del ViewModel y NO bloquea el hilo principal (main).
     * En Success actualizamos nextPageUrl con la URL de la siguiente página que
     * devuelve el servidor; en Error bajamos isLoading y exponemos el mensaje.
     */
    private fun loadPage(url: String?, append: Boolean) {
        if (_isLoading.value == true) return

        // Leemos la lista FUERA de la corrutina (hilo principal) para no acceder
        // a LiveData desde el hilo de trabajo.
        val currentList = if (append) _characterList.value.orEmpty() else emptyList()
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            when (val response = getCharactersUseCase(url)) {
                is ResponseHandler.Error<*> -> {
                    _isLoading.postValue(false)
                    _error.postValue(response.message)
                }

                is ResponseHandler.Success -> {
                    response.data.let { result ->
                        nextPageUrl = result.nextPageUrl
                        // distinctBy { it.id }: red de seguridad ante duplicados.
                        // Combinamos la lista actual + lo nuevo y descartamos
                        // cualquier personaje que ya estuviera (conservamos la
                        // primera aparición = el orden real). Así, aunque una
                        // ruta de datos devuelva solapamientos (estados viejos
                        // de caché), la UI nunca muestra repetidos.
                        val updatedList = (currentList + result.characters).distinctBy { it.id }
                        _characterList.postValue(updatedList)
                        // Solo depuración: ver los datos en Logcat de Android Studio.
                        Log.e("PERSONAJES======", result.characters.toString())
                        _isLoading.postValue(false)
                    }
                }
            }
        }
    }
}