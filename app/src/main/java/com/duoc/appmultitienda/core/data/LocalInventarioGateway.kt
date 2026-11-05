package com.duoc.appmultitienda.core.data

import android.content.Context
import com.duoc.appmultitienda.core.model.*
import com.duoc.appmultitienda.gestion.local.EstadoLocalEntity
import com.duoc.appmultitienda.gestion.local.GestionLocalDatabase
import com.google.gson.Gson
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Adaptador local del flujo completo v1.0.1: misma interfaz, SIN HTTP.
 * Comparte la base existente de Gestión para saldos, eventos, conteos y solicitudes.
 */
class LocalInventarioGateway(context: Context) : InventarioGateway {
    private val dao = GestionLocalDatabase.obtener(context).estadoLocalDao()
    private val gson = Gson()
    private val mutex = Mutex()
    private val _datos = MutableStateFlow(Datos())
    override val datos: StateFlow<Datos> = _datos.asStateFlow()
    override val preferencias: Preferencias = object : Preferencias {
        override suspend fun leer(clave: String): String? = dao.leer("pref:$clave")
        override suspend fun escribir(clave: String, valor: String) { dao.guardar(EstadoLocalEntity("pref:$clave", valor)) }
        override suspend fun borrar(clave: String) { dao.borrar("pref:$clave") }
    }

    private fun ahora() = Instant.now().toString()
    private fun fallo(mensaje: String, codigo: String = "VALIDACION", vigente: Existencia? = null): Nothing =
        throw FalloDatos(mensaje, codigo, vigente = vigente)
    private fun actual(): Usuario = _datos.value.usuario ?: fallo("Inicia sesión.", "SESION")
    private fun exige(vararg roles: String): Usuario = actual().also {
        if (it.rol !in roles) fallo("Tu perfil no tiene permiso para esta operación.", "ROL")
    }
    private fun checkMotivo(texto: String) {
        if (texto.trim().length !in 5..200) fallo("Escribe entre 5 y 200 caracteres.", "MOTIVO")
    }
    private fun checkTienda(snapshot: Snapshot, id: String) {
        val tienda = snapshot.sucursales.firstOrNull { it.id == id && it.activa && it.permitida }
        if (tienda == null) fallo("La ubicación debe estar activa y asignada a tu cuenta.", "TIENDA")
    }
    private fun checkVersion(e: Existencia, version: Long) {
        if (e.version != version) fallo("El saldo cambió. Actualiza y revisa de nuevo.", "STOCK_CAMBIO", e)
    }
    private fun buscarExistencia(s: Snapshot, id: String, sku: String): Existencia =
        s.existencias.firstOrNull { it.tiendaId == id && it.producto.sku == sku }
            ?: fallo("Producto no encontrado en la ubicación.", "PRODUCTO")

    private suspend fun estadoGuardado(): Snapshot =
        dao.leer("snapshot")?.let { runCatching { gson.fromJson(it, Snapshot::class.java) }.getOrNull() }
            ?: semilla()

    private suspend fun publicar(snapshot: Snapshot) {
        val actualizada = snapshot.copy(actualizadoUtc = ahora())
        dao.guardar(EstadoLocalEntity("snapshot", gson.toJson(actualizada)))
        _datos.value = _datos.value.copy(snapshot = visible(actualizada, _datos.value.usuario), cargando = false, error = null)
    }

    /** No ocultamos datos al guardar. Se filtran solo cuando se publican a cada rol. */
    private fun visible(s: Snapshot, usuario: Usuario?): Snapshot {
        if (usuario == null || usuario.rol in setOf("ADMIN", "GERENCIA")) return s
        val asignadas = usuario.tiendas.toSet()
        return s.copy(
            sucursales = s.sucursales.map { it.copy(permitida = it.id in asignadas) },
            existencias = s.existencias.filter { it.tiendaId in asignadas },
            movimientos = s.movimientos.filter { it.tiendaId in asignadas },
            conteos = s.conteos.filter { it.tiendaId in asignadas },
            reposiciones = s.reposiciones.filter { it.tiendaId in asignadas },
            eventos = s.eventos.filter { it.tiendaId in asignadas }
        )
    }

    private suspend fun guardarCambio(transformar: (Snapshot) -> Snapshot) {
        val original = estadoGuardado()
        publicar(transformar(original))
    }

