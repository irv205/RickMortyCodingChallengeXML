package com.irv205.rickmortycodingchallengexml.core.util

/**
 * ============================================================
 * CLASE SELLADA PARA MANEJAR RESULTADOS DE OPERACIONES
 * ============================================================
 * Una "sealed class" (clase sellada) en Kotlin permite definir un conjunto
 * CERRADO de subtipos para una misma operación. Solo se pueden crear los
 * subtipos declarados dentro de ella.
 *
 * En este challenge la usamos para estandarizar el resultado de cualquier
 * operación que pueda fallar (como una llamada a la red):
 *   - Success: operación terminó bien         -> trae los datos (data)
 *   - Error:   operación falló                -> trae el mensaje del fallo (message)
 *
 * Beneficios para la capa de presentación:
 *   - Con un "when" el compilador OBLIGA a tratar todos los casos posibles
 *     (no se te puede olvidar ninguno).
 *   - No dependemos de try/catch en la UI; la lógica de negocio envuelve
 *     el resultado y la vista simplemente reacciona.
 *
 * El "<T>" (genérico) permite reutilizar esta clase con cualquier tipo de dato:
 * ResponseHandler<List<Character>>, ResponseHandler<String>, etc.
 */
sealed class ResponseHandler<T> {

    /**
     * Resultado de éxito.
     * @param data El dato devuelto por la operación (p. ej. la lista de personajes).
     */
    data class Success<T>(val data: T) : ResponseHandler<T>()

    /**
     * Resultado de error.
     * @param message Descripción legible de lo que salió mal.
     */
    data class Error<T>(val message: String) : ResponseHandler<T>()
}