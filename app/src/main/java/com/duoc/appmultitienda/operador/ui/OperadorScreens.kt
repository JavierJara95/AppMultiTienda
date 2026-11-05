package com.duoc.appmultitienda.operador.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.duoc.appmultitienda.core.model.*
import com.duoc.appmultitienda.core.ui.*
import com.duoc.appmultitienda.operador.viewmodel.*

@Composable
fun UbicacionScreen(d: Datos, onElegir: (String) -> Unit) {
    var elegida by rememberSaveable(d.usuario?.id) { mutableStateOf(d.tiendaId) }
    LaunchedEffect(d.tiendaId) { if (d.tiendaId != null) elegida = d.tiendaId }
    Pagina {
        Text("¿Dónde vas a trabajar?", style = MaterialTheme.typography.headlineSmall)
        Text("El stock y los movimientos corresponden a la ubicación que elijas.", color = Muted)
        d.snapshot.sucursales.forEach { t ->
            val available = t.activa && t.permitida
            val selected = elegida == t.id && available
            Surface(
                onClick = { if (available) elegida = t.id },
                enabled = available && !d.cargando,
                shape = RoundedCornerShape(18.dp),
                color = if (available) Color.White else Color(0xFFF1F5F8),
                border = BorderStroke(1.dp, if (selected) Exito else Borde),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(15.dp).heightIn(min = 70.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        if (t.id == "SUC-02") Icons.Default.Warehouse else Icons.Default.Storefront,
                        null,
                        tint = Petroleo,
                        modifier = Modifier.size(30.dp),
                    )
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(t.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${t.id} · ${t.direccion}",
                            color = Muted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Insignia(
                            when {
                                !t.activa -> "Inactiva · No disponible"
                                !t.permitida -> "No asignada a tu cuenta"
                                selected -> "Seleccionada · Activa"
                                else -> "Activa"
                            },
                            if (!available) "error" else "exito",
                        )
                    }
                    if (selected) Icon(Icons.Default.CheckCircle, "Seleccionada", tint = Exito)
                    else if (!available) Icon(Icons.Default.Lock, "No disponible", tint = Muted)
                }
            }
        }
        Principal(
            "Continuar${d.snapshot.sucursales.firstOrNull { it.id==elegida }?.nombre?.let { " en ${it.removePrefix("Tienda ")}" }.orEmpty()}",
            enabled = d.activas.any { it.id == elegida } && !d.cargando,
        ) {
            elegida?.let(onElegir)
        }
        Text(
            "Sesión: ${d.usuario?.id} · ${d.usuario?.rol?.rolVisible()}",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
        AvisoBox("Trabaja en la ubicación correcta", "Puedes cambiar de ubicación desde Inicio.")
    }
}