    override suspend fun login(usuario: String, clave: String) = mutex.withLock {
        val cuenta = when (usuario.trim().lowercase()) {
            "op.alameda" -> Usuario("OPR-017", "Operador", "OPERADOR", listOf("SUC-01"))
            "enc.alameda" -> Usuario("ENC-004", "Encargado", "ENCARGADO", listOf("SUC-01"))
            "gerencia" -> Usuario("GER-001", "Gerencia", "GERENCIA", listOf("SUC-01", "SUC-02", "SUC-03"))
            "admin" -> Usuario("ADM-001", "Admin", "ADMIN", listOf("SUC-01", "SUC-02", "SUC-03"))
            else -> null
        }
        if (clave != "demo123" || cuenta == null) fallo("Usuario o contraseña incorrectos.", "ACCESO")
        val s = estadoGuardado()
        val elegida = preferencias.leer("tienda:${cuenta.id}")?.takeIf { id -> s.sucursales.any { it.id == id && it.activa && id in cuenta.tiendas } }
        _datos.value = Datos(usuario = cuenta, snapshot = visible(s, cuenta), tiendaId = elegida)
    }

    override suspend fun logout() = mutex.withLock { _datos.value = Datos() }

    override suspend fun refrescar() = mutex.withLock {
        val d = _datos.value
        if (d.usuario != null) _datos.value = d.copy(snapshot = visible(estadoGuardado(), d.usuario), error = null)
    }

    override suspend fun seleccionar(id: String) = mutex.withLock {
        val u = actual()
        if (id !in u.tiendas) fallo("Esta ubicación no está asignada a tu usuario.", "TIENDA")
        checkTienda(_datos.value.snapshot, id)
        preferencias.escribir("tienda:${u.id}", id)
        _datos.value = _datos.value.copy(tiendaId = id)
    }

    override suspend fun buscar(codigo: String): Existencia = mutex.withLock {
        val d = _datos.value
        val id = d.tiendaId ?: fallo("Selecciona una ubicación.", "TIENDA")
        checkTienda(d.snapshot, id)
        d.snapshot.existencias.firstOrNull { it.tiendaId == id &&
            (it.producto.sku.equals(codigo.trim(), true) || it.producto.codigoBarras == codigo.trim()) && it.producto.inactivo != true }
            ?: fallo("El código no existe en esta ubicación.", "PRODUCTO")
    }

    private suspend fun idempotente(llave: String, firma: String, tipo: Class<*>): Any? {
        val prev = dao.leer("idempotente:$llave") ?: return null
        val sep = prev.indexOf('\n')
        if (sep < 0 || prev.substring(0, sep) != firma) fallo("La operación fue reutilizada con datos distintos.", "DUPLICADO")
        return gson.fromJson(prev.substring(sep + 1), tipo)
    }
    private suspend fun recordar(llave: String, firma: String, valor: Any) {
        dao.guardar(EstadoLocalEntity("idempotente:$llave", firma + "\n" + gson.toJson(valor)))
    }

    override suspend fun movimiento(request: MovimientoRequest): Movimiento = mutex.withLock {
        val u = exige("OPERADOR", "ENCARGADO")
        val firma = gson.toJson(request.copy(idempotencyKey = ""))
        (idempotente(request.idempotencyKey, firma, Movimiento::class.java) as Movimiento?)?.let { return@withLock it }
        val s = estadoGuardado(); checkTienda(visible(s, u), request.tiendaId)
        checkMotivo(request.motivo)
        val e = buscarExistencia(s, request.tiendaId, request.sku);if (e.producto.inactivo == true) fallo("Producto desactivado.", "PRODUCTO");checkVersion(e, request.version)
        if (request.tipo !in setOf("ENTRADA", "SALIDA") || request.cantidad <= 0) fallo("Cantidad inválida.", "CANTIDAD")
        val diferencia = if (request.tipo == "ENTRADA") request.cantidad else -request.cantidad
        val total = e.cantidad.toLong() + diferencia
        if (total !in 0..Int.MAX_VALUE.toLong()) fallo("Saldo insuficiente o fuera de rango.", "SALDO", e)
        val nuevo = e.copy(cantidad = total.toInt(), version = e.version + 1)
        val m = Movimiento("MOV-" + UUID.randomUUID().toString().take(8), e.tiendaId, e.producto.sku, request.tipo, diferencia,
            e.cantidad, nuevo.cantidad, request.motivo.trim(), request.referencia.trim(), u.id, ahora(), "")
        val evento = Evento(m.id, m.tiendaId, m.sku, m.tipo, if (diferencia > 0) "Entrada" else "Salida", diferencia,
            e.cantidad, nuevo.cantidad, m.motivo, m.referencia, u.id, m.instanteUtc, "CONFIRMADO")
        publicar(s.copy(existencias = s.existencias.map { if (it.tiendaId == e.tiendaId && it.producto.sku == e.producto.sku) nuevo else it },
            movimientos = listOf(m) + s.movimientos, eventos = listOf(evento) + s.eventos))
        recordar(request.idempotencyKey, firma, m)
        m
    }

