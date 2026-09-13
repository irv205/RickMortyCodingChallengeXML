package com.irv205.rickmortycodingchallengexml

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * La clase Application es el punto de entrada de toda app Android.
 * Android crea una instancia única de esta clase ANTES de crear cualquier Activity,
 * y vive mientras la app exista. Es el lugar ideal para inicializar librerías globales.
 *
 * @HiltAndroidApp es una anotación de Hilt (la librería de inyección de dependencias).
 * Al ponerla sobre nuestra clase Application, Hilt:
 *   1) Genera automáticamente todas las clases internas del "componente raíz"
 *      (el contenedor de objetos que gestiona las dependencias de toda la app).
 *   2) Activa el proceso de inyección de dependencias en toda la aplicación.
 *   3) Es OBLIGATORIA: sin ella Hilt no genera nada y el proyecto no compila.
 *
 * Esta clase debe declararse en el AndroidManifest.xml en el atributo android:name=".App"
 * para que el sistema operativo sepa que esta es nuestra clase Application personalizada.
 */
@HiltAndroidApp
class App : Application()