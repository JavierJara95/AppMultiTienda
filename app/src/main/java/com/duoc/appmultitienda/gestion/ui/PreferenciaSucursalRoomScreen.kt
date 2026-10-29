package com.duoc.appmultitienda.gestion.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.appmultitienda.gestion.local.GestionLocalDatabase
import com.duoc.appmultitienda.gestion.repository.PreferenciaSucursalRepository
import com.duoc.appmultitienda.gestion.viewmodel.PreferenciaSucursalViewModel

@Composable
fun PreferenciaSucursalRoomEntry() {
    val contexto = LocalContext.current.applicationContext
    val fabrica = remember(contexto) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val dao = GestionLocalDatabase.obtener(contexto).preferenciaSucursalDao()
                return PreferenciaSucursalViewModel(PreferenciaSucursalRepository(dao)) as T
            }
        }
    }
    val vm: PreferenciaSucursalViewModel = viewModel(factory = fabrica)
    PreferenciaSucursalRoomScreen(vm)
}

@Composable
fun PreferenciaSucursalRoomScreen(vm: PreferenciaSucursalViewModel) {
    val estado by vm.estado.collectAsState()
    var codigo by remember { mutableStateOf("SUC-01") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("MultiTienda · Prueba local Room", style = MaterialTheme.typography.headlineSmall)
        Text("Solo guarda una sucursal preferida, no modifica inventario oficial.")
        if (estado.cargando) CircularProgressIndicator()
        else Text("Guardada: ${estado.codigoActual ?: "ninguna"}")
        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it },
            label = { Text("Código sucursal (ej.: SUC-01)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = codigo.isBlank(),
            supportingText = { if (codigo.isBlank()) Text("El código no puede estar vacío") }
        )
        Button(onClick = { vm.guardar(codigo) }, enabled = codigo.isNotBlank()) {
            Text("Guardar en Room")
        }
        OutlinedButton(onClick = { vm.borrar() }) { Text("Borrar preferencia") }
        estado.mensaje?.let { Text(it) }
    }
}
