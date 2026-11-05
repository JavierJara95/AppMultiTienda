package com.duoc.appmultitienda.core.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.*
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.duoc.appmultitienda.core.data.InventarioGateway
import com.duoc.appmultitienda.core.model.*
import com.duoc.appmultitienda.core.navigation.Rutas
import com.duoc.appmultitienda.gestion.export.AndroidCsvWriter
import com.duoc.appmultitienda.gestion.ui.*
import com.duoc.appmultitienda.gestion.viewmodel.*
import com.duoc.appmultitienda.operador.scanner.ScannerScreen
import com.duoc.appmultitienda.operador.ui.*
import com.duoc.appmultitienda.operador.viewmodel.OperadorViewModel
import com.duoc.appmultitienda.vmFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun AppRoot(repo: InventarioGateway, amplia: Boolean = false) {
    val sesion: SesionViewModel = viewModel(factory = vmFactory { SesionViewModel(repo) })
    val datos by repo.datos.collectAsStateWithLifecycle()
    val acceso by sesion.acceso.collectAsStateWithLifecycle()
    key(datos.usuario?.id) {
        if (datos.usuario == null) LoginScreen(acceso, sesion::editar, sesion::login)
        else if (datos.usuario?.rol == "GERENCIA") GerenciaOriginalRoot(repo, sesion)
        else SesionActiva(repo, datos, sesion, amplia)
    }
}

private data class Tab(val ruta: String, val titulo: String, val icon: ImageVector)

