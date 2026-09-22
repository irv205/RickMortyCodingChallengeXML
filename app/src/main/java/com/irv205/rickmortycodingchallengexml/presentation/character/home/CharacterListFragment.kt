package com.irv205.rickmortycodingchallengexml.presentation.character.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.irv205.rickmortycodingchallengexml.databinding.FragmentCharacterListBinding
import com.irv205.rickmortycodingchallengexml.presentation.navigation.Navigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * ============================================================
 * CAPA DE PRESENTACIÓN: FRAGMENT (ARQUITECTURA MVVM)
 * ============================================================
 * Un Fragment es una "pieza de interfaz reutilizable" que vive DENTRO de una
 * Activity. Antes todo esto estaba en MainActivity; ahora la Activity es solo
 * un contenedor y este fragment posee la lista de personajes.
 *
 * Ciclo de vida del fragment (los métodos clave de esta clase):
 *   - onCreateView(): el sistema nos PIDE que creemos y devolvamos la vista.
 *   - onViewCreated(): la vista YA existe; aquí configuramos listeners, adapter,
 *     observadores, etc. (lo que antes hacía la Activity en onCreate).
 *   - onDestroyView(): la vista se destruye (rotación, navegación); aquí debemos
 *     soltar la referencia al binding para evitar fugas de memoria (memory leaks)
 *     de vistas (la Activity, en cambio, se recicla completa).
 *
 * ¿Por qué @AndroidEntryPoint también aquí? Para que Hilt pueda inyectar
 * dependencias en el fragment (instanciar el CharacterListViewModel anotado
 * con @HiltViewModel).
 */
@AndroidEntryPoint
class CharacterListFragment : Fragment() {

    /**
     * - binding      : acceso a las vistas de fragment_character_list.xml GENERADO
     *                  por ViewBinding (FragmentCharacterListBinding).
     *                  Es "nullable" porque la vista del fragment se destruye/crea
     *                  varias veces durante el ciclo de vida; la guardamos en una var
     *                  que puede ser null.
     * - _binding/binding: patrón común. _binding es la var privada internamente
     *                  "mutable" (null-safe) y "binding" es un getter que devuelve
     *                  el valor de forma segura (con requireNotNull: no podemos
     *                  tener binding null si borramos la vista correctamente).
     */
    private var _binding: FragmentCharacterListBinding? = null

    // Getter que devuelve el binding NO-nullable. requireNotNull() lanza una
    // excepción en desarrollo si accedemos al binding después de onDestroyView()
    // (lo que indicaría que tocamos vistas ya destruidas). En runtime normal
    // nunca es null porque lo asignamos en onCreateView antes de usarlo.
    private val binding get() = requireNotNull(_binding)

    /**
     * NAVEGADOR (inyectado por Hilt): encapsula TODA la lógica de navegación
     * (id de la action, claves del Bundle). El fragment solo LLAMA a navegar;
     * no sabe ni conoce el grafo ni sus argumentos.
     */
    @Inject
    lateinit var navigator: Navigator

    /**
     * Mismo adaptador que antes, perezoso (se crea solo la 1ª vez que se accede).
     * Ahora el adaptador recibe un CALLBACK de click: cuando el usuario pulsa una
     * fila, el propio adaptador nos avisa con el Character pulsado y aquí solo
     * REENVIAMOS al navigator (quien decide la transición y el argumento).
     */
    private val adapter by lazy {
        CharacterListAdapter { character ->
            navigator.navigateToCharacterDetails(character.id)
        }
    }

    /**
     * viewModels() (de fragments): obtiene el CharacterListViewModel asociado a
     * ESTE fragment. Igual que en la Activity, sobrevive a rotaciones; la
     * diferencia es que su ámbito (scope) es el del fragment (se limpia cuando
     * el fragment se destruye), no el de toda la Activity.
     */
    private val viewModel: CharacterListViewModel by viewModels()

