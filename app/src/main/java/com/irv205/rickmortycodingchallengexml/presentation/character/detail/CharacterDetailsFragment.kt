package com.irv205.rickmortycodingchallengexml.presentation.character.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.irv205.rickmortycodingchallengexml.databinding.FragmentCharacterDetailsBinding
import com.irv205.rickmortycodingchallengexml.domain.model.Character
import com.irv205.rickmortycodingchallengexml.presentation.navigation.NavigationArg
import com.irv205.rickmortycodingchallengexml.presentation.navigation.Navigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * ============================================================
 * CAPA DE PRESENTACIÓN: FRAGMENT DEL DETALLE (MVVM)
 * ============================================================
 * Pantalla de DETALLE de un personaje. Se llega desde la lista tocando una fila;
 * CharacterListFragment navegó hasta este destino pasando "characterId" como
 * argumento del grafo de navegación.
 *
 * Flujo:
 *   1) En onViewCreated leemos el argumento characterId del Bundle recibido.
 *   2) Llamamos viewModel.getCharacterById(id): el ViewModel lo pide a través
 *      del caso de uso GetCharacterByIdUseCase (offline-first).
 *   3) Nos SUSCRIBIMOS a los LiveData y pintamos la información:
 *      - character -> bindCharacter(...) rellena las vistas del layout.
 *      - isLoading -> mostramos/ocultamos el ProgressBar.
 *      - error     -> mensaje al usuario.
 */
@AndroidEntryPoint
class CharacterDetailsFragment : Fragment() {

    // Mismo patrón de binding nullable que en CharacterListFragment.
    private var _binding: FragmentCharacterDetailsBinding? = null
    private val binding get() = requireNotNull(_binding)

    /** ViewModel del detalle, con @HiltViewModel (Hilt lo inyecta). */
    private val viewModel: CharacterDetailsViewModel by viewModels()

    /**
     * NAVEGADOR (inyectado por Hilt): conoce la clave del argumento characterId.
     * El fragment no sabe la clave del Bundle; solo pregunta por el id.
     */
    @Inject
    lateinit var navigator: Navigator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCharacterDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuramos el ProgressBar (oculto por defecto).
        binding.pbCharacterDetails.visibility = View.GONE

        // Nos suscribimos a los LiveData antes de pedir nada.
        observer()

        // Leemos el id (vía el navigator) y pedimos el personaje.
        val characterId = navigator.getIntArg(requireArguments(), NavigationArg.CHARACTER_ID)
        viewModel.getCharacterById(characterId)
    }

    /**
     * Observadores de los LiveData del ViewModel: pintan la UI cuando los datos
     * llegan y gestionan el estado de carga/error.
     */
    private fun observer() {
        viewModel.character.observe(viewLifecycleOwner) { character ->
            character?.let { bindCharacter(it) }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.pbCharacterDetails.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Rellena el layout con los datos del personaje.
     * mismo estilo que el adapter de la lista: Glide para la imagen (con
     * RoundedCorners) y .text para cada TextView.
     */
    private fun bindCharacter(character: Character) {
        binding.apply {
            Glide.with(ivCharacterImage)
                .load(character.image)
                .transform(RoundedCorners(30))
                .into(ivCharacterImage)
            tvCharacterName.text = character.name
            tvCharacterStatus.text = character.status
            tvCharacterSpecies.text = character.species
            tvCharacterGender.text = character.gender
            tvCharacterOrigin.text = character.origin.name
            tvCharacterLocation.text = character.location.name
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}