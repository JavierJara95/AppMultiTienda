package com.duoc.appmultitienda.operador.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
// COLORES MULTITIENDA
// =====================================================

private val AzulPerfil = Color(0xFF061D30)
private val AzulBotonPerfil = Color(0xFF0C4869)
private val GrisPerfil = Color(0xFF55708E)
private val FondoPerfil = Color(0xFFF8FAFC)
private val VerdePerfil = Color(0xFF008752)

// =====================================================
// MODELO TEMPORAL DEL PERFIL
// =====================================================

data class PerfilOperadorDemo(

    val id: String,

    val nombre: String,

    val rol: String,

    val correo: String,

    val estado: String
)

// =====================================================
// DATOS DE DEMOSTRACIÓN
// =====================================================

object PerfilOperadorDemoRepository {

    fun obtenerPerfil(): PerfilOperadorDemo {

        return PerfilOperadorDemo(

            id = "OPR-017",

            nombre = "Operador MultiTienda",

            rol = "OPERADOR",

            correo = "operador@multitienda.demo",

            estado = "ACTIVO"
        )
    }
}

// =====================================================
// PANTALLA DEL PERFIL
// =====================================================

@Composable
fun PerfilOperadorScreen(

    nombreSucursal: String,

    perfil: PerfilOperadorDemo,

    onVolver: () -> Unit,

    onCambiarSucursal: () -> Unit,

    onAbrirHistorial: () -> Unit

) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPerfil)
    ) {

        // =============================================
        // ENCABEZADO
        // =============================================

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulPerfil)
                .padding(20.dp)
        ) {

            TextButton(
                onClick = onVolver
            ) {

                Text(
                    text = "‹ Volver",
                    color = Color.White
                )
            }

            Text(
                text = "Mi perfil",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Información del operador",
                color = Color(0xFFBCD5E4),
                fontSize = 14.sp
            )
        }

        // =============================================
        // CONTENIDO
        // =============================================

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // =========================================
            // TARJETA PRINCIPAL
            // =========================================

            Card(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(20.dp),

                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),

                border = BorderStroke(
                    1.dp,
                    Color(0xFFDCE5ED)
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),

                    horizontalAlignment = Alignment.CenterHorizontally,

                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Surface(
                        modifier = Modifier.size(84.dp),

                        shape = CircleShape,

                        color = Color(0xFFE4F0F6)
                    ) {

                        Box(
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = "OP",
                                color = AzulBotonPerfil,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = perfil.nombre,
                        color = AzulPerfil,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "ID: ${perfil.id}",
                        color = GrisPerfil,
                        fontSize = 13.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(50.dp),

                        color = Color(0xFFDDF5E9)
                    ) {

                        Text(
                            text = perfil.estado,
                            color = VerdePerfil,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                        )
                    }
                }
            }

            // =========================================
            // INFORMACIÓN DEL OPERADOR
            // =========================================

            Text(
                text = "Datos del operador",
                color = AzulPerfil,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(18.dp),

                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),

                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    DatoPerfil(
                        titulo = "Código de operador",
                        valor = perfil.id
                    )

                    HorizontalDivider()

                    DatoPerfil(
                        titulo = "Nombre",
                        valor = perfil.nombre
                    )

                    HorizontalDivider()

                    DatoPerfil(
                        titulo = "Rol asignado",
                        valor = perfil.rol
                    )

                    HorizontalDivider()

                    DatoPerfil(
                        titulo = "Correo electrónico",
                        valor = perfil.correo
                    )

                    HorizontalDivider()

                    DatoPerfil(
                        titulo = "Estado",
                        valor = perfil.estado
                    )
                }
            }

            // =========================================
            // SUCURSAL ACTIVA
            // =========================================

            Text(
                text = "Ubicación de trabajo",
                color = AzulPerfil,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(18.dp),

                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),

                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "Sucursal seleccionada",
                        color = GrisPerfil,
                        fontSize = 13.sp
                    )

                    Text(
                        text = nombreSucursal,
                        color = AzulPerfil,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = onCambiarSucursal,

                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(14.dp)
                    ) {

                        Text(
                            text = "Cambiar sucursal"
                        )
                    }
                }
            }

            // =========================================
            // ACCESOS
            // =========================================

            Text(
                text = "Mis actividades",
                color = AzulPerfil,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onAbrirHistorial,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),

                shape = RoundedCornerShape(16.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulBotonPerfil
                )
            ) {

                Text(
                    text = "Ver historial de movimientos",
                    fontWeight = FontWeight.Bold
                )
            }

            // =========================================
            // INFORMACIÓN DE DEMOSTRACIÓN
            // =========================================

            HorizontalDivider()

            Text(
                text = "Perfil de demostración. " +
                        "Los datos no corresponden a una " +
                        "sesión autenticada real.",

                color = GrisPerfil,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

// =====================================================
// COMPONENTE REUTILIZABLE
// =====================================================

@Composable
private fun DatoPerfil(

    titulo: String,

    valor: String

) {

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        Text(
            text = titulo,
            color = GrisPerfil,
            fontSize = 12.sp
        )

        Text(
            text = valor,
            color = AzulPerfil,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}