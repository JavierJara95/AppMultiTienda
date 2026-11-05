package com.duoc.appmultitienda.operador.ui

import android.os.Bundle
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.duoc.appmultitienda.core.integration.ProductoOperadorCompartido
import com.duoc.appmultitienda.core.integration.ConteosOperadorCompartidos
import com.duoc.appmultitienda.core.integration.MovimientosOperadorCompartidos
import com.duoc.appmultitienda.operador.IntegracionModuloOperador
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.duoc.appmultitienda.operador.validation.TipoMovimiento

import com.duoc.appmultitienda.operador.viewmodel.BusquedaProductoViewModel
import com.duoc.appmultitienda.operador.viewmodel.SeleccionSucursalViewModel
import com.duoc.appmultitienda.operador.viewmodel.MovimientoViewModel
import com.duoc.appmultitienda.operador.viewmodel.ConteoFisicoViewModel

import com.duoc.appmultitienda.operador.scanner.EscanerCodigoBarrasScreen
import com.duoc.appmultitienda.operador.scanner.CamaraLectorScreen
import com.duoc.appmultitienda.operador.scanner.ProductoEscaneadoMatcher


class OperadorPaso1DemoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // ==========================================
        // VIEWMODELS
        // ==========================================

        val sucursalViewModel = ViewModelProvider(this)[
            SeleccionSucursalViewModel::class.java
        ]

        val productoViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                BusquedaProductoViewModel(ProductoOperadorCompartido()) as T
        })[BusquedaProductoViewModel::class.java]

        val movimientoViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MovimientoViewModel(
                    MovimientosOperadorCompartidos(),
                    intent.getStringExtra(IntegracionModuloOperador.EXTRA_OPERADOR_ID) ?: "OPR-017"
                ) as T
        })[MovimientoViewModel::class.java]

        val conteoViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ConteoFisicoViewModel(ConteosOperadorCompartidos()) as T
        })[ConteoFisicoViewModel::class.java]

        // Perfil de demostración.
        val perfilOperador = PerfilOperadorDemoRepository.obtenerPerfil().copy(
            id = intent.getStringExtra(IntegracionModuloOperador.EXTRA_OPERADOR_ID) ?: "OPR-017",
            nombre = intent.getStringExtra(IntegracionModuloOperador.EXTRA_OPERADOR_NOMBRE)
                ?: "Operador MultiTienda"
        )

        setContent {

            // ==========================================
            // ESTADOS DE NAVEGACIÓN
            // ==========================================

            var sucursalId by rememberSaveable {
                mutableStateOf<String?>(null)
            }

            var pantalla by rememberSaveable {
                mutableStateOf("seleccion")
            }

            var productoCodigo by rememberSaveable {
                mutableStateOf<String?>(null)
            }

            var origenBusqueda by rememberSaveable {
                mutableStateOf("consulta")
            }

            var origenMovimiento by rememberSaveable {
                mutableStateOf("ficha")
            }

            var origenConteo by rememberSaveable {
                mutableStateOf("ficha")
            }

            var origenHistorial by rememberSaveable {
                mutableStateOf("inicio")
            }

            var codigoEscaneadoPendiente by rememberSaveable {
                mutableStateOf<String?>(null)
            }

            var sucursalEscaneoPendiente by rememberSaveable {
                mutableStateOf<String?>(null)
            }

            // ==========================================
            // VIEWMODELS
            // ==========================================

            val sucursalesState by
            sucursalViewModel.uiState.collectAsStateWithLifecycle()

            val productosState by
            productoViewModel.uiState.collectAsStateWithLifecycle()

            val movimientoState by
            movimientoViewModel.uiState.collectAsStateWithLifecycle()

            // ==========================================
            // REGISTRAR HISTORIAL
            // ==========================================

            val comprobanteActual =
                movimientoState.comprobanteDemo

            LaunchedEffect(comprobanteActual?.idDemostracion) {

                if (comprobanteActual != null) {

                    HistorialOperadorStore.registrar(
                        comprobanteActual
                    )
                }
            }

            // ==========================================
            // SUCURSAL Y PRODUCTO
            // ==========================================

            val sucursal = sucursalesState.sucursales.find {
                it.id == sucursalId
            }

            val producto = productosState.productos.find {
                it.codigo == productoCodigo &&
                        it.sucursalId == sucursalId
            }

            // ==========================================
            // FUNCIONES AUXILIARES
            // ==========================================

            fun limpiarEscaneoPendiente() {

                codigoEscaneadoPendiente = null
                sucursalEscaneoPendiente = null
            }

            fun abrirBusqueda(origen: String) {

                limpiarEscaneoPendiente()

                productoViewModel.limpiarBusqueda()

                productoCodigo = null

                origenBusqueda = origen

                pantalla = "busqueda"
            }

            fun abrirMovimiento(
                tipo: TipoMovimiento,
                origen: String
            ) {

                movimientoViewModel.reiniciarFormulario(tipo)

                origenMovimiento = origen

                pantalla = "movimiento"
            }

            fun abrirHistorial(origen: String) {

                origenHistorial = origen

                pantalla = "historial"
            }

            fun cambiarSucursal() {

                sucursalViewModel.reiniciarSeleccion()

                productoViewModel.limpiarBusqueda()

                movimientoViewModel.reiniciarFormulario()

                conteoViewModel.reiniciarConteo()

                sucursalId = null

                productoCodigo = null

                limpiarEscaneoPendiente()

                pantalla = "seleccion"
            }

            // ==========================================
            // NAVEGACIÓN PRINCIPAL
            // ==========================================

            when {

                // ======================================
                // SELECCIÓN DE SUCURSAL
                // ======================================

                pantalla == "seleccion" -> {

                    SeleccionSucursalScreen(

                        viewModel = sucursalViewModel,

                        onContinuar = { id ->

                            sucursalId = id

                            productoCodigo = null

                            limpiarEscaneoPendiente()

                            pantalla = "inicio"
                        }
                    )
                }

                // ======================================
                // SUCURSAL NO DISPONIBLE
                // ======================================

                sucursal == null -> {

                    LaunchedEffect(sucursalId, pantalla) {

                        productoCodigo = null

                        limpiarEscaneoPendiente()

                        pantalla = "seleccion"
                    }
                }

                // ======================================
                // INICIO
                // ======================================

                pantalla == "inicio" -> {

                    InicioOperadorScreen(

                        nombreSucursal = sucursal.nombre,

                        operadorId = perfilOperador.id,

                        onCambiarUbicacion = {

                            cambiarSucursal()
                        },

                        onEscanear = {

                            limpiarEscaneoPendiente()

                            pantalla = "escaner"
                        },

                        onConteo = {

                            abrirBusqueda("conteo")
                        },

                        onEntrada = {

                            abrirBusqueda("entrada")
                        },

                        onSalida = {

                            abrirBusqueda("salida")
                        },

                        onHistorial = {

                            abrirHistorial("inicio")
                        },

                        onPerfil = {

                            pantalla = "perfil"
                        }
                    )
                }

                // ======================================
                // PERFIL DEL OPERADOR
                // ======================================

                pantalla == "perfil" -> {

                    PerfilOperadorScreen(

                        nombreSucursal = sucursal.nombre,

                        perfil = perfilOperador,

                        onVolver = {

                            pantalla = "inicio"
                        },

                        onCambiarSucursal = {

                            cambiarSucursal()
                        },

                        onAbrirHistorial = {

                            abrirHistorial("perfil")
                        }
                    )
                }

                // ======================================
                // PERMISO DE CÁMARA
                // ======================================

                pantalla == "escaner" -> {

                    EscanerCodigoBarrasScreen(

                        nombreSucursal = sucursal.nombre,

                        onVolver = {

                            pantalla = "inicio"
                        },

                        onAbrirCamara = {

                            limpiarEscaneoPendiente()

                            pantalla = "camara"
                        },

                        onBusquedaManual = {

                            abrirBusqueda("consulta")
                        }
                    )
                }

                // ======================================
                // CÁMARA
                // ======================================

                pantalla == "camara" -> {

                    CamaraLectorScreen(

                        nombreSucursal = sucursal.nombre,

                        onVolver = {

                            pantalla = "escaner"
                        },

                        onCodigoEscaneado = { codigo ->

                            val codigoLimpio = codigo.trim()

                            if (codigoLimpio.isBlank()) {

                                mostrarMensaje(
                                    "El código escaneado no es válido."
                                )

                            } else {

                                productoViewModel.limpiarBusqueda()

                                productoCodigo = null

                                codigoEscaneadoPendiente =
                                    codigoLimpio

                                sucursalEscaneoPendiente =
                                    sucursal.id

                                origenBusqueda = "consulta"

                                productoViewModel.buscarCodigoEscaneado(
                                    codigo = codigoLimpio,
                                    sucursalId = sucursal.id
                                )

                                pantalla = "busqueda"
                            }
                        }
                    )
                }

                // ======================================
                // BÚSQUEDA DE PRODUCTOS
                // ======================================

                pantalla == "busqueda" -> {

                    LaunchedEffect(
                        codigoEscaneadoPendiente,
                        sucursalEscaneoPendiente,
                        sucursal.id,
                        productosState.consulta,
                        productosState.busquedaRealizada,
                        productosState.cargando,
                        productosState.productos
                    ) {

                        val codigoPendiente =
                            codigoEscaneadoPendiente

                        val sucursalPendiente =
                            sucursalEscaneoPendiente

                        if (
                            codigoPendiente != null &&
                            sucursalPendiente == sucursal.id &&
                            productosState.consulta.trim() ==
                            codigoPendiente &&
                            productosState.busquedaRealizada &&
                            !productosState.cargando
                        ) {

                            val encontrado =
                                ProductoEscaneadoMatcher.buscar(

                                    codigoEscaneado = codigoPendiente,

                                    sucursalId = sucursal.id,

                                    productos = productosState.productos
                                )

                            limpiarEscaneoPendiente()

                            if (encontrado != null) {

                                productoCodigo = encontrado.codigo

                                pantalla = "ficha"

                            } else {

                                mostrarMensaje(
                                    "Producto no encontrado en " +
                                            sucursal.nombre +
                                            ". Puedes buscarlo manualmente."
                                )
                            }
                        }
                    }

                    BusquedaProductoScreen(

                        sucursalId = sucursal.id,

                        nombreSucursal = sucursal.nombre,

                        viewModel = productoViewModel,

                        onVolver = {

                            limpiarEscaneoPendiente()

                            pantalla = "inicio"
                        },

                        onProductoSeleccionado = { seleccionado ->

                            limpiarEscaneoPendiente()

                            productoCodigo = seleccionado.codigo

                            when (origenBusqueda) {

                                "entrada" -> {

                                    abrirMovimiento(
                                        TipoMovimiento.ENTRADA,
                                        "busqueda"
                                    )
                                }

                                "salida" -> {

                                    abrirMovimiento(
                                        TipoMovimiento.SALIDA,
                                        "busqueda"
                                    )
                                }

                                "conteo" -> {

                                    conteoViewModel.reiniciarConteo()

                                    origenConteo = "busqueda"

                                    pantalla = "conteo"
                                }

                                else -> {

                                    pantalla = "ficha"
                                }
                            }
                        }
                    )
                }

                // ======================================
                // FICHA DEL PRODUCTO
                // ======================================

                pantalla == "ficha" && producto != null -> {

                    FichaProductoScreen(

                        producto = producto,

                        nombreSucursal = sucursal.nombre,

                        onVolver = {

                            pantalla = "busqueda"
                        },

                        onConteo = {

                            conteoViewModel.reiniciarConteo()

                            origenConteo = "ficha"

                            pantalla = "conteo"
                        },

                        onMovimiento = {

                            abrirMovimiento(
                                TipoMovimiento.ENTRADA,
                                "ficha"
                            )
                        },

                        onHistorial = {

                            abrirHistorial("ficha")
                        }
                    )
                }

                // ======================================
                // HISTORIAL
                // ======================================

                pantalla == "historial" -> {

                    HistorialOperadorScreen(

                        sucursalId = sucursal.id,

                        nombreSucursal = sucursal.nombre,

                        onVolver = {

                            pantalla = origenHistorial
                        }
                    )
                }

                // ======================================
                // CONTEO FÍSICO
                // ======================================

                pantalla == "conteo" && producto != null -> {

                    ConteoFisicoScreen(

                        producto = producto,

                        nombreSucursal = sucursal.nombre,

                        viewModel = conteoViewModel,

                        operadorId = perfilOperador.id,

                        onVolver = {

                            pantalla = origenConteo
                        }
                    )
                }

                // ======================================
                // MOVIMIENTOS
                // ======================================

                pantalla == "movimiento" && producto != null -> {

                    MovimientoScreen(

                        producto = producto,

                        nombreSucursal = sucursal.nombre,

                        viewModel = movimientoViewModel,

                        onVolver = {

                            pantalla = origenMovimiento
                        },

                        onAbrirRevision = {

                            pantalla = "revision"
                        }
                    )
                }

                // ======================================
                // REVISIÓN
                // ======================================

                pantalla == "revision" && producto != null -> {

                    RevisionMovimientoScreen(

                        producto = producto,

                        nombreSucursal = sucursal.nombre,

                        viewModel = movimientoViewModel,

                        onVolverEditar = {

                            pantalla = "movimiento"
                        },

                        onComprobante = {

                            pantalla = "comprobante"
                        }
                    )
                }

                // ======================================
                // COMPROBANTE
                // ======================================

                pantalla == "comprobante" -> {

                    val comprobante =
                        movimientoState.comprobanteDemo

                    if (comprobante != null) {

                        ComprobanteMovimientoDemoScreen(

                            comprobante = comprobante,

                            nombreSucursal = sucursal.nombre,

                            onVolverInicio = {

                                movimientoViewModel
                                    .reiniciarFormulario()

                                productoCodigo = null

                                limpiarEscaneoPendiente()

                                pantalla = "inicio"
                            }
                        )

                    } else {

                        LaunchedEffect(Unit) {
                            pantalla = "inicio"
                        }
                    }
                }

                // ======================================
                // PRODUCTO NO DISPONIBLE
                // ======================================

                pantalla == "ficha" ||
                        pantalla == "movimiento" ||
                        pantalla == "revision" ||
                        pantalla == "conteo" -> {

                    LaunchedEffect(
                        pantalla,
                        productoCodigo
                    ) {

                        productoCodigo = null

                        limpiarEscaneoPendiente()

                        pantalla = "busqueda"
                    }
                }

                // ======================================
                // RUTA DESCONOCIDA
                // ======================================

                else -> {

                    LaunchedEffect(pantalla) {
                        pantalla = "inicio"
                    }
                }
            }
        }
    }

    private fun mostrarMensaje(mensaje: String) {

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_SHORT
        ).show()
    }
}