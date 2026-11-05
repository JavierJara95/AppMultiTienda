package com.duoc.appmultitienda.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.duoc.appmultitienda.core.data.InventarioGateway
import com.duoc.appmultitienda.core.model.*
import kotlinx.coroutines.launch

/** Administración del catálogo SOLO para ADMIN; se guarda en el Snapshot Room existente. */
@Composable
fun CatalogoProductosScreen(datos: Datos, repo: InventarioGateway) {
    if (datos.usuario?.rol != "ADMIN") {
        Pagina { AvisoBox("Sin permiso", "Solo Admin puede administrar productos.", "error") }
        return
    }
    val scope = rememberCoroutineScope()
    val sucursales = datos.snapshot.sucursales
    val productos = remember(datos.snapshot.existencias) {
        datos.snapshot.existencias.groupBy { it.producto.sku }.toSortedMap()
            .map { (_, registros) -> registros.first().producto to registros }
    }
    var texto by remember { mutableStateOf("") }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var editar by remember { mutableStateOf<Pair<Producto, List<Existencia>>?>(null) }
    var dialogVersion by remember { mutableIntStateOf(0) }
    var confirmacion by remember { mutableStateOf<Producto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var ocupada by remember { mutableStateOf(false) }
    val resultados = productos.filter { (p, _) ->
        texto.isBlank() || listOf(p.sku, p.nombre, p.codigoBarras, p.categoria)
            .any { it.contains(texto.trim(), ignoreCase = true) }
    }

    Pagina {
        Text("Catálogo de productos", style = MaterialTheme.typography.headlineSmall, color = Tinta)
        Text("Registra productos, asigna sucursales y modifica datos. El saldo se ajusta con movimientos autorizados.",
            style = MaterialTheme.typography.bodyMedium, color = Muted)
        Principal("Registrar producto", icon = Icons.Default.Add, enabled = !ocupada) {
            editar = null; dialogVersion++; error = null; mostrarFormulario = true
        }
        OutlinedTextField(
            value = texto, onValueChange = { texto = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar por código, barras, nombre o categoría") }, singleLine = true
        )
        Text("${resultados.size} productos · ${productos.count { it.first.inactivo != true }} activos",
            style = MaterialTheme.typography.bodySmall, color = Muted)
        error?.let { AvisoBox("No se pudo completar", it, "error") }
        if (resultados.isEmpty()) AvisoBox("Sin resultados", "No hay productos que coincidan con la búsqueda.")
        resultados.forEach { (p, registros) ->
            Tarjeta {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = Petroleo)
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.nombre, style = MaterialTheme.typography.titleMedium, color = Tinta)
                        Text("${p.sku} · ${p.categoria} · ${p.codigoBarras}", style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    Text(if (p.inactivo == true) "Inactivo" else "Activo",
                        color = if (p.inactivo == true) Error else Exito,
                        style = MaterialTheme.typography.labelSmall)
                }
                Text("Sucursales: ${registros.size} · Mínimo: ${registros.first().minimo} · Stock total: ${registros.sumOf { it.cantidad }}",
                    style = MaterialTheme.typography.bodySmall, color = Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        editar = p to registros
                        dialogVersion++; error = null; mostrarFormulario = true
                    }, enabled = !ocupada, modifier = Modifier.weight(1f)) { Text("Editar") }
                    OutlinedButton(onClick = { confirmacion = p }, enabled = !ocupada, modifier = Modifier.weight(1f)) {
                        Text(if (p.inactivo == true) "Reactivar" else "Desactivar")
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        key(dialogVersion) {
            EditorProductoDialog(
                original = editar, sucursales = sucursales, onCerrar = { mostrarFormulario = false },
                onGuardar = { solicitud ->
                    repo.guardarProducto(solicitud, esNuevo = editar == null)
                },
                onGuardado = { mostrarFormulario = false; error = null }
            )
        }
    }
    confirmacion?.let { p ->
        AlertDialog(
            onDismissRequest = { if (!ocupada) confirmacion = null },
            title = { Text(if (p.inactivo == true) "Reactivar producto" else "Desactivar producto") },
            text = { Text(if (p.inactivo == true) "El producto volverá a estar disponible para operaciones." else
                "El producto permanecerá en el catálogo y en los informes, pero no se podrá operar con él. No se elimina su historial.") },
            confirmButton = {
                TextButton(enabled = !ocupada, onClick = {
                    scope.launch {
                        ocupada = true
                        try {
                            repo.cambiarEstadoProducto(p.sku, activo = p.inactivo == true)
                            error = null
                            confirmacion = null
                        } catch (e: Exception) { error = e.message ?: "No se pudo actualizar el estado" }
                        finally { ocupada = false }
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(enabled = !ocupada, onClick = { confirmacion = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun EditorProductoDialog(
    original: Pair<Producto, List<Existencia>>?,
    sucursales: List<Sucursal>,
    onCerrar: () -> Unit,
    onGuardar: suspend (CatalogoProductoRequest) -> Producto,
    onGuardado: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val previo = original?.first
    var sku by remember { mutableStateOf(previo?.sku.orEmpty()) }
    var barras by remember { mutableStateOf(previo?.codigoBarras.orEmpty()) }
    var nombre by remember { mutableStateOf(previo?.nombre.orEmpty()) }
    var categoria by remember { mutableStateOf(previo?.categoria.orEmpty()) }
    var unidad by remember { mutableStateOf(previo?.unidad ?: "ud") }
    var ubicacion by remember { mutableStateOf(previo?.ubicacion.orEmpty()) }
    var minimo by remember { mutableStateOf(original?.second?.firstOrNull()?.minimo?.toString() ?: "5") }
    val idsPrevios = remember(original) { original?.second?.map { it.tiendaId }?.toSet().orEmpty() }
    val marcadas = remember { mutableStateListOf<String>().apply { addAll(idsPrevios) } }
    var guardando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!guardando) onCerrar() },
        title = { Text(if (previo == null) "Registrar producto" else "Editar ${previo.sku}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 490.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = sku, onValueChange = { sku = it.uppercase() },
                    label = { Text("Código interno (SKU)") }, enabled = previo == null, singleLine = true)
                if (previo != null) Text("El SKU no se cambia para conservar referencias históricas.",
                    color = Muted, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(value = barras, onValueChange = { barras = it.filter(Char::isDigit) },
                    label = { Text("Código de barras (8 a 14 dígitos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, singleLine = true)
                OutlinedTextField(value = categoria, onValueChange = { categoria = it }, label = { Text("Categoría") }, singleLine = true)
                OutlinedTextField(value = unidad, onValueChange = { unidad = it }, label = { Text("Unidad (ud, kg, etc.)") }, singleLine = true)
                OutlinedTextField(value = ubicacion, onValueChange = { ubicacion = it }, label = { Text("Ubicación / pasillo") }, singleLine = true)
                OutlinedTextField(value = minimo, onValueChange = { minimo = it.filter(Char::isDigit) },
                    label = { Text("Stock mínimo por sucursal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                Text("Asignar a sucursales", fontWeight = FontWeight.SemiBold)
                sucursales.forEach { sucursal ->
                    val fija = sucursal.id in idsPrevios
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable(enabled = !fija && !guardando) {
                            if (sucursal.id in marcadas) marcadas.remove(sucursal.id) else marcadas.add(sucursal.id)
                        }) {
                        Checkbox(checked = sucursal.id in marcadas,
                            enabled = !fija && !guardando,
                            onCheckedChange = { elegido ->
                                if (elegido) { if (sucursal.id !in marcadas) marcadas.add(sucursal.id) }
                                else marcadas.remove(sucursal.id)
                            })
                        Text(sucursal.nombre + if (fija) " (ya vinculada)" else "", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text("Los productos nuevos comienzan con stock 0 en cada sucursal. Las cantidades se registran mediante entradas.",
                    color = Muted, style = MaterialTheme.typography.bodySmall)
                mensaje?.let { Text(it, color = Error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(enabled = !guardando, onClick = {
                val cantidadMin = minimo.toIntOrNull()
                if (cantidadMin == null) { mensaje = "Ingresa un stock mínimo válido."; return@TextButton }
                scope.launch {
                    guardando = true
                    try {
                        onGuardar(CatalogoProductoRequest(sku, barras, nombre, categoria, unidad, ubicacion,
                            cantidadMin, marcadas.toList()))
                        onGuardado()
                    } catch (e: Exception) { mensaje = e.message ?: "No se pudo guardar el producto." }
                    finally { guardando = false }
                }
            }) { Text(if (guardando) "Guardando…" else "Guardar") }
        },
        dismissButton = { TextButton(enabled = !guardando, onClick = onCerrar) { Text("Cancelar") } },
        shape = RoundedCornerShape(20.dp)
    )
}
