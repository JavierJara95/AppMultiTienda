package com.duoc.appmultitienda.core.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.appmultitienda.R
import com.duoc.appmultitienda.core.model.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

fun fechaUtc(value: String): String =
    runCatching {
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, HH:mm:ss 'UTC'",
                    Locale.forLanguageTag("es-CL"),
                )
                .withZone(ZoneOffset.UTC)
                .format(Instant.parse(value))
        }
        .getOrDefault(value)

@Composable
fun FondoMarino(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier
            .clipToBounds()
            .background(Brush.verticalGradient(listOf(Marino, Color(0xFF0B3C50))))
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                Color(0xFF063349).copy(alpha = .55f),
                radius = size.width * .48f,
                center = androidx.compose.ui.geometry.Offset(size.width * 1.04f, size.height * .08f),
            )
        }
        content()
    }
}

@Composable
fun Encabezado(
    title: String,
    subtitle: String,
    role: String,
    onBack: (() -> Unit)?,
    onMenu: () -> Unit,
) {
    FondoMarino(Modifier.fillMaxWidth()) {
        Column(
            Modifier.statusBarsPadding()
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null)
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                else
                    Box(
                        Modifier.padding(8.dp)
                            .size(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Petroleo),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Inventory2,
                            null,
                            tint = Color(0xFF1DD4C4),
                            modifier = Modifier.size(21.dp),
                        )
                    }
                Text(
                    title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onMenu) {
                    Icon(Icons.Default.MoreHoriz, "Opciones de sesión", tint = Color.White)
                }
            }
            Row(
                Modifier.padding(horizontal = 12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    null,
                    tint = Color(0xFF80A8BF),
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    subtitle,
                    color = Color(0xFFB2CAD8),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )
                Text(
                    role.rolVisible(),
                    color = Color(0xFFC9E8F6),
                    fontSize = 10.sp,
                    modifier =
                        Modifier.clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF17435C))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