    override suspend fun contar(request: ConteoRequest): Conteo = mutex.withLock {
        val u = exige("OPERADOR", "ENCARGADO")
        val firma = gson.toJson(request.copy(idempotencyKey = ""))
        (idempotente(request.idempotencyKey, firma, Conteo::class.java) as Conteo?)?.let { return@withLock it }
        val s = estadoGuardado(); checkTienda(visible(s, u), request.tiendaId)
        val e = buscarExistencia(s, request.tiendaId, request.sku);if (e.producto.inactivo == true) fallo("Producto desactivado.", "PRODUCTO");checkVersion(e, request.version)
        if (request.contado < 0) fallo("El conteo no puede ser negativo.", "CANTIDAD")
        val estado = if (request.contado == e.cantidad) "CONCILIADO" else "PENDIENTE"
        val c = Conteo("CNT-" + UUID.randomUUID().toString().take(8), e.tiendaId, e.producto.sku, e.cantidad,
            request.contado, request.observacion.trim(), u.id, ahora(), estado, e.version, "", "", "", "")
        val evento = Evento(c.id,c.tiendaId,c.sku,"CONTEO","Conteo físico",c.diferencia,e.cantidad,e.cantidad,
            c.observacion,c.id,u.id,c.instanteUtc,estado)
        publicar(s.copy(conteos = listOf(c) + s.conteos, eventos = listOf(evento) + s.eventos))
        recordar(request.idempotencyKey, firma, c)
        c
    }

    override suspend fun resolver(id: String, request: ResolverRequest): Resolucion = mutex.withLock {
        val u = exige("ENCARGADO")
        val firma = id + ":" + gson.toJson(request.copy(idempotencyKey = ""))
        (idempotente(request.idempotencyKey, firma, Resolucion::class.java) as Resolucion?)?.let { return@withLock it }
        val s = estadoGuardado()
        val c = s.conteos.firstOrNull { it.id == id } ?: fallo("Conteo inexistente.", "CONTEO")
        if (c.estado != "PENDIENTE") fallo("El conteo ya fue resuelto.", "RESUELTO")
        if (c.tiendaId !in u.tiendas) fallo("Encargado no asignado a esta sucursal.", "ENCARGADO")
        checkTienda(visible(s, u), c.tiendaId);checkMotivo(request.motivo)
        val e = buscarExistencia(s, c.tiendaId, c.sku)
        if (request.aprobar) checkVersion(e, request.version)
        val nuevo = if (request.aprobar) e.copy(cantidad = c.contado, version = e.version + 1) else e
        val decision = (if (request.aprobar) "AJ-" else "DEC-") + UUID.randomUUID().toString().take(8)
        val fecha = ahora()
        val actualizado = c.copy(estado = if (request.aprobar) "APROBADO" else "RECHAZADO",
            motivoResolucion = request.motivo.trim(), resolutorId = u.id, resueltoUtc = fecha, decisionId = decision)
        val evento = Evento(decision, c.tiendaId, c.sku, if (request.aprobar) "AJUSTE" else "RECHAZO",
            if (request.aprobar) "Ajuste aprobado" else "Conteo rechazado", if (request.aprobar) c.contado - e.cantidad else 0,
            e.cantidad, nuevo.cantidad, request.motivo.trim(), c.id, u.id, fecha, actualizado.estado)
        val mov = if (request.aprobar) listOf(Movimiento(decision, c.tiendaId, c.sku, "AJUSTE", nuevo.cantidad - e.cantidad,
            e.cantidad, nuevo.cantidad, request.motivo.trim(), c.id, u.id, fecha, c.id)) else emptyList()
        publicar(s.copy(conteos = s.conteos.map { if (it.id == id) actualizado else it },
            existencias = s.existencias.map { if (it.tiendaId == e.tiendaId && it.producto.sku == e.producto.sku) nuevo else it },
            movimientos = mov + s.movimientos, eventos = listOf(evento) + s.eventos))
        Resolucion(actualizado, nuevo, evento).also { recordar(request.idempotencyKey, firma, it) }
    }

