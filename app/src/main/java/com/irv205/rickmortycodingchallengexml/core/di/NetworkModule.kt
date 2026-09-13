package com.irv205.rickmortycodingchallengexml.core.di

import com.irv205.rickmortycodingchallengexml.data.service.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * ============================================================
 * HILT / INYECCIÓN DE DEPENDENCIAS: MÓDULO DE RED
 * ============================================================
 * Arquitectura de red controlada por Hilt. Pero primero, ¿qué es una dependencia?
 *
 * Si una clase necesita otra para funcionar (ej: AppRepositoryImpl necesita ApiService),
 * decimos que la segunda es UNA DEPENDENCIA de la primera. En vez de crearla a mano
 * (con "new"), se la damos ya construida: eso es INYECCIÓN DE DEPENDENCIAS (DI).
 *
 * Hilt es la librería que gestiona esto de forma automática. Un @Module es una clase
 * "auxiliar" que le dice a Hilt CÓMO fabricar e instanciar ciertos objetos que
 * no puede construir él solo. Cada función @Provides es una receta de fabricación.
 *
 * @InstallIn(SingletonComponent::class): los objetos se crean al nivel de toda la app
 * (SingletonComponent = componente de vida completa). El mismo OkHttpClient o Retrofit
 * se comparte en toda la aplicación para no malgastar recursos.
 */
@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    /**
     * RECETA 1: cómo crear un OkHttpClient.
     * OkHttp es la librería de red subyacente que ejecuta las peticiones HTTP.
     * El .Builder() usa el patrón "Builder" de Java/Kotlin: se configura la
     * instancia paso a paso y al final .build() la crea.
     * Aquí lo dejamos con la configuración por defecto (tiempos de espera, etc.).
     *
     * @Singleton: esto le pide a Hilt que SOLO exista UNA instancia compartida
     * durante toda la vida de la app (no se crea de nuevo en cada petición).
     */
    @Provides
    @Singleton
    fun providerHttpClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .build()
    }

    /**
     * RECETA 2: cómo crear un Retrofit.
     * Retrofit es la librería elegante que convierte la interfaz ApiService en llamadas HTTP.
     *
     * Observa que esta receta RECIBE el OkHttpClient creado arriba: Hilt encadena
     * las dependencias automáticamente. Para fabricar Retrofit necesitamos:
     *   - .baseUrl(...) : URL raíz de la API (sin endpoint final).
     *   - .client(...)  : el OkHttpClient que ejecutará de verdad las peticiones.
     *   - .addConverterFactory(...) : ¡clave! convertir el JSON del servidor a objetos Kotlin.
     *
     * Moshi es la librería de serialización JSON que elegimos.
     *   - MoshiBuilder + KotlinJsonAdapterFactory: permite deserializar clases Kotlin
     *     que NO tienen adapter generado (como CharactersResponseDTO) usando reflexión.
     *   - MoshiConverterFactory.create(moshi): Registramos Moshi en Retrofit para que
     *     se encargue de interpretar el JSON de las respuestas.
     */
    @Provides
    @Singleton
    fun providerRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        return Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/api/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /**
     * RECETA 3: cómo crear la ApiService.
     * .create(ApiService::class.java) le pide a Retrofit que genere en tiempo de
     * ejecución la implementación real de la interfaz declarada en data/service.
     * Esta instancia es la que usaremos en toda la app para hacer las llamadas.
     */
    @Provides
    @Singleton
    fun providerService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}