package com.duoc.appmultitienda.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.appmultitienda.gestion.repository.PreferenciaSucursalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PreferenciaSucursalUiState(
    val codigoActual: String? = null,
    val cargando: Boolean = true,
    val mensaje: String? = null
)

class PreferenciaSucursalViewModel(
    private val repositorio: PreferenciaSucursalRepository
) : ViewModel() {
    private val _estado = MutableStateFlow(PreferenciaSucursalUiState())
    val estado = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            repositorio.sucursalGuardada.collect { codigo ->
                _estado.value = _estado.value.copy(codigoActual = codigo, cargando = false)
            }
        }
    }

    fun guardar(codigo: String) {
        viewModelScope.launch {
            try {
                repositorio.guardar(codigo)
                _estado.value = _estado.value.copy(mensaje = "Sucursal guardada localmente")
            } catch (e: IllegalArgumentException) {
                _estado.value = _estado.value.copy(mensaje = e.message)
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(mensaje = "Error al guardar la preferencia")
            }
        }
    }

    fun borrar() {
        viewModelScope.launch {
            try {
                repositorio.borrar()
                _estado.value = _estado.value.copy(mensaje = "Preferencia eliminada")
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(mensaje = "Error al borrar la preferencia")
            }
        }
    }
}
