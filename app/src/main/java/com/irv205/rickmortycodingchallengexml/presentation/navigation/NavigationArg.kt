package com.irv205.rickmortycodingchallengexml.presentation.navigation

/**
 * ============================================================
 * ARGUMENTOS DE NAVEGACIÓN (CATÁLOGO DE CLAVES)
 * ============================================================
 * Catálogo ÚNICO de las claves que viajan entre destinos del grafo de
 * navegación (main_graph.xml). Cada constante debe coincidir con el
 * <argument android:name="..."> de su destino.
 *
 * ¿Por qué un enum y no una función de extracción por parámetro?
 * Porque el número de tipos de Bundle es fijo y pequeño (Int, String,
 * Boolean, Long, ...). Con este catálogo + getters genéricos por TIPO
 * (getIntArg, getStringArg, ...) no hace falta escribir un getter por
 * cada parámetro nuevo: las claves se centralizan aquí y Navigator los
 * lee con un método por tipo, no por argumento.
 */
enum class NavigationArg(val key: String) {
    CHARACTER_ID("characterId")
}