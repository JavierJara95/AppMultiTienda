package com.duoc.appmultitienda.core.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.duoc.appmultitienda.R

/** Presentación visual de inicio. No modifica sesión, datos ni permisos. */
@Composable
fun SplashMultiTienda() {
    val transicion = rememberInfiniteTransition(label = "marca")
    val escala by transicion.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulso",
    )
    Box(
        modifier = Modifier.fillMaxSize().background(Marino).statusBarsPadding().navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.logo_mockup),
                contentDescription = "Logotipo MultiTienda",
                modifier = Modifier.size(176.dp).graphicsLayer(scaleX = escala, scaleY = escala),
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White)) { append("Multi") }
                    withStyle(SpanStyle(color = Celeste)) { append("Tienda") }
                },
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = "Tu inventario, en cada ubicación.",
                color = Color(0xFFBAD0DE),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                modifier = Modifier.width(145.dp).height(3.dp),
                color = Celeste,
                trackColor = Color(0xFF194156),
            )
        }
    }
}
