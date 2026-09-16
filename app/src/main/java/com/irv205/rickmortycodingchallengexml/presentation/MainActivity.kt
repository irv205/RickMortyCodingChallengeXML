package com.irv205.rickmortycodingchallengexml.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.irv205.rickmortycodingchallengexml.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * ============================================================
 * CAPA DE PRESENTACIÓN: VISTA / ACTIVITY (ARQUITECTURA MVVM)
 * ============================================================
 * Esta Activity es un CONTENEDOR MÍNIMO dentro de la arquitectura MVVM.
 * Ya no se encarga de la lista (eso vive en CharacterListFragment); solo:
 *   1) Infla su layout, que contiene un FragmentContainerView.
 *   2) Ese contenedor está conectado al grafo de navegación (main_graph.xml),
 *      así que es ANDROID NAVIGATION quien muestra el CharacterListFragment
 *      automáticamente (la pantalla de inicio del grafo).
 *
 * ¿Por qué no hay "supportFragmentManager.replace(...)" aquí?
 * Porque en activity_main.xml el FragmentContainerView declara el atributo
 * app:navGraph. Con esse atributo, él mismo se convierte en un "NavHost" y se
 * encarga de mostrar el fragment de inicio. No necesitamos transacciones a mano.
 *
 * @AndroidEntryPoint: sigue siendo necesario porque la Activity es el punto
 * de entrada de Hilt (el "host" del grafo de dependencias). Sin él, Hilt no
 * podría proveer el ViewModel al fragment que hospeda.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContentView SIEMPRE: el contenedor debe existir aunque el fragment
        // se recupere tras una rotación. ActivityMainBinding ya no se usa:
        // MainActivity solo tiene el FragmentContainerView a rellenar.
        setContentView(R.layout.activity_main)
    }
}