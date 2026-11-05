package com.duoc.appmultitienda.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.appmultitienda.core.data.InventarioGateway
import com.duoc.appmultitienda.core.model.FalloDatos
import com.duoc.appmultitienda.gestion.validation.GestionValidator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AccesoUi(
    val usuario: String = "",
    val clave: String = "",
    val recordar: Boolean = true,
    val errores: Map<String, String> = emptyMap(),
    val mensaje: String? = null,
    val ocupado: Boolean = false,
)

class SesionViewModel(private val repo: InventarioGateway) : ViewModel() {
    val datos = repo.datos
    private val _acceso = MutableStateFlow(AccesoUi())
    val acceso = _acceso.asStateFlow()

    init {
        viewModelScope.launch {
            repo.preferencias.leer("usuario-recordado")?.let { nombre ->
                _acceso.update { it.copy(usuario = nombre) }
            }
        }
    }

    fun editar(usuario: String? = null, clave: String? = null, recordar: Boolean? = null) {
        if (_acceso.value.ocupado) return
        _acceso.update {
            it.copy(
                usuario = usuario ?: it.usuario,
                clave = clave ?: it.clave,
                recordar = recordar ?: it.recordar,
                errores = emptyMap(),
                mensaje = null,
            )
        }
    }

    fun login() {
        if (_acceso.value.ocupado) return
        val a = _acceso.value
        val errores = GestionValidator.login(a.usuario, a.clave)
        _acceso.update { it.copy(errores = errores) }
        if (errores.isNotEmpty()) return
        _acceso.update { it.copy(ocupado = true, mensaje = null) }
        viewModelScope.launch {
            try {
                // Animación de acceso: 3000 ms. No se cambian las credenciales ni permisos.
                delay(3_000L)
                repo.login(a.usuario, a.clave)
                if (a.recordar) repo.preferencias.escribir("usuario-recordado", a.usuario.trim())
                else repo.preferencias.borrar("usuario-recordado")
                _acceso.update { it.copy(clave = "") }
            } catch (e: FalloDatos) {
                _acceso.update { it.copy(mensaje = e.message) }
            } finally {
                _acceso.update { it.copy(ocupado = false) }
            }
        }
    }

    fun refrescar() =
        viewModelScope.launch {
            try {
                repo.refrescar()
            } catch (_: FalloDatos) {}
        }

    fun seleccionar(id: String, listo: () -> Unit) =
        viewModelScope.launch {
            try {
                repo.seleccionar(id)
                listo()
            } catch (_: FalloDatos) {}
        }

    fun logout() =
        viewModelScope.launch {
            repo.logout()
            _acceso.update { it.copy(clave = "", mensaje = null) }
        }
}
