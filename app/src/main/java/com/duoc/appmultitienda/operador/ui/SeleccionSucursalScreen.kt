package com.duoc.appmultitienda.operador.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duoc.appmultitienda.operador.model.SucursalOperador
import com.duoc.appmultitienda.operador.viewmodel.SeleccionSucursalViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Warehouse

import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Store

private val AzulOscuro = Color(0xFF061D30)
private val AzulBoton = Color(0xFF0C4869)
private val AzulTexto = Color(0xFF14334D)
private val Verde = Color(0xFF008752)
private val VerdeClaro = Color(0xFFDDF5E9)
private val RojoClaro = Color(0xFFFFE5E9)
private val GrisTexto = Color(0xFF55708E)
private val GrisBorde = Color(0xFFDCE5ED)
private val FondoPantalla = Color(0xFFF8FAFC)

@Composable
fun SeleccionSucursalScreen(
    viewModel: SeleccionSucursalViewModel,
    onContinuar: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPantalla)
    ) {

        // Encabezado azul oscuro
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulOscuro)
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 30.dp,
                    bottom = 24.dp
                )
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "‹",
                    color = Color.White,
                    fontSize = 38.sp
                )

                Spacer(Modifier.width(18.dp))

                Text(
                    text = "Tu ubicación",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "•••",
                    color = Color.White,
                    fontSize = 22.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "⌖",
                    color = Color(0xFF9AC7DE),
                    fontSize = 24.sp
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = uiState.sucursalSeleccionada?.nombre
                        ?: "Sin ubicación seleccionada",
                    color = Color(0xFFBCD5E4),
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = Color(0xFF17435C),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Operador",
                        color = Color.White,
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 7.dp
                        )
                    )
                }
            }
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Text(
                text = "¿Dónde vas a trabajar?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AzulOscuro
            )

            Text(
                text = "El stock y los movimientos corresponden a la ubicación que elijas.",
                fontSize = 16.sp,
                color = GrisTexto,
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(10.dp))

            if (uiState.cargando) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AzulBoton
                    )
                }

            } else {

                uiState.sucursales.forEach { sucursal ->

                    TarjetaSucursal(
                        sucursal = sucursal,
                        seleccionada =
                            uiState.sucursalSeleccionada?.id == sucursal.id,
                        onSeleccionar = {
                            viewModel.seleccionarSucursal(sucursal)
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.error != null
            ) {
                Text(
                    text = uiState.error.orEmpty(),
                    color = Color(0xFFB3261E),
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.confirmarSucursal()
                },
                enabled = !uiState.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulBoton,
                    contentColor = Color.White
                )
            ) {

                Text(
                    text = uiState.sucursalSeleccionada?.let {
                        "Continuar en ${
                            it.nombre.removePrefix("Tienda ")
                                .removePrefix("Bodega ")
                        }"
                    } ?: "Seleccionar ubicación",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(
                visible = uiState.confirmada
            ) {

                Surface(
                    color = VerdeClaro,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "✓ Ubicación confirmada correctamente",
                        modifier = Modifier.padding(16.dp),
                        color = Verde,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Sesión: OPR-017 · Operador",
                color = GrisTexto,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            HorizontalDivider(color = GrisBorde)

            Text(
                text = "Trabaja en la ubicación correcta",
                color = AzulTexto,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Text(
                text = "Puedes cambiar de ubicación desde Inicio.",
                color = GrisTexto,
                fontSize = 14.sp
            )
        }
    }

    LaunchedEffect(
        uiState.confirmada,
        uiState.sucursalSeleccionada?.id
    ) {
        if (uiState.confirmada) {
            uiState.sucursalSeleccionada?.let {
                onContinuar(it.id)
            }
        }
    }
}

@Composable
fun TarjetaSucursal(
    sucursal: SucursalOperador,
    seleccionada: Boolean,
    onSeleccionar: () -> Unit
) {

    val habilitada = sucursal.activa && sucursal.asignada

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = habilitada,
                onClick = onSeleccionar
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habilitada) {
                Color.White
            } else {
                Color(0xFFF1F5F8)
            }
        ),
        border = BorderStroke(
            width = if (seleccionada) 2.dp else 1.dp,
            color = if (seleccionada) Verde else GrisBorde
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8F2F7)),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = when (sucursal.id) {
                        "SUC-01" -> Icons.Outlined.Storefront
                        "SUC-02" -> Icons.Outlined.Warehouse
                        else -> Icons.Outlined.Store
                    },
                    contentDescription = sucursal.nombre,
                    tint = AzulBoton,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = sucursal.nombre,
                    color = AzulOscuro,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "${sucursal.id} · ${sucursal.direccion}",
                    color = GrisTexto,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(12.dp))

                val estado = when {
                    !sucursal.activa ->
                        "Inactiva · No disponible"

                    !sucursal.asignada ->
                        "Sin autorización"

                    seleccionada ->
                        "Seleccionada · Activa"

                    else -> "Activa"
                }

                Surface(
                    color = if (habilitada) VerdeClaro else RojoClaro,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = estado,
                        color = if (habilitada) Verde
                        else Color(0xFFD21E40),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = when {
                    !habilitada -> Icons.Outlined.Lock
                    seleccionada -> Icons.Filled.CheckCircle
                    else -> Icons.Outlined.Store
                },
                contentDescription = when {
                    !habilitada -> "Ubicación no disponible"
                    seleccionada -> "Ubicación seleccionada"
                    else -> "Ubicación disponible"
                },
                tint = if (seleccionada) Verde else GrisTexto,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}