    /**
     * El sistema llama onViewCreated SOLO para construir la vista del fragment;
     * antes existía onCreateView inflando XML manualmente. Con ese método:
     *   - inflamos (convertimos a vistas vivas) fragment_character_list.xml
     *   - lo devolvemos para que la Activity lo muestre dentro del contenedor.
     *
     * @param inflater  Convertidor XML -> árbol de vistas.
     * @param container Contenedor padre (el FragmentContainerView de la Activity).
     * @param savedInstanceState Estado salvado de una destrucción previa (si existe).
     * @return La vista raíz del fragment, o null si no tiene UI.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCharacterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Equivalente de MainActivity.onCreate para un fragment: la vista YA existe,
     * así que aquí lanzamos la petición, configuramos el RecyclerView y nos
     * suscribimos a los LiveData. Es el momento correcto para tocar las vistas.
     *
     * @param view La vista creada en onCreateView.
     * @param savedInstanceState Estado salvado de una destrucción previa (si existe).
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. La lista la carga el ViewModel SOLO (en su init). El Fragment solo
        //    observa y reacciona; no lanza la petición aquí para no duplicarla.
        initializeRecyclerViewAndAdapter()

        // 2. Nos SUSCRIBIMOS a los LiveData del ViewModel.
        observer()

        // 3. Configuramos el scroll infinito: al llegar al final, se pide más.
        initScrollListener()
    }

    /**
     * CONFIGURACIÓN DE PAGINACIÓN (SCROLL INFINITO) + GESTIÓN DE IMÁGENES
     * Añade un RecyclerView.OnScrollListener que se dispara cada vez que el
     * usuario hace scroll. Dos responsabilidades:
     *
     * 1) PAGINACIÓN (onScrolled): el Fragment NO decide nada; reenvía los dos
     *    números del RecyclerView (último item visible y total) al ViewModel,
     *    que es quien tiene la lógica de cuándo cargar más.
     *
     * 2) IMÁGENES FLUIDAS (onScrollStateChanged): mientras el usuario está
     *    SCROLLANDO (arrastrando o con inercia), le decimos al adapter que las
     *    imágenes solo vengan de la CACHÉ (aparecen al instante o placeholder,
     *    y no saturan la API con decenas de descargas a la vez, evitando los
     *    errores con scroll rápido). Cuando el dedo se DETIENE (IDLE), volvemos
     *    al modo RED y repintamos las filas visibles para que descarguen las
     *    imágenes que faltan.
     */
    private fun initScrollListener() {
        binding.rvCharacters.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                viewModel.onListScrolled(
                    lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition(),
                    totalItemCount = layoutManager.itemCount
                )
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // ¿El usuario detuvo el scroll? (dedo levantado o inercia terminada)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    adapter.setLoadImagesFromCache(false)
                    // Repintamos SOLO las filas visibles: las que se quedaron con
                    // placeholder por el scroll rápido ahora descargan de la red.
                    rebindVisibleItems(recyclerView)
                } else {
                    // Scroll en movimiento: solo caché, fluido y sin saturar la red.
                    adapter.setLoadImagesFromCache(true)
                }
            }
        })
    }

    /**
     * Lee las filas actualmente VISIBLES en pantalla y las repinta (rebind).
     * Es lo que desbloquea las imágenes cuando la lista pasa de scroll a
     * reposo antes mencionado. findViewHolderForAdapterPosition devuelve el
     * holder si está pintado; si no existe (filas fuera de pantalla) lo
     * ignoramos: RecyclerView las pintará al entrar.
     */
    private fun rebindVisibleItems(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val first = layoutManager.findFirstVisibleItemPosition()
        val last = layoutManager.findLastVisibleItemPosition()
        for (position in first..last) {
            val holder = recyclerView
                .findViewHolderForAdapterPosition(position) as? CharacterListAdapter.ViewHolder
            holder?.rebind()
        }
    }

    /**
     * Configuración del RecyclerView: mismo comportamiento de siempre,
     * pero ahora las vistas provienen del binding del fragment.
     */
    private fun initializeRecyclerViewAndAdapter() {
        binding.rvCharacters.adapter = adapter
        binding.rvCharacters.layoutManager = LinearLayoutManager(requireContext())
    }

    /**
     * Observadores de LiveData: igual que en la Activity. La única diferencia:
     * el "this" de observe() es el fragment, que implementa LifecycleOwner.
     */
    private fun observer() {
        viewModel.characterList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            // Mostramos/ocultamos el ProgressBar inferior mientras carga una página.
            binding.pbLoading.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * onDestroyView: cuando la vista del fragment se destruye (rotación,
     * navegación a otra pantalla, etc.), liberamos el binding para no retener
     * referencias a vistas destruidas. Es una buena práctica imprescindible
     * con ViewBinding en fragments para evitar fugas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}