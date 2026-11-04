package com.duoc.appmultitienda.operador.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// =====================================================
// COLORES DE ERRORES MULTITIENDA
// =====================================================

private val RojoError = Color(0xFFB42318)

private val FondoError = Color(0xFFFFF1F0)


// =====================================================
// COMPONENTE DE ERROR REUTILIZABLE
// =====================================================

@Composable
fun MensajeErrorOperador(

    mensaje: String,

    modifier: Modifier = Modifier,

    onCerrar: (() -> Unit)? = null

) {

    if (mensaje.isBlank()) {
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),

        shape = RoundedCornerShape(14.dp),

        colors = CardDefaults.cardColors(
            containerColor = FondoError
        ),

        border = BorderStroke(
            1.dp,
            Color(0xFFF5C2C0)
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(
                text = "No se pudo completar la operación",
                color = RojoError,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = mensaje,
                color = RojoError,
                fontSize = 13.sp
            )

            if (onCerrar != null) {

                TextButton(
                    onClick = onCerrar
                ) {

                    Text(
                        text = "Cerrar mensaje",
                        color = RojoError
                    )
                }
            }
        }
    }
}