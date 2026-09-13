package com.irv205.rickmortycodingchallengexml.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.irv205.rickmortycodingchallengexml.R
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

        // Configura el diseño "edge-to-edge": el contenido se extiende
        // por detrás de la barra de estado y la barra de navegación.
        enableEdgeToEdge()

        // Indica el layout XML que esta Activity va a mostrar.
        setContentView(R.layout.activity_main)

        /**
         * Listener de insets (márgenes del sistema operativo). Android nos avisa
         * del tamaño de la barra de estado/navegación para que nuestro contenido
         * no quede tapado. Aquí añadimos ese "padding" a la vista raíz (id "main").
         */
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // La Vista le PIDE datos al ViewModel. Nunca hace llamadas de red aquí.
        viewModel.getCharacters()
    }
}