    override suspend fun reponer(request: ReposicionRequest): Reposicion = mutex.withLock {
        val u = exige("ENCARGADO")
        val firma = gson.toJson(request.copy(idempotencyKey = ""))
        (idempotente(request.idempotencyKey, firma, Reposicion::class.java) as Reposicion?)?.let { return@withLock it }
        val s = estadoGuardado();checkTienda(visible(s,u),request.tiendaId)
        if (request.cantidad < 1 || request.destinoId == request.tiendaId) fallo("Cantidad o destino incorrectos.","REPOSICION")
        checkMotivo(request.motivo)
        val e = buscarExistencia(s,request.tiendaId,request.sku)
        if (s.sucursales.none { it.id == request.destinoId && it.activa }) fallo("El destino no está activo.","DESTINO")
        val r = Reposicion("REP-" + UUID.randomUUID().toString().take(8),request.tiendaId,request.sku,request.cantidad,
            request.motivo.trim(),request.destinoId,u.id,ahora(),"PENDIENTE")
        val evento = Evento(r.id,r.tiendaId,r.sku,"REPOSICION","Solicitud de reposición",r.cantidad,e.cantidad,e.cantidad,
            r.motivo,r.destinoId,u.id,r.instanteUtc,r.estado)
        publicar(s.copy(reposiciones = listOf(r) + s.reposiciones, eventos = listOf(evento) + s.eventos))
        recordar(request.idempotencyKey,firma,r)
        r
    }

    override suspend fun cambiarSucursal(id: String, request: EstadoTiendaRequest): Sucursal = mutex.withLock {
        val u = exige("ADMIN")
        val firma = id + ":" + request.activa
        (idempotente(request.idempotencyKey, firma, Sucursal::class.java) as Sucursal?)?.let { return@withLock it }
        val s = estadoGuardado();val previo = s.sucursales.firstOrNull { it.id == id } ?: fallo("Sucursal inexistente.","TIENDA")
        val actualizado = previo.copy(activa = request.activa, actualizadaUtc = ahora())
        val evento = Evento("SUC-EV-" + UUID.randomUUID().toString().take(8),id,"","SUCURSAL",
            if (request.activa) "Sucursal habilitada" else "Sucursal deshabilitada",0,0,0,
            "Cambio de disponibilidad",previo.nombre,u.id,ahora(),if(request.activa)"ACTIVA" else "INACTIVA")
        publicar(s.copy(sucursales = s.sucursales.map { if(it.id == id) actualizado else it }, eventos = listOf(evento) + s.eventos))
        recordar(request.idempotencyKey,firma,actualizado)
        actualizado
    }

