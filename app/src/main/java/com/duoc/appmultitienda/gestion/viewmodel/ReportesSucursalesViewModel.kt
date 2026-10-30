package com.duoc.appmultitienda.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.appmultitienda.gestion.model.*
import com.duoc.appmultitienda.gestion.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReportesSucursalesViewModel(
    private val repository: ReportesSucursalesRepository = FakeReportesSucursalesRepository()
) : ViewModel() {
    val sucursales = repository.sucursales
    private val _seleccion = MutableStateFlow<String?>(null)
    val seleccion: StateFlow<String?> = _seleccion
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje
    private val _filas = MutableStateFlow<List<FilaReporte>>(emptyList())
    val filas: StateFlow<List<FilaReporte>> = _filas

    init { actualizarReporte() }
    fun seleccionar(id: String?) { _seleccion.value = id; actualizarReporte() }
    fun actualizarReporte() = viewModelScope.launch {
        _filas.value = repository.obtenerMovimientos(_seleccion.value)
    }
    fun cambiarEstado(id: String, activa: Boolean) = viewModelScope.launch {
        _mensaje.value = if (repository.cambiarEstado(id, activa)) "Estado de sucursal actualizado (simulación)" else "Sucursal no encontrada"
    }
    fun notificarExportacion(resultado: String) { _mensaje.value = resultado }
    fun limpiarMensaje() { _mensaje.value = null }
    fun csvActual(): String = CsvExportador.generar(_filas.value)
}
