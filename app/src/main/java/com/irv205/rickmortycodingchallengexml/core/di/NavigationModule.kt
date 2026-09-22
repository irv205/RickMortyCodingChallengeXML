package com.irv205.rickmortycodingchallengexml.core.di

import android.app.Activity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.irv205.rickmortycodingchallengexml.R
import com.irv205.rickmortycodingchallengexml.presentation.navigation.Navigator
import com.irv205.rickmortycodingchallengexml.presentation.navigation.NavigatorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

/**
 * ============================================================
 * HILT / INYECCIÓN DE DEPENDENCIAS: MÓDULO DE NAVEGACIÓN
 * ============================================================
 * Mismo patrón que RepositoryModule: @Binds enlaza la INTERFAZ Navigator
 * con su implementación concreta NavigatorImpl.
 *
 * Diferencia clave: vive en ActivityComponent (no SingletonComponent).
 * El NavController es de la ACTIVIDAD (pertenece al NavHost de su layout),
 * así que la navegación solo tiene sentido dentro del ciclo de vida de una
 * actividad. Si fuera singleton, retendría una referencia al NavController
 * de una actividad destruida/reciclada.
 *
 * @Provides provideNavController: "receta" para obtener el NavController del
 * NavHost de la actividad (la FragmentContainerView con id fragment_container
 * que declara app:navGraph en activity_main.xml). El Navigator lo recibe
 * inyectado y los Fragments ya no necesitan pasarlo.
 */
@Module
@InstallIn(ActivityComponent::class)
abstract class NavigationModule {

    /**
     * @Binds: "cuando alguien pida un Navigator, entrégale un NavigatorImpl".
     * @ActivityScoped: una instancia del navigator por cada actividad.
     */
    @Binds
    @ActivityScoped
    abstract fun bindNavigator(navigatorImpl: NavigatorImpl): Navigator

    companion object {

        /**
         * Localiza el NavController del NavHost de la actividad (id de la
         * FragmentContainerView declarada en activity_main.xml).
         */
        @Provides
        fun provideNavController(activity: Activity): NavController =
            Navigation.findNavController(activity, R.id.fragment_container)
    }
}