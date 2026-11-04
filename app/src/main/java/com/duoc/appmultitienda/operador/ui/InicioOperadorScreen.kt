package com.duoc.appmultitienda.operador.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// =====================================================
// COLORES DEL DISEÑO MULTITIENDA
// =====================================================

private val AzulFondo = Color(0xFF06283D)
private val AzulOscuro = Color(0xFF061D30)
private val AzulTarjeta = Color(0xFF0C4869)
private val AzulTexto = Color(0xFF55708E)

private val Blanco = Color.White
private val GrisClaro = Color(0xFFF8FAFC)

private val Celeste = Color(0xFF008CFF)
private val Verde = Color(0xFF00A878)
private val Morado = Color(0xFF9447FF)
private val Rojo = Color(0xFFE83F5B)


// =====================================================
// PANTALLA PRINCIPAL DEL OPERADOR
// =====================================================

@Composable
fun InicioOperadorScreen(

    nombreSucursal: String,

    onCambiarUbicacion: () -> Unit,

    onEscanear: () -> Unit,

    onConteo: () -> Unit,

    onEntrada: () -> Unit,

    onSalida: () -> Unit,

    onHistorial: () -> Unit,

    onPerfil: () -> Unit

) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulFondo)
    ) {

        // =================================================
        // CONTENIDO CON DESPLAZAMIENTO
        // =================================================

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = 20.dp,
                    bottom = 24.dp
                ),

            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // =============================================
            // ENCABEZADO
            // =============================================

            Row(
                modifier = Modifier.fillMaxWidth(),

                verticalAlignment = Alignment.CenterVertically,

                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                Surface(
                    modifier = Modifier.size(40.dp),

                    shape = CircleShape,

                    color = Color(0xFF075B69)
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "⬡",
                            color = Color(0xFF25D8C7),
                            fontSize = 26.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Hola, Operador",
                        color = Blanco,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "⌖  $nombreSucursal",
                        color = Color(0xFFADC7D8),
                        fontSize = 13.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "♧",
                        color = Blanco,
                        fontSize = 25.sp
                    )

                    Surface(
                        color = Color(0xFF19455B),
                        shape = RoundedCornerShape(20.dp)
                    ) {

                        Text(
                            text = "Operador",
                            color = Color(0xFFB7D1DF),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            )
                        )
                    }
                }
            }


            // =============================================
            // TARJETA RESUMEN DE INVENTARIO
            // =============================================

            Card(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(21.dp),

                colors = CardDefaults.cardColors(
                    containerColor = AzulTarjeta
                ),

                border = BorderStroke(
                    1.dp,
                    Color(0xFF245A76)
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Inventario de la tienda",
                        color = Blanco,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),

                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = "—",
                                color = Blanco,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Productos activos",
                                color = Color(0xFFDEEFF7),
                                fontSize = 13.sp
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(55.dp),
                            color = Color(0xFF46718A)
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 24.dp)
                        ) {

                            Text(
                                text = "—",
                                color = Blanco,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Alertas de stock",
                                color = Color(0xFFDEEFF7),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "Saldos oficiales no disponibles · Modo demostración",
                        color = Color(0xFFA9C5D5),
                        fontSize = 11.sp
                    )
                }
            }


            // =============================================
            // REGISTRAR OPERACIÓN
            // =============================================

            Text(
                text = "Registrar operación",
                color = Blanco,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )


            // PRIMERA FILA

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                BotonOperacionOperador(
                    titulo = "Escanear\nproducto",
                    simbolo = "▥",
                    colorSimbolo = Celeste,
                    fondoSimbolo = Color(0xFFE4F3FF),
                    modifier = Modifier.weight(1f),
                    onClick = onEscanear
                )

                BotonOperacionOperador(
                    titulo = "Conteo\nfísico",
                    simbolo = "✓",
                    colorSimbolo = Morado,
                    fondoSimbolo = Color(0xFFF1E7FF),
                    modifier = Modifier.weight(1f),
                    onClick = onConteo
                )
            }


            // SEGUNDA FILA

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                BotonOperacionOperador(
                    titulo = "Entrada\nde stock",
                    simbolo = "↓",
                    colorSimbolo = Verde,
                    fondoSimbolo = Color(0xFFDFF8EC),
                    modifier = Modifier.weight(1f),
                    onClick = onEntrada
                )

                BotonOperacionOperador(
                    titulo = "Salida\nde stock",
                    simbolo = "↑",
                    colorSimbolo = Rojo,
                    fondoSimbolo = Color(0xFFFFE7EC),
                    modifier = Modifier.weight(1f),
                    onClick = onSalida
                )
            }


            // =============================================
            // TARJETA DE CONTEOS
            // =============================================

            Card(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(20.dp),

                colors = CardDefaults.cardColors(
                    containerColor = Blanco
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),

                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement = Arrangement.SpaceBetween,

                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Tus conteos",
                            color = AzulOscuro,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Hoy",
                            color = AzulTexto,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,

                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            text = "♙",
                            color = Color(0xFFE3A500),
                            fontSize = 22.sp
                        )

                        Column {

                            Text(
                                text = "Conteos pendientes de revisión",
                                color = AzulTexto,
                                fontSize = 13.sp
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = "Consulta el estado de tus solicitudes.",
                                color = AzulTexto,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = "Los conteos de demostración no actualizan el stock oficial.",
                        color = AzulTexto,
                        fontSize = 11.sp
                    )
                }
            }


            // =============================================
            // CAMBIAR UBICACIÓN
            // =============================================

            Button(
                onClick = onCambiarUbicacion,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Blanco,
                    contentColor = AzulOscuro
                ),

                shape = RoundedCornerShape(18.dp)
            ) {

                Text(
                    text = "⟳",
                    fontSize = 24.sp
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Cambiar ubicación",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }


            // =============================================
            // IDENTIFICACIÓN TEMPORAL
            // =============================================

            Text(
                text = "OPR-017 · Sesión de demostración",
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFA2BDCD),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }


        // =================================================
        // NAVEGACIÓN INFERIOR
        // =================================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Blanco)
                .navigationBarsPadding()
                .padding(
                    horizontal = 8.dp,
                    vertical = 10.dp
                ),

            horizontalArrangement = Arrangement.SpaceEvenly,

            verticalAlignment = Alignment.CenterVertically
        ) {

            BotonNavegacionOperador(
                simbolo = "⌂",
                titulo = "Inicio",
                seleccionado = true,
                onClick = {}
            )

            BotonNavegacionOperador(
                simbolo = "▥",
                titulo = "Escáner",
                seleccionado = false,
                onClick = onEscanear
            )

            BotonNavegacionOperador(
                simbolo = "◷",
                titulo = "Historial",
                seleccionado = false,
                onClick = onHistorial
            )

            BotonNavegacionOperador(
                simbolo = "♙",
                titulo = "Perfil",
                seleccionado = false,
                onClick = onPerfil
            )
        }
    }
}


