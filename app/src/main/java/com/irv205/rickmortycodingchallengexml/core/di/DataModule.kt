package com.irv205.rickmortycodingchallengexml.core.di

import android.content.Context
import androidx.room.Room
import com.irv205.rickmortycodingchallengexml.data.local.AppDatabase
import com.irv205.rickmortycodingchallengexml.data.local.CharacterDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ============================================================
 * HILT / INYECCIÓN DE DEPENDENCIAS: MÓDULO DE DATOS LOCAL (ROOM)
 * ============================================================
 * Igual que NetworkModule fabrica la red, este módulo fabrica la BD local.
 * Room no se construye solo: necesita un Context y una "receta" de creación,
 * así que usamos @Provides para darle a Hilt las instrucciones.
 *
 * @InstallIn(SingletonComponent::class): al nivel de toda la app, para que solo
 * haya UNA instancia compartida de la BD y del DAO (nunca abrir la BD dos veces).
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /**
     * RECETA 1: crear la AppDatabase.
     * Room.databaseBuilder es el patrón Builder de Room:
     *   - context: el contexto de la app (@ApplicationContext), NO el de una
     *     Activity o Fragment (no debe filtrarse su ciclo de vida).
     *   - AppDatabase::class.java: la clase con el @Database.
     *   - "rick_morty.db": nombre del archivo de la BD en el dispositivo.
     * @Singleton: una única instancia para toda la app.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "rick_morty.db"
        ).build()
    }

    /**
     * RECETA 2: exponer el DAO de personajes.
     * AppDatabase ya expone characterDao() (método abstracto que Room implementa);
     * aquí solo se lo "pasamos" a Hilt para que cualquiera pueda pedir el DAO.
     */
    @Provides
    fun provideCharacterDao(database: AppDatabase): CharacterDao {
        return database.characterDao()
    }
}