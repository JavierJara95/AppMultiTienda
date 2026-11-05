package com.duoc.appmultitienda.gestion.ui

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
import androidx.compose.ui.unit.dp
import com.duoc.appmultitienda.core.model.*
import com.duoc.appmultitienda.core.ui.*
import com.duoc.appmultitienda.gestion.viewmodel.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private fun existencia(d: Datos, tienda: String, sku: String) =
    d.snapshot.existencias.firstOrNull { it.tiendaId == tienda && it.producto.sku == sku }

@Composable
fun DiferenciasScreen(d: Datos, revisar: (Conteo, Boolean) -> Unit) {
    var resueltas by rememberSaveable { mutableStateOf(false) }
    val todas = d.snapshot.conteos.filter { it.tiendaId == d.tiendaId }
    val pendientes = todas.filter { it.estado == "PENDIENTE" }
    val lista =
        todas.filter { if (resueltas) it.estado != "PENDIENTE" else it.estado == "PENDIENTE" }
    Pagina {
        Segmentos(
            listOf("PENDIENTES" to "Pendientes (${pendientes.size})", "RESUELTAS" to "Resueltas"),
            if (resueltas) "RESUELTAS" else "PENDIENTES",
        ) {
            resueltas = it == "RESUELTAS"
        }
        Text(
            "${d.usuario?.id} · Responsable de ${d.tienda?.nombre}",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        if (lista.isEmpty())
            Vacio(
                if (resueltas) "No hay conteos resueltos" else "No hay diferencias pendientes",
                "Los conteos recibidos aparecerán aquí.",
            )
        lista.forEach { c ->
            Tarjeta {
                val e = existencia(d, c.tiendaId, c.sku)
                Text(e?.producto?.nombre ?: c.sku, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${c.sku} · ${c.id} · ${d.tienda?.nombre}",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
                HorizontalDivider(color = Borde)
                Linea("Stock oficial al contar / contado", "${c.esperado} / ${c.contado} uds")
                Linea(
                    "Diferencia propuesta",
                    "${c.diferencia.conSigno()} uds",
                    if (c.diferencia < 0) Error else Exito,
                )
                Text(
                    "${c.actorId} · ${fechaUtc(c.instanteUtc)}",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "“${c.observacion}”",
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (c.estado == "PENDIENTE" && d.usuario?.rol == "ENCARGADO") {
                    Principal("Revisar y aprobar ajuste") { revisar(c, true) }
                    Secundario("Rechazar con motivo") { revisar(c, false) }
                } else {
                    Insignia(c.estado, if (c.estado == "RECHAZADO") "error" else "exito")
                    if (c.motivoResolucion.isNotBlank())
                        Text(
                            "${c.motivoResolucion}\n${c.resolutorId} · ${fechaUtc(c.resueltoUtc)}",
                            color = Muted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                }
            }
        }
        Text(
            "Solo el encargado de esta ubicación puede resolver. Cada decisión exige un motivo auditable.",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun ResolverScreen(
    f: ResolucionForm,
    d: Datos,
    onMotivo: (String) -> Unit,
    confirmar: () -> Unit,
    cancelar: () -> Unit,
) {
    val c = f.conteo ?: return
    val e = existencia(d, c.tiendaId, c.sku) ?: return
    val previo = if (f.versionRevisada == c.version) c.esperado else e.cantidad
    Pagina {
        ProductoMini(e)
        Text(
            "${c.id} · ${d.tienda?.nombre}",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        Tarjeta(color = Color(0xFFF7FAFE)) {
            Linea(
                if (f.aprobar) "Stock oficial revisado" else "Stock permanece",
                if (f.aprobar) "$previo → ${c.contado} uds" else "${e.cantidad} uds",
            )
            Linea(
                if (f.aprobar) "Ajuste autorizado" else "Diferencia propuesta",
                "${(if(f.aprobar)c.contado-previo else c.diferencia).conSigno()} uds",
            )
        }
        Campo(
            "Motivo obligatorio",
            f.motivo,
            onMotivo,
            error = f.errorMotivo,
            helper = "${f.motivo.trim().length}/200 · Entre 5 y 200 caracteres.",
            enabled = !f.ocupado && !f.bloqueado,
            multiline = true,
        )
        AvisoBox(
            "Revisión del encargado",
            if (f.aprobar)
                "La aprobación genera un ajuste auditable. El saldo se comprobará de nuevo."
            else "El rechazo conserva el saldo y registra tu motivo e identidad.",
            if (f.aprobar) "info" else "aviso",
        )
        f.mensaje?.let {
            AvisoBox(
                if (f.bloqueado) "Confirmación pendiente" else "Revisa la solicitud",
                it,
                if (f.bloqueado) "aviso" else "info",
            )
        }
        Principal(
            if (f.bloqueado) "Reintentar decisión"
            else if (f.aprobar) "Aprobar y ajustar stock" else "Confirmar rechazo",
            enabled = f.errorMotivo == null,
            busy = f.ocupado,
            color = if (f.aprobar) Exito else Error,
            onClick = confirmar,
        )
        Secundario("Cancelar", enabled = !f.ocupado, onClick = cancelar)
    }
}

@Composable
fun ConflictoScreen(f: ResolucionForm, d: Datos, actualizar: () -> Unit, volver: () -> Unit) {
    val c = f.conteo ?: return
    val vigente = f.conflicto ?: return
    Pagina {
        ProductoMini(vigente)
        AvisoBox(
            "El saldo cambió",
            "Otro movimiento actualizó este producto mientras revisabas ${c.id}.",
            "aviso",
        )
        Tarjeta(color = Color(0xFFF7FAFE)) {
            Linea("Saldo al contar", "${c.esperado} uds")
            Linea("Saldo vigente", "${vigente.cantidad} uds")
            Linea("Unidades contadas", "${c.contado} uds")
            Linea("Propuesta anterior", "${c.diferencia.conSigno()} uds")
        }
        AvisoBox(
            "No se aplicó el ajuste",
            "Revisa la diferencia con el nuevo saldo. No se sobrescribió el stock.",
        )
        f.mensaje?.let { Text(it, color = Muted, style = MaterialTheme.typography.bodySmall) }
        Principal(
            "Actualizar y revisar de nuevo",
            icon = Icons.Default.Sync,
            busy = f.ocupado,
            onClick = actualizar,
        )
        Secundario("Volver a pendientes", enabled = !f.ocupado, onClick = volver)
        Text(
            "Aprobación bloqueada hasta consultar el nuevo saldo.",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun ResolucionRecibidaScreen(r: Resolucion?, historial: () -> Unit, diferencias: () -> Unit) {
    if (r == null) return
    val aprobado = r.conteo.estado == "APROBADO"
    Pagina {
        AvisoBox(
            "${if(aprobado)"Ajuste aprobado" else "Rechazo registrado"} · ${r.evento.id}",
            if (aprobado) "Stock actualizado en el almacenamiento local."
            else "No generó un movimiento de stock.",
            if (aprobado) "exito" else "aviso",
        )
        ProductoMini(r.existencia)
        StockCard(
            r.existencia,
            if (aprobado) "Stock oficial" else "Stock oficial sin cambios",
            r.evento.instanteUtc,
        )
        Tarjeta {
            Insignia(
                "${r.conteo.id} · ${if(aprobado)"Aprobado" else "Rechazado"}",
                if (aprobado) "exito" else "error",
            )
            Text(r.conteo.motivoResolucion)
            Text(
                "${r.evento.id} · ${r.evento.titulo}\n${r.conteo.tiendaId} · Actor: ${r.evento.actorId}\n${fechaUtc(r.evento.instanteUtc)}",
                color = Muted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (aprobado)
            Principal("Ver ajuste en historial", icon = Icons.Default.History, onClick = historial)
        Secundario("Volver a diferencias", onClick = diferencias)
    }
}

@Composable
fun HistorialScreen(
    d: Datos,
    filtro: HistorialFiltro,
    errores: Map<String, String>,
    editar: (HistorialFiltro) -> Unit,
) {
    var detalles by rememberSaveable { mutableStateOf(false) }
    val permisoGlobal = d.usuario?.rol == "ADMIN"
    val eventos =
        d.snapshot.eventos.filter { e ->
            val producto = existencia(d, e.tiendaId, e.sku)?.producto?.nombre.orEmpty()
            val date = e.instanteUtc.take(10)
            errores.isEmpty() &&
                (permisoGlobal || e.tiendaId == d.tiendaId) &&
                (filtro.tipo == "TODOS" || e.tipo == filtro.tipo) &&
                (filtro.sku == "TODOS" || e.sku == filtro.sku) &&
                (filtro.texto.isBlank() ||
                    "${e.id} ${e.sku} $producto ${e.referencia}".contains(filtro.texto, true)) &&
                (filtro.actor.isBlank() || e.actorId.contains(filtro.actor, true)) &&
                (filtro.desde.isBlank() || date >= filtro.desde) &&
                (filtro.hasta.isBlank() || date <= filtro.hasta)
        }
    Pagina {
        Campo(
            "Buscar producto o ID",
            filtro.texto,
            { editar(filtro.copy(texto = it)) },
            leading = Icons.Default.Search,
        )
        Selector(
            "",
            filtro.sku,
            listOf("TODOS" to "Todos los productos") +
                d.snapshot.existencias
                    .map { it.producto }
                    .distinctBy { it.sku }
                    .map { it.sku to it.nombre },
        ) {
            editar(filtro.copy(sku = it))
        }
        Segmentos(
            listOf(
                "TODOS" to "Todos",
                "AJUSTE" to "Ajustes",
                "ENTRADA" to "Entradas",
                "SALIDA" to "Salidas",
            ),
            filtro.tipo,
        ) {
            editar(filtro.copy(tipo = it))
        }
        TextButton(onClick = { detalles = !detalles }) {
            Icon(Icons.Default.FilterList, null)
            Text("Filtros de auditoría")
        }
        AnimatedVisibility(detalles) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Selector(
                    "Tipo de registro",
                    filtro.tipo,
                    listOf(
                        "TODOS" to "Todos",
                        "CONTEO" to "Conteos",
                        "RECHAZO" to "Rechazos",
                        "REPOSICION" to "Reposiciones",
                        "SUCURSAL" to "Sucursales",
                        "AJUSTE" to "Ajustes",
                        "ENTRADA" to "Entradas",
                        "SALIDA" to "Salidas",
                    ),
                ) {
                    editar(filtro.copy(tipo = it))
                }
                Campo(
                    "Actor anonimizado",
                    filtro.actor,
                    { editar(filtro.copy(actor = it)) },
                    helper = "Ejemplo: OPR-017",
                )
                Campo(
                    "Desde UTC (opcional)",
                    filtro.desde,
                    { editar(filtro.copy(desde = it)) },
                    error = errores["desde"],
                    helper = "AAAA-MM-DD",
                )
                Campo(
                    "Hasta UTC (opcional)",
                    filtro.hasta,
                    { editar(filtro.copy(hasta = it)) },
                    error = errores["hasta"],
                    helper = "AAAA-MM-DD",
                )
                Secundario("Limpiar filtros") { editar(HistorialFiltro()) }
            }
        }
        Text(
            "${eventos.size} registros · UTC",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        if (eventos.isEmpty())
            Vacio("Sin registros para estos filtros", "Prueba otro producto, tipo o periodo.")
        eventos.forEach { e ->
            Tarjeta {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        e.titulo,
                        style = MaterialTheme.typography.labelLarge,
                        color =
                            when (e.tipo) {
                                "ENTRADA",
                                "AJUSTE" -> Exito
                                "SALIDA",
                                "RECHAZO" -> Error
                                else -> Petroleo
                            },
                    )
                    Text(e.id, color = Muted, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    existencia(d, e.tiendaId, e.sku)?.producto?.nombre
                        ?: d.snapshot.sucursales
                            .firstOrNull { it.id == e.tiendaId }
                            ?.nombre
                            .orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (e.sku.isNotBlank())
                    Linea(
                        if (e.tipo == "CONTEO") "Diferencia / stock sin cambios"
                        else "Cantidad / saldo final",
                        "${e.cantidad.conSigno()} uds / ${e.nuevoSaldo} uds",
                    )
                Text(
                    "${e.actorId} · ${e.tiendaId} · ${e.referencia}\n${fechaUtc(e.instanteUtc)}",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "Motivo: ${e.motivo}",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
                Insignia(e.resultado, if (e.resultado == "RECHAZADO") "error" else "info")
            }
        }
        Text(
            "Registro inmutable · producto, actor y ubicación",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun AlertasScreen(d: Datos, onReponer: (Existencia) -> Unit, onDiferencias: () -> Unit) {
    var solicitudes by rememberSaveable { mutableStateOf(false) }
    val activas = d.activas.map { it.id }.toSet()
    val stock =
        d.snapshot.existencias.filter {
            it.tiendaId in activas && (d.usuario?.rol == "ADMIN" || it.tiendaId == d.tiendaId)
        }
    val alertas = stock.filter { it.cantidad <= it.minimo }.sortedBy { it.cantidad }
    val reposiciones =
        d.snapshot.reposiciones.filter { d.usuario?.rol == "ADMIN" || it.tiendaId == d.tiendaId }
    val pendientes =
        d.snapshot.conteos.count {
            it.estado == "PENDIENTE" && (d.usuario?.rol == "ADMIN" || it.tiendaId == d.tiendaId)
        }
    Pagina {
        Segmentos(
            listOf(
                "ACTIVAS" to "Activas (${alertas.size})",
                "SOLICITUDES" to "Solicitudes (${reposiciones.size})",
            ),
            if (solicitudes) "SOLICITUDES" else "ACTIVAS",
        ) {
            solicitudes = it == "SOLICITUDES"
        }
        Text(
            "Stock ≤ mínimo: bajo · Stock 0: quiebre",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        if (solicitudes) {
            if (reposiciones.isEmpty())
                Vacio("No hay solicitudes", "Las reposiciones informativas se mostrarán aquí.")
            reposiciones.forEach { r ->
                Tarjeta {
                    Text(
                        existencia(d, r.tiendaId, r.sku)?.producto?.nombre ?: r.sku,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Linea("Solicitud", r.id)
                    Linea("Cantidad", "${r.cantidad} uds")
                    Insignia(r.estado, "aviso")
                    Text(
                        "${r.motivo}\n${r.actorId} · ${fechaUtc(r.instanteUtc)}",
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        } else {
            if (alertas.isEmpty())
                Vacio("Sin alertas de stock", "Los saldos están por encima del mínimo configurado.")
            alertas.forEach { e ->
                Tarjeta {
                    Insignia(
                        estadoStock(e.cantidad, e.minimo),
                        if (e.cantidad == 0) "error" else "aviso",
                    )
                    Text(e.producto.nombre, style = MaterialTheme.typography.titleMedium)
                    Linea(
                        "Stock / mínimo",
                        "${e.cantidad} / ${e.minimo} uds",
                        if (e.cantidad == 0) Error else Aviso,
                    )
                    Text(
                        "${d.snapshot.sucursales.firstOrNull{it.id==e.tiendaId}?.nombre} · ${e.producto.sku}",
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (d.usuario?.rol == "ENCARGADO")
                        Principal("Solicitar reposición", icon = Icons.Default.Send) {
                            onReponer(e)
                        }
                }
            }
            if (pendientes > 0) {
                AvisoBox(
                    "Diferencias pendientes",
                    "$pendientes conteos requieren revisión.",
                    "aviso",
                )
                if (d.usuario?.rol == "ENCARGADO")
                    Secundario("Ver diferencias", onClick = onDiferencias)
            }
        }
        AvisoBox("Solicitud informativa", "No compra productos ni aumenta el stock.")
    }
}

@Composable
fun ReposicionScreen(
    f: ReposicionForm,
    d: Datos,
    editar: (String, String) -> Unit,
    confirmar: () -> Unit,
) {
    val e = f.base ?: return
    Pagina {
        ProductoMini(e)
        StockCard(e, "Stock oficial sin cambios", d.snapshot.actualizadoUtc)
        Cantidad(
            "Cantidad solicitada",
            f.cantidad,
            { editar("cantidad", it) },
            f.errores["cantidad"],
            enabled = !f.ocupado && !f.bloqueado,
        )
        Selector(
            "Destino sugerido",
            f.destinoId,
            d.snapshot.sucursales
                .filter { it.activa && it.id != e.tiendaId }
                .map { it.id to it.nombre },
            enabled = !f.ocupado && !f.bloqueado,
        ) {
            editar("destino", it)
        }
        f.errores["destino"]?.let {
            Text(it, color = Error, style = MaterialTheme.typography.bodySmall)
        }
        Campo(
            "Motivo de reposición",
            f.motivo,
            { editar("motivo", it) },
            f.errores["motivo"],
            helper = "Entre 5 y 200 caracteres.",
            enabled = !f.ocupado && !f.bloqueado,
            multiline = true,
        )
        AvisoBox(
            "Solicitud, no compra",
            "No reserva unidades ni aumenta el inventario. La recepción requiere una entrada.",
        )
        f.mensaje?.let { AvisoBox("No se recibió confirmación", it, "error") }
        Principal(
            if (f.bloqueado) "Reintentar solicitud" else "Enviar solicitud",
            icon = Icons.Default.Send,
            busy = f.ocupado,
            onClick = confirmar,
        )
    }
}

@Composable
fun ReposicionRecibidaScreen(r: Reposicion?, d: Datos, volver: () -> Unit) {
    if (r == null) return
    val e = existencia(d, r.tiendaId, r.sku)
    Pagina {
        AvisoBox(
            "Solicitud enviada · ${r.id}",
            "Registrada localmente. Pendiente de atención.",
            "exito",
        )
        if (e != null) {
            ProductoMini(e)
            StockCard(e, "Stock oficial sin cambios", r.instanteUtc)
        }
        Tarjeta {
            Linea("Cantidad solicitada", "${r.cantidad} uds")
            Linea(
                "Destino sugerido",
                d.snapshot.sucursales.firstOrNull { it.id == r.destinoId }?.nombre ?: r.destinoId,
            )
        }
        Insignia("Pendiente de atención", "aviso")
        Text(
            "${r.motivo}\nSolicitante: ${r.actorId}\n${fechaUtc(r.instanteUtc)}",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        AvisoBox(
            "Solicitud, no compra",
            "No reserva unidades ni aumenta el inventario. La recepción requiere una entrada.",
        )
        Principal("Volver a alertas", onClick = volver)
    }
}

@Composable
fun DashboardScreen(d: Datos, onIr: (String) -> Unit) {
    var ubicacion by rememberSaveable { mutableStateOf("TODAS") }
    val activas = d.activas.map { it.id }.toSet()
    LaunchedEffect(activas) {
        if (ubicacion != "TODAS" && ubicacion !in activas) ubicacion = "TODAS"
    }
    val stock =
        d.snapshot.existencias.filter {
            it.tiendaId in activas && (ubicacion == "TODAS" || it.tiendaId == ubicacion)
        }
    val pendientes =
        d.snapshot.conteos.filter {
            it.estado == "PENDIENTE" &&
                it.tiendaId in activas &&
                (ubicacion == "TODAS" || it.tiendaId == ubicacion)
        }
    Pagina {
        Selector(
            "",
            ubicacion,
            listOf("TODAS" to "Todas las sucursales") + d.activas.map { it.id to it.nombre },
        ) {
            ubicacion = it
        }
        Text(
            fechaUtc(d.snapshot.actualizadoUtc),
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        Insignia(
            if (d.error == null) "Datos locales actualizados" else "Consulta pendiente",
            if (d.error == null) "exito" else "aviso",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metrica(
                stock.sumOf { it.cantidad.toLong() }.toString(),
                "Unidades oficiales",
                Icons.Default.Inventory2,
                Modifier.weight(1f),
            ) {
                onIr("alertas")
            }
            Metrica(
                stock.size.toString(),
                "Pares tienda-producto",
                Icons.Default.Storefront,
                Modifier.weight(1f),
            ) {
                onIr("sucursales")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metrica(
                stock.count { it.cantidad <= it.minimo }.toString(),
                "Alertas activas",
                Icons.Default.WarningAmber,
                Modifier.weight(1f),
            ) {
                onIr("alertas")
            }
            Metrica(
                pendientes.size.toString(),
                "Diferencias pendientes",
                Icons.Default.Assignment,
                Modifier.weight(1f),
            ) {
                onIr("reporte_diferencias")
            }
        }
        val hoy = LocalDate.now(ZoneOffset.UTC)
        val ultimos =
            d.snapshot.movimientos.filter {
                it.tiendaId in activas && (ubicacion == "TODAS" || it.tiendaId == ubicacion)
            }
        Tarjeta {
            Text("Movimientos", style = MaterialTheme.typography.titleMedium)
            Text(
                "Últimos 7 días · registros confirmados",
                color = Muted,
                style = MaterialTheme.typography.bodySmall,
            )
            val serie =
                (6 downTo 0).map { dias ->
                    val fecha = hoy.minusDays(dias.toLong())
                    fecha to
                        ultimos.count {
                            runCatching {
                                    Instant.parse(it.instanteUtc)
                                        .atOffset(ZoneOffset.UTC)
                                        .toLocalDate() == fecha
                                }
                                .getOrDefault(false)
                        }
                }
            val max = serie.maxOf { it.second }.coerceAtLeast(1)
            Row(
                Modifier.fillMaxWidth().height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                serie.forEach { (fecha, n) ->
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            n.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                        Box(
                            Modifier.width(17.dp)
                                .height((n.toFloat() / max * 58).coerceAtLeast(2f).dp)
                                .background(
                                    Azul,
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                                )
                        )
                        Text(
                            fecha.dayOfMonth.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }
            }
        }
        Tarjeta {
            Text("Stock por ubicación", style = MaterialTheme.typography.titleMedium)
            d.activas
                .filter { ubicacion == "TODAS" || it.id == ubicacion }
                .forEach { t ->
                    val total = stock.filter { it.tiendaId == t.id }.sumOf { it.cantidad.toLong() }
                    val sum = stock.sumOf { it.cantidad.toLong() }.coerceAtLeast(1)
                    Linea(t.nombre, total.toString())
                    LinearProgressIndicator(
                        progress = { total.toFloat() / sum },
                        modifier = Modifier.fillMaxWidth(),
                        color = Azul,
                        trackColor = Borde,
                    )
                }
        }
        Text(
            "Totales de sucursales activas. Las ubicaciones inactivas se excluyen del tablero.",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        Secundario("Historial", icon = Icons.Default.History) { onIr("historial") }
        Secundario("Sucursales", icon = Icons.Default.Storefront) { onIr("sucursales") }
    }
}

@Composable
private fun Metrica(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 115.dp),
        shape = RoundedCornerShape(17.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Borde),
    ) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(icon, null, tint = Azul, modifier = Modifier.size(19.dp))
                Text(value, style = MaterialTheme.typography.headlineSmall)
            }
            Text(label, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SucursalesScreen(
    d: Datos,
    s: GestionUi,
    proponer: (Sucursal) -> Unit,
    cerrar: () -> Unit,
    confirmar: () -> Unit,
) {
    Pagina {
        Text(
            "${d.snapshot.sucursales.count{it.activa}} activas · ${d.snapshot.sucursales.count{!it.activa}} inactivas",
            style = MaterialTheme.typography.labelLarge,
        )
        d.snapshot.sucursales.forEach { t ->
            Tarjeta(color = if (t.activa) Color.White else Color(0xFFF0F5F8)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        if (t.id == "SUC-02") Icons.Default.Warehouse else Icons.Default.Storefront,
                        null,
                        tint = Petroleo,
                    )
                    Column(Modifier.weight(1f)) {
                        Text(t.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${t.id} · ${t.direccion}",
                            color = Muted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Switch(
                        t.activa,
                        onCheckedChange = { proponer(t) },
                        enabled = !s.ocupadoSucursal,
                        thumbContent = {
                            Icon(
                                if (t.activa) Icons.Default.Check else Icons.Default.Close,
                                null,
                                Modifier.size(14.dp),
                            )
                        },
                    )
                }
                Insignia(if (t.activa) "Activa" else "Inactiva", if (t.activa) "exito" else "error")
                Text(
                    if (t.encargadoId.isNotBlank()) "Encargado asignado: ${t.encargadoId}"
                    else "Sin encargado asignado",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "Actualizada: ${fechaUtc(t.actualizadaUtc)}",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        AvisoBox(
            "Control de disponibilidad",
            "Una sucursal inactiva no puede seleccionarse. Su historial se conserva; habilitarla no modifica su stock.",
        )
        Text(
            "Los cambios de estado registran la identidad de Gerencia y la fecha UTC.",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
    }
    val t = s.sucursalPendiente
    if (t != null)
        ModalBottomSheet(onDismissRequest = cerrar, containerColor = Color.White) {
            Column(
                Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                val activar = s.cambioRequest?.activa ?: !t.activa
                Text(
                    "¿${if(activar)"Habilitar" else "Deshabilitar"} ${t.nombre.removePrefix("Tienda ")}?",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    if (activar)
                        "Estará disponible para las cuentas asignadas. El stock y el historial se conservan."
                    else
                        "Dejará de estar disponible para nuevas operaciones. El historial y el stock se conservan.",
                    color = Muted,
                )
                Text(
                    "${t.id} · ${t.nombre}\nActor: ${d.usuario?.id} · Se registrará fecha UTC",
                    style = MaterialTheme.typography.bodySmall,
                )
                s.mensajeSucursal?.let { AvisoBox("No se recibió confirmación", it, "error") }
                Principal(
                    "${if(activar)"Habilitar" else "Deshabilitar"} sucursal",
                    color = if (activar) Exito else Error,
                    busy = s.ocupadoSucursal,
                    onClick = confirmar,
                )
                Secundario(
                    if (t.activa) "Mantener activa" else "Mantener inactiva",
                    enabled = !s.ocupadoSucursal,
                    onClick = cerrar,
                )
                Spacer(Modifier.height(16.dp))
            }
        }
}