@Composable
private fun AccionInicio(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(17.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Borde),
        modifier = modifier.heightIn(min = 80.dp),
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(color = tint.copy(alpha = .12f), shape = RoundedCornerShape(10.dp)) {
                Icon(icon, null, tint = tint, modifier = Modifier.padding(8.dp).size(22.dp))
            }
            Text(title, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun InicioOperadorScreen(
    d: Datos,
    onAccion: (String) -> Unit,
    onUbicacion: () -> Unit,
    onPendientes: () -> Unit,
) {
    Pagina(oscuro = true) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF10435C),
            border = BorderStroke(1.dp, Color(0xFF285770)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(19.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Inventario de la tienda",
                    color = Color(0xFFD8EFF9),
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            d.existenciasTienda.size.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                        )
                        Text(
                            "Productos activos",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD0E8F7),
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            d.existenciasTienda.count { it.cantidad <= it.minimo }.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                        )
                        Text(
                            "Alertas de stock",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD0E8F7),
                        )
                    }
                }
                Text(
                    "Saldo oficial consultado · ${fechaUtc(d.snapshot.actualizadoUtc)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFADC8DA),
                )
            }
        }
        Text(
            "Registrar operación",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AccionInicio(
                "Escanear\nproducto",
                Icons.Default.QrCodeScanner,
                Azul,
                Modifier.weight(1f),
            ) {
                onAccion("producto")
            }
            AccionInicio(
                "Conteo\nfísico",
                Icons.Default.Assignment,
                Color(0xFFA04DF3),
                Modifier.weight(1f),
            ) {
                onAccion("conteo")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AccionInicio(
                "Entrada\nde stock",
                Icons.Default.MoveToInbox,
                Exito,
                Modifier.weight(1f),
            ) {
                onAccion("ENTRADA")
            }
            AccionInicio("Salida\nde stock", Icons.Default.Outbox, Error, Modifier.weight(1f)) {
                onAccion("SALIDA")
            }
        }
        val pendientes =
            d.snapshot.conteos.count { it.tiendaId == d.tiendaId && it.estado == "PENDIENTE" }
        Tarjeta {
            Text("Tus conteos", style = MaterialTheme.typography.titleMedium)
            Text("$pendientes pendientes de revisión", color = Muted)
            Text(
                "El encargado resolverá las diferencias.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
            TextButton(onClick = onPendientes) { Text("Ver conteos") }
        }
        Secundario("Cambiar ubicación", icon = Icons.Default.Sync, onClick = onUbicacion)
        Text(
            "${d.usuario?.id} · Sesión de demostración",
            color = Color(0xFFA9C5D6),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun ManualScreen(
    s: OperadorUi,
    denegada: Boolean,
    onCode: (String) -> Unit,
    onBuscar: () -> Unit,
    onScanner: () -> Unit,
) {
    val ctx = LocalContext.current
    Pagina {
        Text("Busca un producto", style = MaterialTheme.typography.headlineSmall)
        Text("Escribe el código de barras o la referencia interna del catálogo.", color = Muted)
        Campo(
            "Código de producto",
            s.codigo,
            onCode,
            error = s.errorBusqueda,
            helper = "Código de barras o referencia, por ejemplo CAF-001",
            enabled = !s.buscando,
            leading = Icons.Default.QrCodeScanner,
        )
        if (denegada) {
            AvisoBox(
                "Sin acceso a la cámara",
                "El permiso fue denegado. Ingresa el código manualmente o habilita la cámara en los ajustes de Android.",
                "aviso",
            )
            Secundario("Abrir ajustes de Android") {
                ctx.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${ctx.packageName}"),
                    )
                )
            }
        }
        Principal("Buscar producto", busy = s.buscando, onClick = onBuscar)
        AnimatedVisibility(s.errorBusqueda != null) {
            AvisoBox(
                "Producto no encontrado o consulta no disponible",
                s.errorBusqueda.orEmpty(),
                "error",
            )
        }
        Text(
            "Ingreso manual disponible sin usar la cámara.",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        // Teclado táctil del mockup; el campo también admite teclado físico y SKU alfanumérico.
        listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "⌫"),
            )
            .forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fila.forEach { digit ->
                        if (digit.isBlank()) Spacer(Modifier.weight(1f))
                        else
                            OutlinedButton(
                                onClick = {
                                    onCode(
                                        if (digit == "⌫") s.codigo.dropLast(1) else s.codigo + digit
                                    )
                                },
                                enabled = !s.buscando,
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                shape = RoundedCornerShape(11.dp),
                                border = BorderStroke(1.dp, Borde),
                                colors =
                                    ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.White,
                                        contentColor = Tinta,
                                    ),
                            ) {
                                Text(digit, style = MaterialTheme.typography.titleLarge)
                            }
                    }
                }
            }
        Secundario("Volver al escáner", onClick = onScanner)
    }
}

