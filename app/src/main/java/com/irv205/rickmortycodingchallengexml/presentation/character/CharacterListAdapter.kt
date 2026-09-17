package com.irv205.rickmortycodingchallengexml.presentation.character

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.irv205.rickmortycodingchallengexml.databinding.ItemCharacterBinding
import com.irv205.rickmortycodingchallengexml.domain.model.Character

/**
 * ============================================================
 * ADAPTADOR DEL RECYCLERVIEW (PATRÓN ADAPTER)
 * ============================================================
 * Este adapter ("CharacterListAdapter") complementa al CharacterListFragment:
 * el fragment es la vista y el adaptador es quien sabe dibujar cada fila.
 *
 * Un RecyclerView NO sabe dibujar tus datos: solo sabe que va a mostrar porciones
 * de pantalla llamadas "holders". El patrón Adapter es el puente que le dice:
 *   - Cuántos elementos hay                 (ReciclerView lo pregunta).
 *   - Cómo inflar el layout de cada uno     (onCreateViewHolder).
 *   - Qué dato poner en cada elemento       (onBindViewHolder).
 *
 * ListAdapter<Character, ...>: una variante de RecyclerView.Adapter optimizada.
 * Dado que hereda de ListAdapter:
 *   - Recibe una List<Character> de golpe con submitList(...).
 *   - Calcula las DIFERENCIAS entre la lista anterior y la nueva con DiffUtil
 *     (un algoritmo de comparación) y solo re-dibuja (anima) los elementos
 *     que cambian. Mucho más eficiente que notifyDataSetChanged().
 *
 * Los parámetros entre <> del tipo son genéricos:
 *   - Character        (primer genérico) -> qué tipo de dato maneja cada item.
 *   - ViewHolder      (segundo genérico) -> qué clase dibuja cada item.
 */

/**
 * Clase del adaptador. "CharacterListAdapter()" con paréntesis vacíos (constructor
 * sin args): el adaptador no necesita ningún dato de entrada; los personajes le
 * llegan later mediante submitList(...) desde el fragment.
 */
class CharacterListAdapter() : ListAdapter<Character, CharacterListAdapter.ViewHolder>(CharacterDiffCallback) {

    /**
     * onCreateViewHolder: se llama cuando RecyclerView necesita crear UN NUEVO
     * contenedor visual (no cuando hay datos nuevos). Aquí "inflamos" (convertimos
     * a objetos vivos) el layout XML item_character.xml usando ViewBinding.
     *
     * LayoutInflater es la herramienta que lee un XML y lo transforma en un árbol
     * de vistas. .inflate(deContexto, elPadre, false) -> el "false" indica que el
     * layout NO se debe anexar todavía a la vista padre (lo hará el RecyclerView).
     *
     * @param p0 El ViewGroup padre (el propio RecyclerView) que contendrá el item.
     * @param p1 Tipo de vista (no usado; permite varios tipos de fila).
     * @return Un ViewHolder ya preparado que envuelve el binding del item.
     */
    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): ViewHolder {
        return ViewHolder(ItemCharacterBinding.inflate(LayoutInflater.from(p0.context), p0, false))
    }

    /**
     * onBindViewHolder: se llama cuando toca "pintar" un dato concreto en la posición
     * indicada. getItem(p1) recupera el Character de esa posición de la lista
     * y el ViewHolder lo dibuja con su método bind(...).
     *
     * @param p0 El ViewHolder de esa fila (reciclado o nuevo).
     * @param p1 La posición del elemento en la lista (0, 1, 2, ...).
     */
    override fun onBindViewHolder(
        p0: ViewHolder,
        p1: Int
    ) {
        val item = getItem(p1)
        p0.bind(item)
    }

    /**
     * ViewHolder: "holder de la vista". Envuelve las referencias al layout de UNA
     * fila para no buscar las vistas cada vez que se pinta el mismo elemento
     * (una optimización clave de RecyclerView: el holder se reutiliza).
     *
     * "inner class": necesita acceder a miembros de la clase externa
     * (CharacterListAdapter); aunque aquí realmente solo usa su propio binding.
     *
     * Extiende RecyclerView.ViewHolder(binding.root), que es la vista raíz del layout.
     */
    inner class ViewHolder(private val binding: ItemCharacterBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * bind: rellena las vistas del holder con los datos del Character.
         * binding.apply { ... } permite referirse a las vistas del binding (ivCharacter,
         * tvNameCharacter, tvStatus) SIN escribir "binding." delante de cada una.
         *
         * Glide es la librería de imágenes que elegimos:
         *   Glide.with(contexto).load(url).into(imageView)
         * descarga la imagen de Internet EN SEGUNDO PLANO, la cachea y la pinta.
         */
        fun bind(item: Character) {
            binding.apply {
                Glide.with(ivCharacter).load(item.image).transform(RoundedCorners(20)) .into(ivCharacter)
                tvNameCharacter.text = item.name
                tvStatus.text = item.status
            }
        }
    }
}

/**
 * OBJECT: DiffUtil / "Callback de diferencias".
 * En Kotlin, "object" crea un SINGLETON (una única instancia compartida en toda
 * la app). Es perfecto para un comparador sin estado como este.
 *
 * DiffUtil.ItemCallback<Character> le dice a ListAdapter cómo comparar filas
 * mediante DOS métodos (ambos obligatorios):
 *
 * 1) areItemsTheSame -> ¿son EL MISMO elemento de la lista? (compara "identidad").
 * 2) areContentsTheSame -> ¿tienen EL MISMO CONTENIDO? (compara "datos").
 *
 * La "identity" y el "content" pueden diferir: p. ej. "mismo personaje" pero con
 * "estado actualizado". Con esta información ListAdapter decide exactamente qué
 * filas animar/repintar al llegar una lista nueva.
 */
object CharacterDiffCallback : DiffUtil.ItemCallback<Character>() {

    /**
     * @param oldItem Personaje de la lista anterior.
     * @param newItem Personaje de la lista nueva.
     * @return true si son el mismo elemento. Ahora comparamos por "id" (campo
     *         único que añadimos al modelo de dominio) en vez de por igualdad
     *         estructural. Es OBLIGATORIO con la paginación: el adaptador recibe
     *         la lista ACUMULADA (página 1 + página 2 + ...) y con el id puede
     *         identificar los items que ya existen y solo dibujar/animar los
     *         personajes NUEVOS que llegan con cada página.
     */
    override fun areItemsTheSame(oldItem: Character, newItem: Character): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * @param oldItem Personaje de la lista anterior.
     * @param newItem Personaje de la lista nueva.
     * @return true si, siendo el mismo elemento, su contenido NO ha cambiado
     *         (no hace falta repintar). Como la lista es inmutable (data class),
     *         comparar con "==" es suficiente.
     */
    override fun areContentsTheSame(oldItem: Character, newItem: Character): Boolean {
        return oldItem == newItem
    }
}