package com.irv205.rickmortycodingchallengexml.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ============================================================
 * CAPA DE DATOS: ENTIDAD DE ROOM (TABLA "characters")
 * ============================================================
 * Una @Entity define la TABLA de la base de datos local. Cada propiedad
 * es una COLUMNA y cada instancia de la clase es una FILA.
 *
 * - Inflamos los campos de origen y ubicación: origin y location son objetos
 *   anidados en la API (name + url). Room podría guardarlos como objetos
 *   independientes, pero para este challenge es más simple aplanarlos en
 *   columnas separadas (originName, originUrl, locationName, locationUrl).
 * - "episode" es List<String>: necesita un TypeConverter (ver Converters.kt).
 * - @PrimaryKey -> id: valor único que identifica cada fila. Como la API ya
 *   nos da un id, lo usamos directo. Con REPLACE en el DAO, si llega otro
 *   personaje con el mismo id se actualiza en vez de duplicarse.
 */
@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val originName: String,
    val originUrl: String,
    val locationName: String,
    val locationUrl: String,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String
)