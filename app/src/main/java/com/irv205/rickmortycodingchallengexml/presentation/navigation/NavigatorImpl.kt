package com.irv205.rickmortycodingchallengexml.presentation.navigation

import android.os.Bundle
import androidx.navigation.NavController
import com.irv205.rickmortycodingchallengexml.R
import dagger.Lazy
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

/**
 * ============================================================
 * NAVEGACIÓN CENTRALIZADA (IMPLEMENTACIÓN)
 * ============================================================
 * Único sitio de la app que conoce los entresijos de Android Navigation:
 *   - el NavController del NavHost de la actividad (inyectado por Hilt),
 *   - el id de la action "lista -> detalle" (main_graph.xml),
 *   - la clave de cada argumento (via el catálogo NavigationArg).
 *
 * Así los Fragments no saben NADA de esto: solo llaman a los métodos
 * de la interfaz Navigator, sin pasar el NavController (la vista solo dice
 * "quiero ir al detalle del id X"; el Navigator ya sabe cómo llegar).
 *
 * ¿Por qué Lazy<NavController> y no NavController directo? El primer fragment
 * del grafo (CharacterListFragment) se inyecta MIENTRAS MainActivity infla
 * su layout (setContentView): la FragmentContainerView todavía no está
 * adjunta a la Activity, así que resolver el NavController ahí mismo
 * (Navigation.findNavController -> requireViewById) lanza "ID does not
 * reference a View inside this Activity". Con Lazy, la resolución se aplaza
 * hasta el primer .get(), es decir, cuando el usuario pulsa una fila: para
 * entonces toda la jerarquía de vistas ya está adjunta y el NavController es
 * localizable.
 *
 * @ActivityScoped (no @Singleton): el NavController vive por actividad. Una
 * instancia global retendría una referencia caduca tras recrear la activity.
 */
@ActivityScoped
class NavigatorImpl @Inject constructor(
    private val navController: Lazy<NavController>
) : Navigator {

    /**
     * NAVEGACIÓN AL DETALLE: ejecuta la transición declarada en main_graph.xml
     * con el id de la action y empaqueta el id del personaje como argumento.
     *
     * @param characterId Id del personaje a mostrar en el detalle.
     */
    override fun navigateToCharacterDetails(characterId: Int) {
        navController.get().navigate(
            R.id.action_characterListFragment_to_characterDetailsFragment,
            Bundle().apply { putInt(NavigationArg.CHARACTER_ID.key, characterId) }
        )
    }

    /**
     * Getter genérico de Int: la clave sale del catálogo NavigationArg, no de
     * cada parámetro. Si algún día hay más argumentos Int, se reutiliza este
     * mismo método (no se crea uno nuevo por parámetro).
     */
    override fun getIntArg(arguments: Bundle, arg: NavigationArg): Int =
        arguments.getInt(arg.key)

    /**
     * Getter genérico de String (argumento OBLIGATORIO): si el fragment no
     * recibió el argumento, lanzamos un error de programación. requireNotNull
     * devuelve el valor o lanza IllegalArgumentException.
     */
    override fun getStringArg(arguments: Bundle, arg: NavigationArg): String =
        requireNotNull(arguments.getString(arg.key)) {
            "Navigation argument \"${arg.key}\" is missing (required in main_graph.xml)"
        }
}