fun Pagina(
    modifier: Modifier = Modifier,
    oscuro: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val fondo = if (oscuro) Color.Transparent else Fondo
    Box(Modifier.fillMaxSize().background(if (oscuro) Color(0xFF0B3549) else Marino)) {
        Column(
            modifier
                .fillMaxSize()
                .clip(
                    RoundedCornerShape(
                        topStart = if (oscuro) 0.dp else 24.dp,
                        topEnd = if (oscuro) 0.dp else 24.dp,
                    )
                )
                .background(fondo)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
    }
}

@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = color,
        border = BorderStroke(1.dp, Borde),
        shadowElevation = 1.dp,
    ) {
        Column(
            Modifier.padding(14.dp).animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
fun Principal(
    text: String,
    enabled: Boolean = true,
    color: Color = Petroleo,
    icon: ImageVector? = null,
    busy: Boolean = false,
    onClick: () -> Unit,
) {
    Button(
        onClick,
        enabled = enabled && !busy,
        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
        shape = RoundedCornerShape(14.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = color,
                disabledContainerColor = Color(0xFFDEE7EF),
                disabledContentColor = Color(0xFF526E8A),
            ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Tinta)
        else if (icon != null) Icon(icon, null, Modifier.size(19.dp))
        if (icon != null || busy) Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun Secundario(
    text: String,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Borde),
        colors =
            ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Tinta),
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(19.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun AvisoBox(title: String, text: String, tipo: String = "info") {
    val (bg, fg) =
        when (tipo) {
            "error" -> Color(0xFFFFE7ED) to Error
            "exito" -> Color(0xFFE1F8ED) to Exito
            "aviso" -> Color(0xFFFFF3D7) to Aviso
            else -> Color(0xFFE8F3FC) to Color(0xFF155C88)
        }
    val icon =
        when (tipo) {
            "error" -> Icons.Default.ErrorOutline
            "exito" -> Icons.Default.Check
            "aviso" -> Icons.Default.WarningAmber
            else -> Icons.Default.Info
        }
    Surface(
        shape = RoundedCornerShape(15.dp),
        color = bg,
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = fg, style = MaterialTheme.typography.labelLarge)
                Text(text, color = fg, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun Insignia(text: String, tipo: String = "info") {
    val (bg, fg) =
        when (tipo) {
            "exito" -> Color(0xFFDFF8EC) to Exito
            "error" -> Color(0xFFFFE7ED) to Error
            "aviso" -> Color(0xFFFFF1D1) to Aviso
            else -> Color(0xFFE6F1FA) to Muted
        }
    Text(
        text,
        color = fg,
        style = MaterialTheme.typography.labelSmall,
        modifier =
            Modifier.clip(RoundedCornerShape(20.dp))
                .background(bg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
fun Linea(label: String, value: String, color: Color = Tinta) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            color = color,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1.2f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
}

@Composable
fun ProductoMini(e: Existencia, conSaldo: Boolean = false) {
    Tarjeta {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFFEFF4F8)),
                contentAlignment = Alignment.Center,
            ) {
                if (e.producto.sku == "CAF-001")
                    Image(
                        painterResource(R.drawable.cafe_mockup),
                        null,
                        modifier = Modifier.fillMaxSize(),
                    )
                else
                    Icon(
                        if (e.producto.sku == "LEC-004") Icons.Default.LocalDrink
                        else Icons.Default.Inventory2,
                        null,
                        tint = Petroleo,
                        modifier = Modifier.size(29.dp),
                    )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(e.producto.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${e.producto.sku} · ${e.producto.categoria}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                Text(
                    e.producto.codigoBarras,
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
        if (conSaldo) {
            HorizontalDivider(color = Borde)
            Linea("Stock oficial de referencia", "${e.cantidad} uds")
        }
    }
}

@Composable
fun StockCard(e: Existencia, title: String = "Stock oficial", utc: String = "") {
    Tarjeta(color = Color(0xFFF7FAFE)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = Muted)
            val status = estadoStock(e.cantidad, e.minimo)
            Insignia(
                status,
                when (status) {
                    "Quiebre" -> "error"
                    "Stock bajo" -> "aviso"
                    else -> "exito"
                },
            )
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(e.cantidad.toString(), style = MaterialTheme.typography.headlineLarge)
            Text(" uds", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            Spacer(Modifier.weight(1f))
            Text(
                "Mínimo: ${e.minimo} uds",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        if (utc.isNotBlank())
            Text(
                "Saldo consultado · ${fechaUtc(utc)}",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
    }
}

@Composable
fun Campo(
    label: String,
    value: String,
    onValue: (String) -> Unit,
    error: String? = null,
    helper: String? = null,
    enabled: Boolean = true,
    multiline: Boolean = false,
    number: Boolean = false,
    secret: Boolean = false,
    leading: ImageVector? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (error != null) Error else Tinta,
        )
        OutlinedTextField(
            value,
            onValue,
            modifier =
                Modifier.fillMaxWidth().heightIn(min = if (multiline) 88.dp else 52.dp).semantics {
                    contentDescription = label
                },
            enabled = enabled,
            singleLine = !multiline,
            minLines = if (multiline) 2 else 1,
            shape = RoundedCornerShape(13.dp),
            isError = error != null,
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        when {
                            number -> KeyboardType.Number
                            secret -> KeyboardType.Password
                            else -> KeyboardType.Text
                        }
                ),
            visualTransformation =
                if (secret) PasswordVisualTransformation() else VisualTransformation.None,
            leadingIcon = leading?.let { { Icon(it, null, Modifier.size(20.dp)) } },
            trailingIcon =
                if (error != null) {
                    { Icon(Icons.Default.ErrorOutline, "Error en $label", tint = Error) }
                } else null,
            colors =
                OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Borde,
                    focusedBorderColor = Azul,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    disabledContainerColor = Color(0xFFF0F4F8),
                    disabledTextColor = Tinta,
                ),
        )
        if (error != null || helper != null)
            Text(
                error ?: helper.orEmpty(),
                color = if (error != null) Error else Muted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.semantics { if (error != null) this.error(error) },
            )
    }
}

@Composable
fun Cantidad(
    label: String,
    value: String,
    onValue: (String) -> Unit,
    error: String?,
    enabled: Boolean = true,
    helper: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (error != null) Error else Tinta,
        )
        Row(
            Modifier.fillMaxWidth()
                .heightIn(min = 52.dp)
                .border(1.dp, if (error != null) Error else Borde, RoundedCornerShape(13.dp))
                .clip(RoundedCornerShape(13.dp))
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    val n = value.toIntOrNull() ?: 0
                    onValue((n.toLong() - 1).coerceAtLeast(0).toString())
                },
                enabled = enabled,
            ) {
                Icon(Icons.Default.Remove, "Disminuir cantidad", tint = Azul)
            }
            OutlinedTextField(
                value,
                onValue,
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.weight(1f).semantics { contentDescription = label },
                textStyle =
                    MaterialTheme.typography.titleLarge.copy(
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = if (error != null) Error else Tinta,
                    ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Azul,
                        disabledBorderColor = Color.Transparent,
                        disabledTextColor = Tinta,
                    ),
            )
            IconButton(
                onClick = {
                    val n = value.toIntOrNull() ?: 0
                    onValue((n.toLong() + 1).coerceAtMost(Int.MAX_VALUE.toLong()).toString())
                },
                enabled = enabled,
            ) {
                Icon(Icons.Default.Add, "Aumentar cantidad", tint = Azul)
            }
        }
        if (error != null || helper != null)
            Text(
                error ?: helper.orEmpty(),
                color = if (error != null) Error else Muted,
                style = MaterialTheme.typography.bodySmall,
            )
    }
}

@Composable
fun Selector(
    label: String,
    actual: String,
    opciones: List<Pair<String, String>>,
    enabled: Boolean = true,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        if (label.isNotBlank()) Text(label, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(13.dp),
                border = BorderStroke(1.dp, Borde),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Tinta,
                    ),
            ) {
                Text(
                    opciones.firstOrNull { it.first == actual }?.second ?: "Seleccionar",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Icon(Icons.Default.ExpandMore, null, Modifier.size(20.dp), tint = Muted)
            }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                opciones.forEach { (id, nombre) ->
                    DropdownMenuItem(
                        text = { Text(nombre) },
                        onClick = {
                            expanded = false
                            onSelect(id)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun Segmentos(
    opciones: List<Pair<String, String>>,
    actual: String,
    enabled: Boolean = true,
    onSelect: (String) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFE5EFF5))
            .padding(2.dp)
    ) {
        opciones.forEach { (id, texto) ->
            TextButton(
                onClick = { onSelect(id) },
                enabled = enabled,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                shape = RoundedCornerShape(22.dp),
                colors =
                    ButtonDefaults.textButtonColors(
                        containerColor = if (actual == id) Petroleo else Color.Transparent,
                        contentColor = if (actual == id) Color.White else Muted,
                    ),
                contentPadding = PaddingValues(horizontal = 3.dp, vertical = 8.dp),
            ) {
                Text(
                    texto,
                    fontSize = 11.sp,
                    fontWeight = if (actual == id) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
fun Vacio(title: String, texto: String) {
    AvisoBox(title, texto)
}
