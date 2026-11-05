package com.duoc.appmultitienda.gestion.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.duoc.appmultitienda.core.ui.*
import com.duoc.appmultitienda.gestion.viewmodel.AccesoUi

@Composable
fun LogoMarca() {
    Image(androidx.compose.ui.res.painterResource(com.duoc.appmultitienda.R.drawable.logo_mockup), contentDescription = "MultiTienda", modifier = Modifier.size(132.dp))
}

@Composable
fun LoginScreen(s: AccesoUi, onEditar: (String?, String?, Boolean?) -> Unit, onLogin: () -> Unit) {
    var visible by rememberSaveable { mutableStateOf(false) }
    var demoMenu by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
    FondoMarino(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            LogoMarca()
            Text(
                buildAnnotatedString {
                    withStyle(androidx.compose.ui.text.SpanStyle(color = Color.White)) {
                        append("Multi")
                    }
                    withStyle(androidx.compose.ui.text.SpanStyle(color = Celeste)) {
                        append("Tienda")
                    }
                },
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                "Tu inventario, en cada ubicación.",
                color = Color(0xFFBAD0DE),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(9.dp))
            if (s.mensaje != null) AvisoBox("No pudimos iniciar sesión", s.mensaje, "error")
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Inicia sesión",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                )
                Text(
                    "Ingresa con tu cuenta de la empresa.",
                    color = Color(0xFFBBD0DF),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            val colors =
                OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFF28566D),
                    focusedBorderColor = Celeste,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedContainerColor = Color(0xFF082C42),
                    focusedContainerColor = Color(0xFF082C42),
                    cursorColor = Celeste,
                    disabledTextColor = Color(0xFFBBD0DF),
                )
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    "Usuario",
                    color = Color(0xFFD3E3EF),
                    style = MaterialTheme.typography.labelLarge,
                )
                OutlinedTextField(
                    s.usuario,
                    { onEditar(it, null, null) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !s.ocupado,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(Icons.Default.PersonOutline, null, tint = Color(0xFFB6D0DF))
                    },
                    isError = s.errores["usuario"] != null,
                    colors = colors,
                    textStyle = MaterialTheme.typography.bodyMedium,
                )
                s.errores["usuario"]?.let {
                    Text(it, color = Color(0xFFFFB6C7), style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    "Contraseña",
                    color = Color(0xFFD3E3EF),
                    style = MaterialTheme.typography.labelLarge,
                )
                OutlinedTextField(
                    s.clave,
                    { onEditar(null, it, null) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !s.ocupado,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation =
                        if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFFB6D0DF)) },
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                if (visible) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                if (visible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = Color(0xFFB6D0DF),
                            )
                        }
                    },
                    isError = s.errores["clave"] != null,
                    colors = colors,
                    textStyle = MaterialTheme.typography.bodyMedium,
                )
                s.errores["clave"]?.let {
                    Text(it, color = Color(0xFFFFB6C7), style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    s.recordar,
                    { onEditar(null, null, it) },
                    enabled = !s.ocupado,
                    colors =
                        CheckboxDefaults.colors(
                            checkedColor = Color(0xFFCAE6F7),
                            checkmarkColor = Marino,
                            uncheckedColor = Color(0xFFBBD0DF),
                        ),
                )
                Text(
                    "Recordar usuario en este dispositivo",
                    color = Color(0xFFBBD0DF),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Principal(
                if (s.mensaje == null) "Iniciar sesión" else "Intentar de nuevo",
                color = Color(0xFF006DDD),
                icon = Icons.AutoMirrored.Filled.Login,
                busy = s.ocupado,
                onClick = onLogin,
            )
            Box(Modifier.fillMaxWidth()) {
                Surface(
                    onClick = { demoMenu = true },
                    enabled = !s.ocupado,
                    color = Color(0xFF0A3349),
                    shape = RoundedCornerShape(17.dp),
                    border = BorderStroke(1.dp, Color(0xFF1B4D64)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Default.Storefront, null, tint = Celeste)
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                "Cuenta de demostración",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(
                                "OPR-017 · Operador",
                                color = Color(0xFFC4DCEB),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                "Acceso ficticio para esta propuesta.",
                                color = Color(0xFF9FC1D4),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Icon(
                            Icons.Default.ExpandMore,
                            "Elegir cuenta de prueba",
                            tint = Color(0xFFBBD0DF),
                        )
                    }
                }
                DropdownMenu(demoMenu, onDismissRequest = { demoMenu = false }) {
                    listOf(
                            "op.alameda" to "OPR-017 · Operador",
                            "enc.alameda" to "ENC-004 · Encargado",
                            "gerencia" to "GER-001 · Gerencia (informes)",
                            "admin" to "ADM-001 · Admin (gestión)",
                        )
                        .forEach { (cuenta, etiqueta) ->
                            DropdownMenuItem(
                                text = { Text(etiqueta) },
                                onClick = {
                                    demoMenu = false
                                    onEditar(cuenta, "demo123", null)
                                },
                            )
                        }
                    Text(
                        "Contraseña de prueba: demo123",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }
            Text(
                "¿Necesitas acceso? Contacta a tu administrador.",
                color = Color(0xFF9FBDCF),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "MultiTienda ${com.duoc.appmultitienda.BuildConfig.VERSION_NAME} · Datos sintéticos",
                color = Color(0xFF86A8BD),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
    if (s.ocupado) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xEF041D31)) {
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                LogoMarca()
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(54.dp),
                    color = Celeste,
                    strokeWidth = 4.dp,
                    strokeCap = StrokeCap.Round,
                )
                Spacer(Modifier.height(20.dp))
                Text("Iniciando sesión…", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text("Preparando tu espacio de trabajo", color = Color(0xFFBBD0DF))
            }
        }
    }
}
}