    /** Nuevo catálogo editable, persistido en la misma tabla Room de snapshot. */
    override suspend fun guardarProducto(request: CatalogoProductoRequest, esNuevo: Boolean): Producto = mutex.withLock {
        val u = exige("ADMIN")
        val sku = request.sku.trim().uppercase()
        val barras = request.codigoBarras.trim()
        val nombre = request.nombre.trim()
        val categoria = request.categoria.trim()
        val unidad = request.unidad.trim()
        val ubicacion = request.ubicacion.trim()
        val tiendas = request.sucursalesIds.distinct()
        if (!Regex("[A-Z0-9][A-Z0-9-]{2,19}").matches(sku)) fallo("Código interno: 3 a 20 letras, números o guiones.", "PRODUCTO")
        if (!Regex("[0-9]{8,14}").matches(barras)) fallo("Código de barras: entre 8 y 14 dígitos.", "PRODUCTO")
        if (nombre.length !in 3..100 || categoria.length !in 2..50 || unidad.length !in 1..20 || ubicacion.length !in 2..80)
            fallo("Revisa nombre (3-100), categoría (2-50), unidad (1-20) y ubicación (2-80).", "PRODUCTO")
        if (request.minimo !in 0..1_000_000) fallo("Stock mínimo fuera de rango.", "PRODUCTO")
        if (tiendas.isEmpty()) fallo("Selecciona al menos una sucursal.", "PRODUCTO")
        val s = estadoGuardado()
        if (tiendas.any { id -> s.sucursales.none { it.id == id } }) fallo("Sucursal inválida.", "TIENDA")
        val anteriores = s.existencias.filter { it.producto.sku == sku }
        if (esNuevo && anteriores.isNotEmpty()) fallo("El código interno ya existe.", "DUPLICADO")
        if (!esNuevo && anteriores.isEmpty()) fallo("El producto ya no existe.", "PRODUCTO")
        if (s.existencias.any { it.producto.codigoBarras == barras && it.producto.sku != sku })
            fallo("El código de barras ya pertenece a otro producto.", "DUPLICADO")
        val prevIds = anteriores.map { it.tiendaId }.toSet()
        // No eliminar relaciones: podrían referenciar saldos, movimientos o conteos históricos.
        if (!tiendas.containsAll(prevIds)) fallo("No se puede quitar una sucursal ya vinculada; se conserva la trazabilidad.", "HISTORIAL")
        val previo = anteriores.firstOrNull()?.producto
        val producto = Producto(sku, barras, nombre, categoria, unidad, ubicacion, previo?.inactivo)
        val actualizados = s.existencias.map { e ->
            if (e.producto.sku == sku) e.copy(producto = producto, minimo = request.minimo, version = e.version + 1)
            else e
        }.toMutableList()
        for (id in tiendas) if (id !in prevIds) actualizados.add(Existencia(id, producto, 0, request.minimo, 0))
        val evento = Evento("PRD-" + UUID.randomUUID().toString().take(8), tiendas.first(), sku,
            "PRODUCTO", if (esNuevo) "Producto registrado" else "Ficha de producto actualizada", 0, 0, 0,
            if (esNuevo) "Alta de catálogo" else "Cambio de datos y mínimo", nombre, u.id, ahora(), "CONFIRMADO")
        publicar(s.copy(existencias = actualizados, eventos = listOf(evento) + s.eventos))
        producto
    }

    override suspend fun cambiarEstadoProducto(sku: String, activo: Boolean): Producto = mutex.withLock {
        val u = exige("ADMIN")
        val s = estadoGuardado()
        val existentes = s.existencias.filter { it.producto.sku == sku }
        if (existentes.isEmpty()) fallo("Producto no encontrado.", "PRODUCTO")
        val producto = existentes.first().producto
        if (producto.inactivo != true && activo || producto.inactivo == true && !activo)
            return@withLock producto
        if (!activo && s.conteos.any { it.sku == sku && it.estado == "PENDIENTE" })
            fallo("Hay conteos pendientes de aprobación para este producto.", "CONTEO")
        val nuevo = producto.copy(inactivo = if (activo) null else true)
        val evento = Evento("PRD-" + UUID.randomUUID().toString().take(8), existentes.first().tiendaId,
            sku, "PRODUCTO", if (activo) "Producto reactivado" else "Producto desactivado", 0, 0, 0,
            "Cambio de estado de catálogo", producto.nombre, u.id, ahora(), if (activo) "ACTIVO" else "INACTIVO")
        publicar(s.copy(existencias = s.existencias.map {
            if (it.producto.sku == sku) it.copy(producto = nuevo, version = it.version + 1) else it
        }, eventos = listOf(evento) + s.eventos))
        nuevo
    }

