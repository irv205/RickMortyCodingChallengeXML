package com.irv205.rickmortycodingchallengexml.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ============================================================
 * CAPA DE DATOS: ENTIDAD DE ESTADO DE PAGINACIÓN
 * ============================================================
 * Guarda la URL de la SIGUIENTE página ("info.next" que mandó el servidor en
 * la última consulta). Es la "memoria" de cuántas páginas hemos descargado:
 *
 *   - nextPageUrl != null -> la caché está a MEDIAS: aún quedaban páginas por
 *                            descargar cuando se cerró la app. Al reabrirla, se
 *                            reanuda DESDE esta URL (no se vuelve a empezar).
 *   - nextPageUrl == null -> la caché está COMPLETA: la última página descargada
 *                            era la última del servidor. No hay más que pedir.
 *
 * Es una tabla de UNA SOLA fila (una sola app, un solo estado de paginación):
 * usamos id fijo = 1 como clave. No hay relación con CharacterEntity; es
 * información de metadatos, no de un personaje concreto.
 */
@Entity(tableName = "page_info")
data class PageInfoEntity(
    @PrimaryKey
    val id: Int = 1,
    val nextPageUrl: String?
)