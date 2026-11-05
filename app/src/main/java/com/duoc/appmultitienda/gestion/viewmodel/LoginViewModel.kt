package com.duoc.appmultitienda.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.appmultitienda.gestion.model.LoginRepository
import com.duoc.appmultitienda.gestion.model.UsuarioSesion
import com.duoc.appmultitienda.gestion.validation.LoginValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val errorUsuario: String? = null,
    val errorClave: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val sesion: UsuarioSesion? = null
)

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun cambiarUsuario(valor: String) = _uiState.update {
        it.copy(usuario = valor, errorUsuario = null, errorGeneral = null)
    }

    fun cambiarClave(valor: String) = _uiState.update {
        it.copy(clave = valor, errorClave = null, errorGeneral = null)
    }

    fun iniciarSesion() {
        if (_uiState.value.cargando) return
        val estado = _uiState.value
        val errores = LoginValidator.validar(estado.usuario, estado.clave)
        if (errores.hayErrores) {
            _uiState.update { it.copy(errorUsuario = errores.usuario, errorClave = errores.clave) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null) }
            try {
                // Animación visible durante 3000 ms antes de mostrar el resultado.
                delay(3000L)
                val sesion = repository.iniciarSesion(estado.usuario, estado.clave)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        sesion = sesion,
                        errorGeneral = if (sesion == null) "Usuario o contraseña incorrectos" else null,
                        clave = if (sesion != null) "" else it.clave
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(cargando = false, errorGeneral = "No fue posible iniciar sesión") }
            }
        }
    }

    fun cerrarSesion() = _uiState.update { LoginUiState(usuario = it.usuario) }
}
