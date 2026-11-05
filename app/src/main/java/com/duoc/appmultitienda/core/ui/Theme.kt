package com.duoc.appmultitienda.core.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Marino = Color(0xFF041D31)
val Tinta = Color(0xFF0A2B48)
val Petroleo = Color(0xFF0D506A)
val Azul = Color(0xFF0087FF)
val Celeste = Color(0xFF00B7F2)
val Exito = Color(0xFF088655)
val Error = Color(0xFFD92648)
val Aviso = Color(0xFFAF6900)
val Muted = Color(0xFF56779B)
val Borde = Color(0xFFDCE7F1)
val Fondo = Color(0xFFF4F8FB)

@Composable
fun MultiTiendaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme =
            lightColorScheme(
                primary = Petroleo,
                secondary = Azul,
                tertiary = Celeste,
                background = Fondo,
                surface = Color.White,
                onSurface = Tinta,
                onBackground = Tinta,
                onPrimary = Color.White,
                error = Error,
                outline = Borde,
            ),
        typography =
            Typography(
                headlineLarge =
                    TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp,
                    ),
                headlineSmall =
                    TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
                titleLarge =
                    TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp),
                titleMedium =
                    TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, lineHeight = 21.sp),
                bodyLarge = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
                bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
                bodySmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp),
                labelLarge =
                    TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp),
                labelSmall = TextStyle(fontSize = 10.sp, lineHeight = 14.sp),
            ),
        content = content,
    )
}
