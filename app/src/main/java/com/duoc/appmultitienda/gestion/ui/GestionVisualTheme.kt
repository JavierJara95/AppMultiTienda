package com.duoc.appmultitienda.gestion.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Paleta tomada del módulo Operador y ajustada al mockup MultiTienda. */
object GestionColores {
    val fondo = Color(0xFFF5F8FB)
    val cabecera = Color(0xFF061D30)
    val tarjeta = Color.White
    val tarjetaElevada = Color(0xFFEAF2F7)
    val contorno = Color(0xFFD8E5ED)
    val texto = Color(0xFF0A2B43)
    val textoSecundario = Color(0xFF536E81)
    val azulAccion = Color(0xFF0B4C70)
    val verde = Color(0xFF008866)
    val rojo = Color(0xFFC83349)
    val naranja = Color(0xFFB86B14)
}

private val GestionScheme = lightColorScheme(
    primary = GestionColores.azulAccion,
    onPrimary = Color.White,
    primaryContainer = GestionColores.tarjeta,
    onPrimaryContainer = GestionColores.texto,
    secondary = GestionColores.verde,
    onSecondary = Color.White,
    background = GestionColores.fondo,
    onBackground = GestionColores.texto,
    surface = GestionColores.tarjeta,
    onSurface = GestionColores.texto,
    surfaceVariant = GestionColores.tarjetaElevada,
    onSurfaceVariant = GestionColores.textoSecundario,
    surfaceContainer = GestionColores.tarjeta,
    surfaceContainerLow = GestionColores.fondo,
    surfaceContainerHigh = GestionColores.tarjetaElevada,
    surfaceContainerHighest = GestionColores.tarjetaElevada,
    outline = GestionColores.contorno,
    error = GestionColores.rojo,
    onError = Color.White,
    inverseSurface = GestionColores.cabecera,
    inverseOnSurface = Color.White
)

@Composable
fun GestionVisualTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = GestionScheme, content = content)
}

@Composable
fun GestionSeccionTitulo(titulo: String, subtitulo: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(titulo, color = GestionColores.texto, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        if (subtitulo != null) Text(subtitulo, color = GestionColores.textoSecundario, fontSize = 13.sp)
    }
}

@Composable
fun GestionTarjeta(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(
            containerColor = GestionColores.tarjeta,
            contentColor = GestionColores.texto
        ),
        border = BorderStroke(1.dp, GestionColores.contorno)
    ) {
        CompositionLocalProvider(LocalContentColor provides GestionColores.texto) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )
        }
    }
}


/** El acceso conserva su aspecto marino; las pantallas de Gestión usan tema claro. */
private val AccesoScheme = darkColorScheme(
    primary = Color(0xFF008CFF), onPrimary = Color.White,
    background = Color(0xFF06283D), onBackground = Color.White,
    surface = Color(0xFF0C4869), onSurface = Color.White,
    surfaceVariant = Color(0xFF124F70), onSurfaceVariant = Color(0xFFD3E4EE),
    outline = Color(0xFF40748F)
)

@Composable
fun AccesoVisualTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AccesoScheme, content = content)
}
