package com.irv205.rickmortycodingchallengexml.presentation.character

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.irv205.rickmortycodingchallengexml.R
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
 * Clase del adaptador. Constructor con UN parámetro:
 *   - onItemClick: (Character) -> Unit. Es una FUNCIÓN que el adaptador ejecuta
 *     cuando el usuario PULSA una fila. Quién la crea (el fragment) decide QUÉ
 *     pasa con el carácter pulsado; el adaptador solo avisa. De esta forma el
 *     adaptador permanece genérico y no sabe nada de navegación.
 *
 * @param onItemClick Callback que recibe el Character de la fila pulsada.
 */
class CharacterListAdapter(
    private val onItemClick: (Character) -> Unit = {}
) : ListAdapter<Character, CharacterListAdapter.ViewHolder>(CharacterDiffCallback) {

    /**
     * Opciones base de Glide para TODAS las imágenes de la lista:
     *   - centerCrop(): recorta y centra la imagen para rellenar el ImageView.
     *   - override(200, 200): fuerza la decodificación a un tamaño pequeño.
     *     La fila muestra la imagen en 100dp; cargar el JPEG original (puede
     *     ser de 300x300 o más) es despilfarro de memoria y tiempo. Al fijar
     *     tamaño, la descarga es MÁS LIGERA y más fácil de cachear.
     *   - placeholder / error: colores de relleno mientras carga y si falla.
     *     RequestOptions es INMUTABLE: las .xxx() devuelven copias nuevas,
     *     por eso se puede compartir sin que se "contamine".
     */
    private val baseOptions = RequestOptions()
        .centerCrop()
        .override(200, 200)
        .placeholder(R.color.image_placeholder)
        .error(R.color.image_placeholder)

    /**
     * Modo "solo caché". El fragment lo activa mientras el usuario hace scroll
     * y lo desactiva al frenar (IDLE):
     *   - true  -> Glide SOLO lee de memoria/disco. Si la imagen ya se cargó
     *              antes aparece AL INSTANTE; si no, se queda con el placeholder.
     *              Así durante el scroll no se satura la red ni se disparan
     *              decenas de descargas simultáneas (evita errores al hacer
     *              fling rápido).
     *   - false -> Glide carga de la red normalmente.
     */
    private var loadImagesFromCacheOnly = false

    /** Público: el fragment lo llama desde su scroll listener. */
    fun setLoadImagesFromCache(cacheOnly: Boolean) {
        loadImagesFromCacheOnly = cacheOnly
    }

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

        // El Character que este holder pinta ahora mismo. Lo guardamos para
        // poder repintar la fila sin depender de su posición (ver rebind()).
        private var currentItem: Character? = null

        /**
         * bind: rellena las vistas del holder con los datos del Character.
         * binding.apply { ... } permite referirse a las vistas del binding (ivCharacter,
         * tvNameCharacter, tvStatus) SIN escribir "binding." delante de cada una.
         *
         * Glide recibe:
         *   - .apply(options)       : placeholder/error y "solo caché o red"
         *                             (según el modo activado por el scroll).
         *   - .transform(RoundedCorners(20)) : esquinas redondeadas.
         *
         * La ÚLTIMA línea asigna el Listener de click sobre TODA la fila
         * (itemView es la vista raíz del layout que inflamos: recuerda, en
         * ViewHolder(...) pasamos binding.root ESO es itemView). Al pulsar
         * cualquier zona del item, se ejecuta onItemClick(item) con SU personaje.
         */
        fun bind(item: Character) {
            currentItem = item
            binding.apply {
                // En modo solo-caché le pedimos a Glide que NI INTENTE la red;
                // así el scroll se mantiene fluido y no satura la API.
                val options = if (loadImagesFromCacheOnly) {
                    baseOptions.onlyRetrieveFromCache(true)
                } else {
                    baseOptions
                }
                Glide.with(ivCharacter)
                    .load(item.image)
                    .apply(options)
                    .transform(RoundedCorners(20))
                    .into(ivCharacter)
                tvNameCharacter.text = item.name
                tvStatus.text = item.status
            }
            // Click en la fila -> ejecutamos el callback con el personaje pulsado.
            itemView.setOnClickListener { onItemClick(item) }
        }

        /**
         * Repinta el holder con su item actual. Lo usa el fragment al llegar al
         * estado IDLE: las filas visibles que se quedaron con placeholder por el
         * scroll rápido ahora se descargan de la red de verdad.
         */
        fun rebind() {
            currentItem?.let { bind(it) }
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