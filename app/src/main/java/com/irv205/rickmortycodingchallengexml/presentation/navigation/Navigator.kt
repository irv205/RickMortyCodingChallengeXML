package com.irv205.rickmortycodingchallengexml.presentation.navigation

import android.os.Bundle

/**
 * ============================================================
 * NAVEGACIÓN CENTRALIZADA (CONTRATO / INTERFAZ)
 * ============================================================
 * Un Navigator es la única capa que SABE CÓMO se navega en la app:
 *   - a qué destino ir (id de la action del grafo de navegación),
 *   - qué argumentos empaquetar en la navegación,
 *   - cómo leer los argumentos que llegan a un destino.
 *
 * Los Fragments NO deben manejar esa lógica. Solo la LLAMAN, pasando
 * ÚNICAMENTE los datos que la navegación necesita (p. ej. el id del
 * personaje). El NavController lo resuelve internamente el Navigator.
 *
 * Así las clases de navegación (action ids, claves de Bundle) viven en UN
 * solo sitio (NavigatorImpl) y los Fragments quedan desacoplados del grafo.
 *
 * La LECTURA de argumentos también es responsabilidad del Navigator, pero con
 * getters genéricos por TIPO (ver getIntArg/getStringArg): el número de métodos
 * está acotado por los tipos de Bundle, no por la cantidad de parámetros.
 */
interface Navigator {

    /**
     * Navega al destino DETALLE (CharacterDetailsFragment) desde la lista.
     * @param characterId Id del personaje que se verá en el detalle.
     */
    fun navigateToCharacterDetails(characterId: Int)

    /**
     * Lee un argumento de tipo Int del Bundle de un destino.
     * @param arguments Bundle de argumentos del fragment destino (requireArguments()).
     * @param arg       Constante del catálogo NavigationArg que se quiere leer.
     * @return El valor del argumento.
     */
    fun getIntArg(arguments: Bundle, arg: NavigationArg): Int

    /**
     * Lee un argumento de tipo String del Bundle de un destino.
     * El argumento se considera OBLIGATORIO (declarado en el grafo).
     * @param arguments Bundle de argumentos del fragment destino (requireArguments()).
     * @param arg       Constante del catálogo NavigationArg que se quiere leer.
     * @return El valor del argumento.
     * @throws IllegalArgumentException Si el argumento no llegó (error de programación).
     */
    fun getStringArg(arguments: Bundle, arg: NavigationArg): String
}