@Composable
fun ProductoScreen(
    e: Existencia?,
    d: Datos,
    movimiento: () -> Unit,
    conteo: () -> Unit,
    historial: () -> Unit,
) {
    if (e == null) {
        Pagina {
            Vacio(
                "Selecciona un producto",
                "Escanea o busca un código de la ubicación seleccionada.",
            )
        }
        return
    }
    Pagina {
        ProductoMini(e)
        StockCard(e, utc = d.snapshot.actualizadoUtc)
        Text("Operaciones", style = MaterialTheme.typography.titleMedium)
        Principal("Realizar conteo físico", icon = Icons.Default.QrCodeScanner, onClick = conteo)
        Secundario(
            "Registrar entrada o salida",
            icon = Icons.Default.MoveToInbox,
            onClick = movimiento,
        )
        Tarjeta {
            Text("Datos del producto", style = MaterialTheme.typography.titleMedium)
            Linea("Unidad", "Unidad (${e.producto.unidad})")
            Linea("Ubicación interna", e.producto.ubicacion)
            Linea("Stock mínimo", "${e.minimo} uds")
        }
        Text(
            "El stock es independiente por sucursal. Los conteos requieren aprobación para generar un ajuste.",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        Secundario("Ver historial del producto", icon = Icons.Default.History, onClick = historial)
    }
}

@Composable
fun MovimientoScreen(f: MovimientoForm, editar: (String, String) -> Unit, revisar: () -> Unit) {
    val e = f.base ?: return
    Pagina {
        ProductoMini(e, conSaldo = true)
        Segmentos(
            listOf("ENTRADA" to "Entrada", "SALIDA" to "Salida"),
            f.tipo,
            enabled = !f.ocupado && !f.bloqueado,
        ) {
            editar("tipo", it)
        }
        Cantidad(
            "Cantidad de ${if(f.tipo=="ENTRADA")"entrada" else "salida"}",
            f.cantidad,
            { editar("cantidad", it) },
            f.errores["cantidad"],
            enabled = !f.ocupado && !f.bloqueado,
            helper = "Unidades enteras mayores que 0.",
        )
        Campo(
            "Motivo del movimiento",
            f.motivo,
            { editar("motivo", it) },
            error = f.errores["motivo"],
            helper = "Entre 5 y 200 caracteres.",
            enabled = !f.ocupado && !f.bloqueado,
        )
        Campo(
            "Referencia (opcional)",
            f.referencia,
            { editar("referencia", it) },
            error = f.errores["referencia"],
            enabled = !f.ocupado && !f.bloqueado,
        )
        f.mensaje?.let {
            AvisoBox(
                if (f.bloqueado) "Confirmación pendiente" else "No se pudo registrar",
                it,
                "error",
            )
        }
        if (f.errores["cantidad"] != null)
            AvisoBox("No se puede registrar la operación", f.errores["cantidad"].orEmpty(), "error")
        else
            f.cantidad.toIntOrNull()?.let { q ->
                val next =
                    e.cantidad.toLong() + (if (f.tipo == "ENTRADA") q.toLong() else -q.toLong())
                AvisoBox(
                    "Revisa antes de registrar",
                    "${if(f.tipo=="ENTRADA")"Entrada" else "Salida"} de $q uds: saldo previsto ${e.cantidad} → $next. Todavía no se ha guardado.",
                )
            }
        Principal(
            if (f.bloqueado) "Revisar solicitud pendiente" else "Revisar movimiento",
            enabled = f.errores.isEmpty(),
            busy = f.ocupado,
            onClick = revisar,
        )
    }
}

@Composable
fun RevisionMovimientoScreen(
    f: MovimientoForm,
    d: Datos,
    confirmar: () -> Unit,
    volver: () -> Unit,
) {
    val e = f.base ?: return
    val q = f.cantidad.toIntOrNull() ?: 0
    val delta = if (f.tipo == "ENTRADA") q else -q
    Pagina {
        ProductoMini(e)
        AvisoBox(
            if (f.bloqueado) "Solicitud pendiente de confirmar" else "Borrador · No guardado",
            if (f.bloqueado) "El reintento recuperará la misma solicitud y su comprobante."
            else "Revisa los datos antes de confirmar la operación.",
        )
        Tarjeta {
            Text(
                "¿Confirmar ${if(f.tipo=="ENTRADA")"entrada" else "salida"}?",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text("${e.producto.nombre} · ${d.tienda?.nombre}", color = Muted)
            Linea("Cantidad", "${delta.conSigno()} uds", if (delta >= 0) Exito else Error)
            Linea("Saldo anterior / final", "${e.cantidad} / ${e.cantidad.toLong()+delta} uds")
            Linea("Motivo", f.motivo)
            Linea("Referencia", f.referencia.ifBlank { "Sin referencia" })
            Linea("Actor", d.usuario?.id.orEmpty())
        }
        f.mensaje?.let { AvisoBox("No se recibió confirmación", it, "error") }
        Principal(
            if (f.bloqueado) "Reintentar confirmación"
            else "Confirmar ${if(f.tipo=="ENTRADA")"entrada" else "salida"}",
            color = Exito,
            icon = Icons.Default.Check,
            busy = f.ocupado,
            onClick = confirmar,
        )
        Secundario(
            if (f.bloqueado) "Volver a la solicitud" else "Volver a editar",
            enabled = !f.ocupado,
            onClick = volver,
        )
        Text(
            "Se validará el saldo antes de guardar.",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun ConteoScreen(f: ConteoForm, editar: (String, String) -> Unit, enviar: () -> Unit) {
    val e = f.base ?: return
    Pagina {
        ProductoMini(e, conSaldo = true)
        Cantidad(
            "Unidades contadas",
            f.cantidad,
            { editar("cantidad", it) },
            f.errores["cantidad"],
            enabled = !f.ocupado && !f.bloqueado,
        )
        if (f.errores["cantidad"] != null)
            AvisoBox(
                "Revisa la cantidad",
                "0 es válido si no hay unidades físicas. La comparación se mostrará con un valor válido.",
                "aviso",
            )
        else
            f.cantidad
                .toIntOrNull()
                ?.takeIf { it >= 0 }
                ?.let { q ->
                    Surface(
                        color = Color(0xFFFFF3D7),
                        shape = RoundedCornerShape(17.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                "Diferencia detectada",
                                style = MaterialTheme.typography.labelLarge,
                                color = Aviso,
                            )
                            Text(
                                "${(q-e.cantidad).conSigno()} uds",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Aviso,
                            )
                            Text(
                                "${e.cantidad} esperadas · $q contadas",
                                color = Aviso,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
        Campo(
            "Observación",
            f.observacion,
            { editar("observacion", it) },
            error = f.errores["observacion"],
            multiline = true,
            enabled = !f.ocupado && !f.bloqueado,
        )
        when {
            f.ocupado ->
                AvisoBox("Enviando conteo…", "Esperando confirmación. El stock sigue sin cambios.")
            f.mensaje != null -> AvisoBox("No se recibió confirmación", f.mensaje, "error")
            else ->
                AvisoBox(
                    "Contar no modifica el stock",
                    "El saldo seguirá en ${e.cantidad} uds hasta que el encargado autorice el ajuste.",
                )
        }
        Principal(
            if (f.bloqueado) "Reintentar envío" else "Enviar conteo a revisión",
            enabled = f.errores.isEmpty(),
            busy = f.ocupado,
            onClick = enviar,
        )
    }
}

@Composable
fun ComprobanteScreen(
    m: Movimiento?,
    base: Existencia?,
    inicio: () -> Unit,
    historial: () -> Unit,
) {
    if (m == null || base == null) return
    Pagina {
        AvisoBox(
            "${if(m.tipo=="ENTRADA")"Entrada" else "Salida"} registrada",
            "Operación confirmada y guardada localmente.",
            "exito",
        )
        ProductoMini(base)
        StockCard(base.copy(cantidad = m.nuevoSaldo), "Stock oficial actualizado", m.instanteUtc)
        Tarjeta {
            Linea(m.tipo, m.id)
            Linea("Cantidad", "${m.cantidad.conSigno()} uds")
            Linea("Saldo anterior / final", "${m.previo} / ${m.nuevoSaldo} uds")
        }
        Text(
            "Motivo: ${m.motivo}\nReferencia: ${m.referencia.ifBlank{"Sin referencia"}}\nActor: ${m.actorId}\n${fechaUtc(m.instanteUtc)}",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        Principal("Volver a Inicio", onClick = inicio)
        Secundario("Ver en historial", onClick = historial)
    }
}

@Composable
fun ConteoRecibidoScreen(c: Conteo?, base: Existencia?, producto: () -> Unit) {
    if (c == null || base == null) return
    Pagina {
        AvisoBox(
            "Conteo recibido · ${c.id}",
            if (c.estado == "CONCILIADO") "El conteo coincide con el saldo. No requiere ajuste."
            else "Pendiente de decisión del encargado. No se realizó ningún ajuste.",
            "exito",
        )
        ProductoMini(base)
        StockCard(base, "Stock oficial sin cambios", c.instanteUtc)
        Tarjeta {
            Linea("Conteo físico", "${c.contado} uds")
            Linea("Diferencia propuesta", "${c.diferencia.conSigno()} uds")
        }
        Insignia(
            if (c.estado == "CONCILIADO") "Conciliado" else "Pendiente de aprobación",
            if (c.estado == "CONCILIADO") "exito" else "aviso",
        )
        Text(
            "“${c.observacion}”\n${c.tiendaId} · ${c.actorId}\n${fechaUtc(c.instanteUtc)}",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        Principal("Volver al producto", onClick = producto)
    }
}
