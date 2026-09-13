package com.irv205.rickmortycodingchallengexml.core.di

import com.irv205.rickmortycodingchallengexml.data.repository.AppRepositoryImpl
import com.irv205.rickmortycodingchallengexml.domain.repository.AppRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ============================================================
 * HILT / INYECCIÓN DE DEPENDENCIAS: MÓDULO DE REPOSITORIO
 * ============================================================
 * Este @Module resuelve UN problema concreto de diseño:
 *
 * El ViewModel necesita un "AppRepository", pero AppRepository es una INTERFAZ
 * (un contrato), no una clase instanciable. ¿Cuál implementación le damos?
 *   -> AppRepositoryImpl, que hace las llamadas de red con Retrofit.
 *
 * @Binds es LA anotación para este caso. Le dice a Hilt:
 *   "Cuando alguien pida un AppRepository, entrégale un AppRepositoryImpl".
 * Es decir: enlaza (bind) la interfaz con la implementación concreta.
 *
 * COMPARACIÓN CON NetworkModule:
 *   - NetworkModule  usa @Provides  -> fabrica objetos que Hilt no puede crear solo
 *                                     (Retrofit, OkHttp, ApiService).
 *   - RepositoryModule usa @Binds  -> solo dice cuál implementación usar para una
 *                                     interfaz ya existente e instanciable.
 *
 * Al ser "abstract class", las funciones @Binds no llevan cuerpo: solo declaran
 * la relación. Hilt se encarga de instanciar AppRepositoryImpl (que con @Inject
 * constructor, declara cómo construirse él mismo).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * @Binds: registra que la interfaz AppRepository se resuelve con AppRepositoryImpl.
     * @Singleton: la misma instancia del repositorio se comparte en toda la app.
     *
     * @param appRepositoryImpl La implementación concreta (Hilt la inyectará con
     *        el constructor anotado con @Inject de AppRepositoryImpl).
     * @return AppRepository La interfaz tal y como la consume el ViewModel.
     */
    @Binds
    @Singleton
    abstract fun providerRepository(appRepositoryImpl: AppRepositoryImpl): AppRepository
}