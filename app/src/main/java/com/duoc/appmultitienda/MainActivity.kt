package com.duoc.appmultitienda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.delay
import com.duoc.appmultitienda.core.ui.SplashMultiTienda
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.duoc.appmultitienda.core.data.LocalInventarioGateway
import com.duoc.appmultitienda.core.ui.AppRoot
import com.duoc.appmultitienda.core.ui.MultiTiendaTheme

/** Arranque único: pantallas 1.0.1 + fuente local del proyecto integrado. */
class MainActivity : ComponentActivity() {
    private val repositorio by lazy { LocalInventarioGateway(applicationContext) }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultiTiendaTheme {
                var mostrandoSplash by rememberSaveable { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    delay(1_800L)
                    mostrandoSplash = false
                }
                if (mostrandoSplash) {
                    SplashMultiTienda()
                } else {
                    val ancho = calculateWindowSizeClass(this).widthSizeClass
                    AppRoot(repositorio, amplia = ancho != WindowWidthSizeClass.Compact)
                }
            }
        }
    }
}
