package com.irv205.rickmortycodingchallengexml.presentation.character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.irv205.rickmortycodingchallengexml.databinding.FragmentCharacterListBinding
import dagger.hilt.android.AndroidEntryPoint

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

    /** Mismo adaptador que antes, perezoso (se crea solo la 1ª vez que se accede). */
    private val adapter by lazy { CharacterListAdapter() }

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

        // 1. La vista le PIDE datos al ViewModel (nunca llama a la red).
        viewModel.getCharacters()

        // 2. Configuramos el RecyclerView con su adaptador.
        initializeRecyclerViewAndAdapter()

        // 3. Nos SUSCRIBIMOS a los LiveData del ViewModel.
        observer()
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