    private fun ean(prefix: String): String {
        val suma = prefix.mapIndexed { i, c -> (c - '0') * (if (i % 2 == 0) 1 else 3) }.sum()
        return prefix + ((10 - suma % 10) % 10)
    }
    /** Productos y secuencia de operaciones inspirados en el catálogo original v1.0.1. */
    private fun semilla(): Snapshot {
        val productos = mutableListOf(
            Producto("CAF-001","7501234500018","Café molido 500 g","Despensa","ud","Pasillo 2 · A-03"),
            Producto("ARR-002","7501234500025","Arroz largo 1 kg","Despensa","ud","Pasillo 1 · A-01"),
            Producto("ACE-003","7501234500032","Aceite vegetal 1 L","Despensa","ud","Pasillo 1 · A-02"),
            Producto("LEC-004","7501234500049","Leche entera 1 L","Lácteos","ud","Pasillo 3 · B-01"),
            Producto("GAL-005","7501234500056","Galletas de avena 200 g","Despensa","ud","Pasillo 2 · A-04"),
            Producto("HAR-006","7501234500063","Harina 1 kg","Despensa","ud","Pasillo 1 · A-04")
        )
        val nombres = listOf("Azúcar 1 kg","Sal fina 1 kg","Fideos 400 g","Salsa de pimentón 200 g","Lentejas 1 kg",
            "Garbanzos 1 kg","Té negro 20 bolsas","Mermelada 250 g","Atún 170 g","Agua 1,5 L","Jugo 1 L","Avena 500 g",
            "Chocolate 100 g","Jabón 90 g","Detergente 1 L","Papel higiénico 4 rollos","Servilletas 100 unidades","Toallas de papel",
            "Esponjas 3 unidades","Lavalozas 750 ml","Shampoo 400 ml","Pasta dental 90 g","Cepillo dental","Bolsas de basura 10 unidades",
            "Yogur natural 1 L","Mantequilla 250 g","Queso 250 g","Maíz en conserva","Arvejas 300 g","Aceitunas 200 g",
            "Puré de papas 125 g","Cereal 300 g","Frutos secos 200 g","Galletas de agua 200 g","Vinagre 500 ml","Bicarbonato 100 g")
        for (i in nombres.indices) {
            val n = i + 7
            productos += Producto("PRD-%03d".format(n),ean("78000000%04d".format(n)),nombres[i],
                if (i in 13..23) "Hogar" else "Despensa", "ud", "Pasillo ${i % 4 + 1} · B-%02d".format(i + 1))
        }
        val ta = intArrayOf(48,16,0,36,6,22); val tn = intArrayOf(24,40,18,36,14,30)
        val min = intArrayOf(12,24,8,10,10,10)
        var totalA = 0;var totalN = 0
        val existencias = mutableListOf<Existencia>()
        productos.forEachIndexed { i,p ->
            var a = if(i < 6) ta[i] else 19
            var n = if(i < 6) tn[i] else if(i < 8) 0 else if(i < 12) i - 6 else 35
            if(i == 41) { a = 830 - totalA;n = 1312 - totalN }
            totalA += a;totalN += n
            val m = if(i < 6) min[i] else 5
            existencias += Existencia("SUC-01",p,a,m,0)
            existencias += Existencia("SUC-02",p,n,m,0)
            existencias += Existencia("SUC-03",p,0,m,0)
        }
        val sucursales = listOf(
            Sucursal("SUC-01","Tienda Alameda","Av. Alameda 120",true,"ENC-004",ahora(),true),
            Sucursal("SUC-02","Bodega Norte","Parque Industrial 45",true,"ENC-006",ahora(),true),
            Sucursal("SUC-03","Tienda Mirador","Calle Mirador 86",false,"ENC-008",ahora(),true))
        val iniciales = listOf(
            Conteo("CNT-1048","SUC-01","CAF-001",48,44,"Faltan 4 unidades en estantería.","OPR-017",ahora(),"PENDIENTE",0,"","","",""),
            Conteo("CNT-1049","SUC-01","LEC-004",36,38,"Recuento físico de prueba.","OPR-019",ahora(),"PENDIENTE",0,"","","",""),
            Conteo("CNT-1050","SUC-02","ARR-002",40,39,"Verificar caja de prueba.","OPR-019",ahora(),"PENDIENTE",0,"","","",""),
            Conteo("CNT-1051","SUC-02","CAF-001",24,26,"Recuento de prueba en bodega.","OPR-019",ahora(),"PENDIENTE",0,"","","", ""))
        val eventos = iniciales.map { c -> Evento(c.id,c.tiendaId,c.sku,"CONTEO","Conteo físico",c.diferencia,c.esperado,c.esperado,
            c.observacion,c.id,c.actorId,c.instanteUtc,"PENDIENTE") }
        return Snapshot(sucursales,existencias,emptyList(),iniciales,emptyList(),eventos,ahora())
    }
}
