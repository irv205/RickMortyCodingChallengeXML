package com.irv205.rickmortycodingchallengexml.data.local

import androidx.room.TypeConverter
import org.json.JSONArray

/**
 * ============================================================
 * CAPA DE DATOS: CONVERTIDORES DE TIPO PARA ROOM
 * ============================================================
 * Room guarda columnas de tipos "sencillos" (Int, String, Boolean...).
 * Nuestro campo "episode" es un List<String> (URLs de episodios) y Room NO sabe
 * serializarlo solo. Los TypeConverters resuelven eso: indican cómo pasar
 * del tipo Kotlin al tipo SQLite y viceversa.
 *
 * Solución elegida: convertimos la lista a un JSON (texto) con org.json
 * (incluido en Android, sin dependencias extra) y luego la reconstruimos.
 *
 * OJO: para que Room se entere de que estos convertidores existen, el objeto
 * Converters debe registrarse con @TypeConverters(...) en la clase AppDatabase.
 */
class Converters {

    /**
     * List<String> -> String: convierte la lista de episodios a un JSONArray
     * [ "url1", "url2" ] y lo guardamos como texto en la columna.
     */
    @TypeConverter
    fun fromEpisodeList(value: List<String>): String {
        return JSONArray(value).toString()
    }

    /**
     * String -> List<String>: lee el texto de la columna y reconstruye la lista.
     * Si lo que hay está vacío, devolvemos una lista vacía en vez de romper.
     */
    @TypeConverter
    fun toEpisodeList(value: String): List<String> {
        val array = JSONArray(value)
        return (0 until array.length()).map { array.getString(it) }
    }
}