package com.duoc.appmultitienda.gestion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.collectAsState
import com.duoc.appmultitienda.gestion.model.FakeLoginRepository
import com.duoc.appmultitienda.gestion.viewmodel.LoginViewModel

@Composable
fun GestionEntry(
    loginKey: String = "javier-login-default",
    onSessionStarted: (com.duoc.appmultitienda.gestion.model.UsuarioSesion) -> Unit = {}
) {
    val vm: LoginViewModel = viewModel(key = loginKey, factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LoginViewModel(FakeLoginRepository()) as T
    })
    val estado by vm.uiState.collectAsState()

    LaunchedEffect(estado.sesion) {
        estado.sesion?.let(onSessionStarted)
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (estado.sesion != null) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("MultiTienda", style = MaterialTheme.typography.headlineMedium)
                    Text("Acceso concedido: ${estado.sesion!!.rol.etiqueta}")
                    Text("Identificador: ${estado.sesion!!.identificador}")
                    Text("Módulo de ${estado.sesion!!.rol.etiqueta}: próxima etapa")
                    Button(onClick = vm::cerrarSesion) { Text("Cerrar sesión") }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                ) {
                    Text("MultiTienda", style = MaterialTheme.typography.headlineLarge)
                    Text("Inicia sesión con una cuenta de demostración")
                    OutlinedTextField(
                        value = estado.usuario, onValueChange = vm::cambiarUsuario,
                        label = { Text("Usuario") }, isError = estado.errorUsuario != null,
                        supportingText = { estado.errorUsuario?.let { Text(it) } },
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estado.clave, onValueChange = vm::cambiarClave,
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = estado.errorClave != null,
                        supportingText = { estado.errorClave?.let { Text(it) } },
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    estado.errorGeneral?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(
                        onClick = vm::iniciarSesion, enabled = !estado.cargando,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (estado.cargando) "Validando…" else "Iniciar sesión") }
                    Text("Pruebas: gerencia / demo123 o admin / demo123", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