@Composable
private fun SesionActiva(
    repo: InventarioGateway,
    d: Datos,
    sesion: SesionViewModel,
    amplia: Boolean,
) {
    val context = LocalContext.current
    val user = d.usuario ?: return
    val gerente = user.rol == "ADMIN"
    val encargado = user.rol == "ENCARGADO"
    val op: OperadorViewModel =
        viewModel(
            key = "operador:${user.id}:${d.tiendaId}",
            factory = vmFactory { OperadorViewModel(repo) },
        )
    val gestion: GestionViewModel =
        viewModel(
            key = "gestion:${user.id}:${d.tiendaId}",
            factory =
                vmFactory { GestionViewModel(repo, AndroidCsvWriter(context.contentResolver)) },
        )
    val o by op.ui.collectAsStateWithLifecycle()
    val g by gestion.ui.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val ruta = entry?.destination?.route ?: if (gerente) "dashboard" else "ubicacion"
    var opciones by remember { mutableStateOf(false) }
    val busy =
        o.movimiento.ocupado ||
            o.conteo.ocupado ||
            o.buscando ||
            g.resolucion.ocupado ||
            g.reposicion.ocupado ||
            g.ocupadoSucursal ||
            g.exportacion in listOf("PREPARANDO", "ELEGIR", "SELECTOR", "GUARDANDO")
    val ir: (String) -> Unit = { target ->
        val permitida = Rutas.permite(user.rol, target)
        val next =
            if (!gerente && d.tiendaId == null && target !in setOf("ubicacion", "perfil"))
                "ubicacion"
            else target
        if (permitida)
            nav.navigate(next) {
                launchSingleTop = true
                when (next) {
                    "comprobante" -> popUpTo("movimiento") { inclusive = true }
                    "conteo_recibido" -> popUpTo("conteo") { inclusive = true }
                    "resolucion_recibida" -> popUpTo("diferencias") { inclusive = false }
                    "reposicion_recibida" -> popUpTo("reposicion") { inclusive = true }
                }
            }
    }
    LaunchedEffect(op) { op.eventos.collect { ir(it) } }
    LaunchedEffect(gestion) { gestion.eventos.collect { ir(it) } }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(user.id, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                try {
                    repo.refrescar()
                } catch (_: FalloDatos) {}
                delay(15_000)
            }
        }
    }
    LaunchedEffect(d.tiendaId, d.snapshot.sucursales) {
        if (!gerente && d.tiendaId == null && ruta !in setOf("ubicacion", "perfil"))
            nav.navigate("ubicacion") {
                popUpTo(nav.graph.id) { inclusive = true }
                launchSingleTop = true
            }
    }
    BackHandler(enabled = busy) { /* Confirmación en curso: se espera su resultado. */ }
    val tabs =
        when {
            gerente ->
                listOf(
                    Tab("dashboard", "Dashboard", Icons.Default.BarChart),
                    Tab("alertas", "Alertas", Icons.Default.NotificationsNone),
                    Tab("reportes", "Reportes", Icons.Default.Description),
                    Tab("catalogo", "Productos", Icons.Default.Inventory2),
                    Tab("sucursales", "Más", Icons.Default.Menu),
                )
            encargado ->
                listOf(
                    Tab("inicio", "Inicio", Icons.Default.Home),
                    Tab("diferencias", "Diferencias", Icons.Default.Assignment),
                    Tab("alertas", "Alertas", Icons.Default.NotificationsNone),
                    Tab("historial", "Historial", Icons.Default.History),
                )
            else ->
                listOf(
                    Tab("inicio", "Inicio", Icons.Default.Home),
                    Tab("escaner", "Escáner", Icons.Default.QrCodeScanner),
                    Tab("historial", "Historial", Icons.Default.History),
                    Tab("perfil", "Perfil", Icons.Default.PersonOutline),
                )
        }
    val seleccion =
        when (ruta) {
            "manual",
            "manual_denegado",
            "producto",
            "movimiento",
            "revision",
            "comprobante",
            "conteo",
            "conteo_recibido" -> if (encargado) "inicio" else "escaner"
            "resolver",
            "conflicto",
            "resolucion_recibida" -> "diferencias"
            "reposicion",
            "reposicion_recibida" -> "alertas"
            "exportacion_resultado" -> "reportes"
            "ubicacion" -> "inicio"
            else -> ruta
        }
    val title =
        when (ruta) {
            "inicio" -> "Hola, ${user.nombre}"
            "resolver" -> if (g.resolucion.aprobar) "Aprobar ajuste" else "Rechazar diferencia"
            "resolucion_recibida" ->
                if (g.reciboResolucion?.conteo?.estado == "APROBADO") "Stock actualizado"
                else "Conteo rechazado"
            else -> Rutas.titulos[ruta] ?: "MultiTienda"
        }
    val subtitle =
        if (gerente) "Administración · ${d.activas.size} sucursales activas"
        else d.tienda?.nombre ?: "Sin ubicación seleccionada"
    Scaffold(
        containerColor = Marino,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Encabezado(
                title,
                subtitle,
                user.rol,
                if (nav.previousBackStackEntry != null && !busy) {
                    { nav.popBackStack() }
                } else null,
            ) {
                opciones = true
            }
            DropdownMenu(opciones, onDismissRequest = { opciones = false }) {
                DropdownMenuItem(
                    text = { Text("${user.id} · Perfil") },
                    enabled = !busy,
                    onClick = {
                        opciones = false
                        ir("perfil")
                    },
                )
                if (gerente) DropdownMenuItem(
                    text = { Text("Gestionar productos") },
                    enabled = !busy,
                    onClick = { opciones = false; ir("catalogo") },
                )
                if (!gerente)
                    DropdownMenuItem(
                        text = { Text("Cambiar ubicación") },
                        enabled = !busy,
                        onClick = {
                            opciones = false
                            ir("ubicacion")
                        },
                    )
                DropdownMenuItem(
                    text = { Text("Actualizar datos") },
                    enabled = !busy,
                    onClick = {
                        opciones = false
                        sesion.refrescar()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Cerrar sesión") },
                    enabled = !busy,
                    onClick = {
                        opciones = false
                        sesion.logout()
                    },
                )
            }
        },
        bottomBar = {
            if (!amplia)
                NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected = seleccion == t.ruta,
                            onClick = { ir(t.ruta) },
                            enabled = !busy,
                            icon = { Icon(t.icon, null) },
                            label = { Text(t.titulo, style = MaterialTheme.typography.labelSmall) },
                            colors =
                                NavigationBarItemDefaults.colors(
                                    selectedIconColor = Azul,
                                    selectedTextColor = Azul,
                                    indicatorColor = Color(0xFFE5F3FD),
                                    unselectedTextColor = Muted,
                                    unselectedIconColor = Muted,
                                ),
                        )
                    }
                }
        },
    ) { padding ->
        Row(Modifier.fillMaxSize().padding(padding)) {
            if (amplia)
                NavigationRail(containerColor = Color.White, modifier = Modifier.fillMaxHeight()) {
                    tabs.forEach { t ->
                        NavigationRailItem(
                            selected = seleccion == t.ruta,
                            onClick = { ir(t.ruta) },
                            enabled = !busy,
                            icon = { Icon(t.icon, null) },
                            label = { Text(t.titulo) },
                        )
                    }
                }
            Box(
                Modifier.weight(1f).fillMaxHeight().background(Fondo),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(Modifier.widthIn(max = 840.dp).fillMaxSize()) {
                    if (d.cargando && d.snapshot.actualizadoUtc.isBlank())
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    d.error?.let { err ->
                        Surface(color = Color(0xFFFFF3D7), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    err,
                                    color = Aviso,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(
                                    onClick = { sesion.refrescar() },
                                    enabled = !d.cargando,
                                ) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                    NavHost(
                        navController = nav,
                        startDestination = if (gerente) "dashboard" else "ubicacion",
                        modifier = Modifier.weight(1f),
                    ) {
                        composable("ubicacion") {
                            UbicacionScreen(d) { id ->
                                sesion.seleccionar(id) {
                                    nav.navigate("inicio") {
                                        popUpTo("ubicacion") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }
                        composable("inicio") {
                            if (encargado)
                                Pagina {
                                    Text(
                                        "Inventario de la ubicación",
                                        style = MaterialTheme.typography.headlineSmall,
                                    )
                                    Tarjeta {
                                        Linea("Productos", d.existenciasTienda.size.toString())
                                        Linea(
                                            "Diferencias pendientes",
                                            d.snapshot.conteos
                                                .count {
                                                    it.tiendaId == d.tiendaId &&
                                                        it.estado == "PENDIENTE"
                                                }
                                                .toString(),
                                        )
                                        Linea(
                                            "Alertas de stock",
                                            d.existenciasTienda
                                                .count { it.cantidad <= it.minimo }
                                                .toString(),
                                        )
                                    }
                                    Principal(
                                        "Revisar diferencias",
                                        icon = Icons.Default.Assignment,
                                    ) {
                                        ir("diferencias")
                                    }
                                    Secundario(
                                        "Consultar producto",
                                        icon = Icons.Default.QrCodeScanner,
                                    ) {
                                        op.accion("producto")
                                        ir("escaner")
                                    }
                                    Secundario("Alertas y reposición") { ir("alertas") }
                                    Secundario("Cambiar ubicación") { ir("ubicacion") }
                                }
                            else
                                InicioOperadorScreen(
                                    d,
                                    { a ->
                                        op.accion(a)
                                        ir("escaner")
                                    },
                                    { ir("ubicacion") },
                                    {
                                        gestion.historial(
                                            HistorialFiltro(tipo = "CONTEO", actor = user.id)
                                        )
                                        ir("historial")
                                    },
                                )
                        }
                        composable("escaner") {
                            ScannerScreen(o.buscando, op::buscar) { denied ->
                                ir(if (denied) "manual_denegado" else "manual")
                            }
                        }
                        composable("manual") {
                            ManualScreen(o, false, op::codigo, { op.buscar() }) { ir("escaner") }
                        }
                        composable("manual_denegado") {
                            ManualScreen(o, true, op::codigo, { op.buscar() }) { ir("escaner") }
                        }
                        composable("producto") {
                            val producto =
                                o.producto?.let { selected ->
                                    d.snapshot.existencias.firstOrNull {
                                        it.tiendaId == d.tiendaId &&
                                            it.producto.sku == selected.producto.sku
                                    }
                                }
                            ProductoScreen(
                                producto,
                                d,
                                { op.abrirMovimiento() },
                                { op.abrirConteo() },
                                {
                                    gestion.historial(
                                        HistorialFiltro(sku = producto?.producto?.sku ?: "TODOS")
                                    )
                                    ir("historial")
                                },
                            )
                        }
                        composable("movimiento") {
                            MovimientoScreen(
                                o.movimiento,
                                op::editarMovimiento,
                                op::revisarMovimiento,
                            )
                        }
                        composable("revision") {
                            RevisionMovimientoScreen(o.movimiento, d, op::confirmarMovimiento) {
                                ir("movimiento")
                            }
                        }
                        composable("comprobante") {
                            ComprobanteScreen(
                                o.reciboMovimiento,
                                o.movimiento.base,
                                { ir("inicio") },
                                {
                                    gestion.historial(
                                        HistorialFiltro(texto = o.reciboMovimiento?.id.orEmpty())
                                    )
                                    ir("historial")
                                },
                            )
                        }
                        composable("conteo") {
                            ConteoScreen(o.conteo, op::editarConteo, op::enviarConteo)
                        }
                        composable("conteo_recibido") {
                            ConteoRecibidoScreen(o.reciboConteo, o.conteo.base) { ir("producto") }
                        }
                        composable("diferencias") { DiferenciasScreen(d, gestion::revisar) }
                        composable("resolver") {
                            ResolverScreen(g.resolucion, d, gestion::motivo, gestion::resolver) {
                                ir("diferencias")
                            }
                        }
                        composable("conflicto") {
                            ConflictoScreen(g.resolucion, d, gestion::revisarSaldoNuevo) {
                                ir("diferencias")
                            }
                        }
                        composable("resolucion_recibida") {
                            ResolucionRecibidaScreen(
                                g.reciboResolucion,
                                {
                                    gestion.historial(
                                        HistorialFiltro(
                                            texto = g.reciboResolucion?.evento?.id.orEmpty()
                                        )
                                    )
                                    ir("historial")
                                },
                                { ir("diferencias") },
                            )
                        }
                        composable("historial") {
                            HistorialScreen(d, g.historial, g.erroresHistorial, gestion::historial)
                        }
                        composable("alertas") {
                            AlertasScreen(d, gestion::abrirReposicion) { ir("diferencias") }
                        }
                        composable("reposicion") {
                            ReposicionScreen(
                                g.reposicion,
                                d,
                                gestion::editarReposicion,
                                gestion::reponer,
                            )
                        }
                        composable("reposicion_recibida") {
                            ReposicionRecibidaScreen(g.reciboReposicion, d) { ir("alertas") }
                        }
                        composable("dashboard") {
                            DashboardScreen(d) { r ->
                                if (r == "reporte_diferencias") {
                                    gestion.reporte(g.reporte.copy(tipo = "Diferencias"))
                                    ir("reportes")
                                } else ir(r)
                            }
                        }
                        composable("reportes") { ReportesScreen(d, g, gestion) }
                        composable("exportacion_resultado") {
                            ExportacionResultadoScreen(g, { ir("reportes") }, { ir("reportes") })
                        }
                        composable("catalogo") {
                            CatalogoProductosScreen(datos = d, repo = repo)
                        }
                        composable("sucursales") {
                            SucursalesScreen(
                                d,
                                g,
                                gestion::proponerSucursal,
                                gestion::cerrarSucursal,
                                gestion::cambiarSucursal,
                            )
                        }
                        composable("perfil") {
                            Pagina {
                                Text(user.nombre, style = MaterialTheme.typography.headlineSmall)
                                Tarjeta {
                                    Linea("Cuenta", user.id)
                                    Linea("Rol", user.rol.rolVisible())
                                    Linea("Ubicación", d.tienda?.nombre ?: "Todas")
                                }
                                AvisoBox(
                                    "Demostración académica",
                                    "Usuarios y datos ficticios. Todas las operaciones se guardan localmente.",
                                )
                                if (!gerente) Secundario("Cambiar ubicación") { ir("ubicacion") }
                                Principal("Cerrar sesión", enabled = !busy) { sesion.logout() }
                            }
                        }
                    }
                }
            }
        }
    }
}
