package com.duoc.appmultitienda.operador.viewmodel

import com.duoc.appmultitienda.operador.model.SucursalOperador
import com.duoc.appmultitienda.operador.repository.SucursalOperadorRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeleccionSucursalViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val alameda = SucursalOperador(
        id = "SUC-01",
        nombre = "Tienda Alameda",
        direccion = "Av. Alameda 120",
        activa = true,
        asignada = true
    )

    private val norte = SucursalOperador(
        id = "SUC-02",
        nombre = "Bodega Norte",
        direccion = "Parque Industrial 45",
        activa = true,
        asignada = true
    )

    private val mirador = SucursalOperador(
        id = "SUC-03",
        nombre = "Tienda Mirador",
        direccion = "Calle Mirador 86",
        activa = false,
        asignada = true
    )

    private val fakeRepository = object : SucursalOperadorRepository {

        override suspend fun obtenerSucursales(): List<SucursalOperador> {
            return listOf(alameda, norte, mirador)
        }
    }

    @Test
    fun cargarSucursalesObtieneTresTiendas() = runTest {

        val viewModel = SeleccionSucursalViewModel(fakeRepository)

        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.sucursales.size)
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun seleccionarAlamedaActualizaEstado() = runTest {

        val viewModel = SeleccionSucursalViewModel(fakeRepository)

        advanceUntilIdle()

        viewModel.seleccionarSucursal(alameda)

        assertEquals(
            "SUC-01",
            viewModel.uiState.value.sucursalSeleccionada?.id
        )
    }

    @Test
    fun seleccionarTiendaInactivaEsRechazado() = runTest {

        val viewModel = SeleccionSucursalViewModel(fakeRepository)

        advanceUntilIdle()

        viewModel.seleccionarSucursal(mirador)

        assertEquals(
            null,
            viewModel.uiState.value.sucursalSeleccionada
        )

        assertEquals(
            "La tienda seleccionada está inactiva.",
            viewModel.uiState.value.error
        )
    }

    @Test
    fun confirmarSinSeleccionMuestraError() = runTest {

        val viewModel = SeleccionSucursalViewModel(fakeRepository)

        advanceUntilIdle()

        viewModel.confirmarSucursal()

        assertFalse(viewModel.uiState.value.confirmada)

        assertEquals(
            "Debes seleccionar una tienda.",
            viewModel.uiState.value.error
        )
    }

    @Test
    fun confirmarAlamedaEsExitoso() = runTest {

        val viewModel = SeleccionSucursalViewModel(fakeRepository)

        advanceUntilIdle()

        viewModel.seleccionarSucursal(alameda)
        viewModel.confirmarSucursal()

        assertTrue(viewModel.uiState.value.confirmada)

        assertEquals(
            "SUC-01",
            viewModel.uiState.value.sucursalSeleccionada?.id
        )
    }
}