// =====================================================
// COMPONENTE: BOTÓN DE OPERACIÓN
// =====================================================

@Composable
private fun BotonOperacionOperador(

    titulo: String,

    simbolo: String,

    colorSimbolo: Color,

    fondoSimbolo: Color,

    modifier: Modifier = Modifier,

    onClick: () -> Unit

) {

    Card(
        modifier = modifier
            .height(88.dp)
            .clickable(onClick = onClick),

        shape = RoundedCornerShape(17.dp),

        colors = CardDefaults.cardColors(
            containerColor = Blanco
        ),

        border = BorderStroke(
            1.dp,
            Color(0xFFE0E7ED)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Surface(
                modifier = Modifier.size(46.dp),

                shape = RoundedCornerShape(13.dp),

                color = fondoSimbolo
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = simbolo,
                        color = colorSimbolo,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = titulo,
                color = AzulOscuro,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// =====================================================
// COMPONENTE: NAVEGACIÓN INFERIOR
// =====================================================

@Composable
private fun RowScope.BotonNavegacionOperador(

    simbolo: String,

    titulo: String,

    seleccionado: Boolean,

    onClick: () -> Unit

) {

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        Surface(
            modifier = Modifier
                .width(55.dp)
                .height(34.dp),

            shape = RoundedCornerShape(20.dp),

            color = if (seleccionado) {
                Color(0xFFE4F3FF)
            } else {
                Color.Transparent
            }
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = simbolo,
                    fontSize = 25.sp,
                    color = if (seleccionado) {
                        Celeste
                    } else {
                        AzulTexto
                    }
                )
            }
        }

        Text(
            text = titulo,
            fontSize = 11.sp,
            color = if (seleccionado) {
                Celeste
            } else {
                AzulTexto
            },
            fontWeight = if (seleccionado) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )
    }
}