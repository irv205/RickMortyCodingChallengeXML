package com.irv205.rickmortycodingchallengexml.presentation

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.irv205.rickmortycodingchallengexml.R
import com.irv205.rickmortycodingchallengexml.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * ============================================================
 * CAPA DE PRESENTACIÓN: VISTA / ACTIVITY (ARQUITECTURA MVVM)
 * ============================================================
 * La Activity es el "ojo" de la app: se encarga SOLO de mostrar la interfaz
 * y escuchar al usuario. Toda la lógica de negocio vive en el ViewModel.
 *
 * @AndroidEntryPoint: marca esta Activity para que Hilt la inyecte
 * automáticamente con las dependencias que necesite (aquí, el ViewModel).
 * Este anotador también es obligatorio en las Activities que usen Hilt.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    /**
     * VIEWBINDING: acceso a las vistas del XML SIN findViewById.
     * Con "viewBinding = true" (en build.gradle.kts) el sistema genera clases
     * automáticas por cada layout: ActivityMainBinding pertenece a activity_main.xml.
     *
     * - binding.root    : la vista raíz del layout, que se entrega a setContentView.
     * - binding.rvCharacters : el RecyclerView que definimos en el XML.
     *
     * "lateinit var": declara una var que se inicializará más tarde (en onCreate),
     * porque Android aún no nos da el binding al construir la clase.
     */
    private lateinit var binding: ActivityMainBinding

    /**
     * "by lazy { ... }": la expresión SOLO se ejecuta la primera vez que se accede
     * a la variable. El MainAdapter no necesita datos para crearse, así que lo
     * preparamos de forma perezosa y lo reutilizamos todo el tiempo.
     */
    private val adapter by lazy { MainAdapter() }
    /**
     * by viewModels(): delegado de Kotlin que crea (o recupera) el MainViewModel
     * asociado automaticamente a esta Activity. Hilt lo fabrica porque la clase
     * tiene @HiltViewModel y su constructor @Inject.
     *
     * ¿Por qué "recupera"? El ViewModel sobrevive a los cambios de configuración:
     * al girar la pantalla, la Activity se reconstruye pero el ViewModel (y sus datos)
     * se conservan. Así no perdemos la lista de personajes cargada.
     */
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding: inflamos (creamos) las vistas a partir del XML y las
        // marcamos como el contenido que la Activity va a mostrar.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. La Vista le PIDE datos al ViewModel (nunca hace llamadas de red aquí).
        viewModel.getCharacters()

        // 2. Configuramos el RecyclerView con su adaptador.
        initializeRecyclerViewAndAdapter()

        // 3. Nos SUSCRIBIMOS a los LiveData del ViewModel para reaccionar a los datos.
        observer()
    }

    /**
     * Configuración del RecyclerView en dos piezas:
     *   - adapter      : el MainAdapter que dibuja cada fila (patrón Adapter).
     *   - layoutManager: decide CÓMO se ordenan las filas. LinearLayoutManager =
     *     una columna vertical que se puede hacer scroll (la lista típica).
     */
    private fun initializeRecyclerViewAndAdapter() {
        binding.rvCharacters.adapter = adapter
        binding.rvCharacters.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Observadores de LiveData. LiveData es "observable": cuando el valor cambia,
     * estamos aquí para reaccionar. La sintaxis "observe(this) { ... }" registra
     * un callback que Android ejecuta en el hilo principal (aunque el dato llegue
     * desde otro hilo, LiveData se encarga de saltar al main thread).
     *
     * 1) characterList: cuando el ViewModel tenga la lista de personajes
     *    la entregamos al adaptador con submitList(...), que además calcula
     *    las diferencias y anima/re-dibuja solo lo que cambió.
     *
     * 2) error: si algo falló, el ViewModel avisa aquí y mostramos un toast
     *    (notificación flotante breve en la parte inferior de la pantalla).
     */
    private fun observer() {
        viewModel.characterList.observe(this) {
            adapter.submitList(it)
        }
        viewModel.error.observe(this) {
            Toast.makeText(applicationContext, it, Toast.LENGTH_LONG).show()
        }
    }
}