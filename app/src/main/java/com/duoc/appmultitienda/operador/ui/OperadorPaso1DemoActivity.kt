package com.duoc.appmultitienda.operador.ui

import android.os.Bundle
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.duoc.appmultitienda.operador.validation.TipoMovimiento

import com.duoc.appmultitienda.operador.viewmodel.BusquedaProductoViewModel
import com.duoc.appmultitienda.operador.viewmodel.SeleccionSucursalViewModel
import com.duoc.appmultitienda.operador.viewmodel.MovimientoViewModel
import com.duoc.appmultitienda.operador.viewmodel.ConteoFisicoViewModel


class OperadorPaso1DemoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // ViewModel para seleccionar sucursales
        val sucursalViewModel = ViewModelProvider(this)[
            SeleccionSucursalViewModel::class.java
        ]

        // ViewModel para buscar productos
        val productoViewModel = ViewModelProvider(this)[
            BusquedaProductoViewModel::class.java
        ]

        // ViewModel para registrar movimientos
        val movimientoViewModel = ViewModelProvider(this)[
            MovimientoViewModel::class.java
        ]
        val conteoViewModel = ViewModelProvider(this)[
            ConteoFisicoViewModel::class.java
        ]

        setContent {

            // Sucursal donde trabaja el operador
            var sucursalId by rememberSaveable {
                mutableStateOf<String?>(null)
            }

            // Pantalla que se está mostrando
            var pantalla by rememberSaveable {
                mutableStateOf("seleccion")
            }

            // Producto seleccionado
            var productoCodigo by rememberSaveable {
                mutableStateOf<String?>(null)
            }

            // Indica por qué abrimos la búsqueda
            var origenBusqueda by rememberSaveable {
                mutableStateOf("consulta")
            }

            // Permite regresar a la pantalla correcta
            var origenMovimiento by rememberSaveable {
                mutableStateOf("ficha")
            }
            var origenConteo by rememberSaveable {
                mutableStateOf("ficha")
            }

            // Estado de las sucursales
            val sucursalesState by
            sucursalViewModel.uiState.collectAsStateWithLifecycle()

            // Estado de los productos
            val productosState by
            productoViewModel.uiState.collectAsStateWithLifecycle()

            val movimientoState by
            movimientoViewModel.uiState.collectAsStateWithLifecycle()

            // Obtener sucursal activa
            val sucursal = sucursalesState.sucursales.find {
                it.id == sucursalId
            }

            // Obtener producto seleccionado
            val producto = productosState.productos.find {
                it.codigo == productoCodigo &&
                        it.sucursalId == sucursalId
            }

            // Función para abrir la búsqueda
            fun abrirBusqueda(origen: String) {

                productoViewModel.limpiarBusqueda()

                productoCodigo = null

                origenBusqueda = origen

                pantalla = "busqueda"
            }

            // Función para abrir el formulario
            fun abrirMovimiento(
                tipo: TipoMovimiento,
                origen: String
            ) {

                movimientoViewModel.reiniciarFormulario(tipo)

                origenMovimiento = origen

                pantalla = "movimiento"
            }

            // NAVEGACIÓN

            when {

                // 1. SELECCIÓN DE TIENDA

                pantalla == "seleccion" -> {

                    SeleccionSucursalScreen(
                        viewModel = sucursalViewModel,

                        onContinuar = { id ->

                            sucursalId = id

                            productoCodigo = null

                            pantalla = "inicio"
                        }
                    )
                }

                // 2. UBICACIÓN NO DISPONIBLE

                sucursal == null -> {

                    LaunchedEffect(sucursalId, pantalla) {

                        productoCodigo = null

                        pantalla = "seleccion"
                    }
                }

                // 3. INICIO DEL OPERADOR

                pantalla == "inicio" -> {

                    InicioOperadorScreen(

                        nombreSucursal = sucursal.nombre,

                        onCambiarUbicacion = {

                            // Reiniciar selección anterior
                            sucursalViewModel.reiniciarSeleccion()

                            // Limpiar resultados
                            productoViewModel.limpiarBusqueda()

                            // Limpiar formulario
                            movimientoViewModel.reiniciarFormulario()

                            conteoViewModel.reiniciarConteo()

                            // Eliminar sucursal activa
                            sucursalId = null

                            productoCodigo = null

                            pantalla = "seleccion"
                        },

                        onEscanear = {

                            abrirBusqueda("consulta")
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

                            mostrarMensaje(
                                "Historial: módulo de Javier"
                            )
                        },

                        onPerfil = {

                            mostrarMensaje(
                                "Perfil: pendiente de integración"
                            )
                        }
                    )
                }

                // 4. BÚSQUEDA DE PRODUCTOS

                pantalla == "busqueda" -> {

                    BusquedaProductoScreen(

                        sucursalId = sucursal.id,

                        nombreSucursal = sucursal.nombre,

                        viewModel = productoViewModel,

                        onVolver = {

                            pantalla = "inicio"
                        },

                        onProductoSeleccionado = { seleccionado ->

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

                // 5. FICHA DEL PRODUCTO

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

                            mostrarMensaje(
                                "Historial: módulo de Javier"
                            )
                        }
                    )
                }
                // PANTALLA DE CONTEO FÍSICO

                pantalla == "conteo" && producto != null -> {

                    ConteoFisicoScreen(

                        producto = producto,

                        nombreSucursal = sucursal.nombre,

                        viewModel = conteoViewModel,

                        onVolver = {

                            pantalla = origenConteo
                        }
                    )
                }

                // 6. FORMULARIO DE MOVIMIENTOS

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
                // PANTALLA DE REVISIÓN DEL MOVIMIENTO

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
                // PANTALLA DEL COMPROBANTE

                pantalla == "comprobante" -> {

                    val comprobante = movimientoState.comprobanteDemo

                    if (comprobante != null) {

                        ComprobanteMovimientoDemoScreen(

                            comprobante = comprobante,

                            nombreSucursal = sucursal.nombre,

                            onVolverInicio = {

                                movimientoViewModel.reiniciarFormulario()

                                productoCodigo = null

                                pantalla = "inicio"
                            }
                        )

                    } else {

                        LaunchedEffect(Unit) {
                            pantalla = "inicio"
                        }
                    }
                }
                // PRODUCTO NO DISPONIBLE

                pantalla == "ficha" ||
                        pantalla == "movimiento" ||
                        pantalla == "revision" ||
                        pantalla == "conteo" -> {

                    LaunchedEffect(pantalla, productoCodigo) {

                        productoCodigo = null

                        pantalla = "busqueda"
                    }
                }
                // 8. RUTA DESCONOCIDA

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