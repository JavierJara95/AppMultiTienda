package com.duoc.appmultitienda.operador.validation

import com.duoc.appmultitienda.operador.model.SucursalOperador

object SeleccionSucursalValidator {

    fun validar(
        sucursal: SucursalOperador?
    ): String? {

        return when {

            sucursal == null ->
                "Debes seleccionar una tienda."

            !sucursal.activa ->
                "La tienda seleccionada está inactiva."

            !sucursal.asignada ->
                "No tienes autorización para esta tienda."

            else -> null
        }
    }
}