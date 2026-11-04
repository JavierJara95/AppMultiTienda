package com.duoc.appmultitienda.operador

import android.content.Context
import android.content.Intent

import com.duoc.appmultitienda.operador.ui.OperadorPaso1DemoActivity


// =====================================================
// RESULTADO DEL ACCESO AL MÓDULO
// =====================================================

sealed class ResultadoAccesoOperador {

    data object AccesoPermitido : ResultadoAccesoOperador()

    data class AccesoDenegado(
        val mensaje: String
    ) : ResultadoAccesoOperador()
}


// =====================================================
// CONTRATO DE INTEGRACIÓN DE MULTITIENDA
// =====================================================

object IntegracionModuloOperador {

    const val ROL_OPERADOR = "OPERADOR"

    const val EXTRA_OPERADOR_ID =
        "com.duoc.appmultitienda.OPERADOR_ID"

    const val EXTRA_OPERADOR_NOMBRE =
        "com.duoc.appmultitienda.OPERADOR_NOMBRE"

    const val EXTRA_OPERADOR_ROL =
        "com.duoc.appmultitienda.OPERADOR_ROL"


    // =================================================
    // VALIDAR LOS DATOS RECIBIDOS
    // =================================================

    fun validarAcceso(
        operadorId: String,
        nombre: String,
        rol: String
    ): ResultadoAccesoOperador {

        if (operadorId.isBlank()) {

            return ResultadoAccesoOperador.AccesoDenegado(
                "No se recibió el identificador del operador."
            )
        }

        if (nombre.isBlank()) {

            return ResultadoAccesoOperador.AccesoDenegado(
                "No se recibió el nombre del operador."
            )
        }

        if (!rol.equals(ROL_OPERADOR, ignoreCase = true)) {

            return ResultadoAccesoOperador.AccesoDenegado(
                "El usuario no tiene asignado el rol OPERADOR."
            )
        }

        return ResultadoAccesoOperador.AccesoPermitido
    }


    // =================================================
    // CREAR INTENT DEL MÓDULO
    // =================================================

    fun crearIntent(
        context: Context,
        operadorId: String,
        nombre: String,
        rol: String
    ): Intent {

        return Intent(
            context,
            OperadorPaso1DemoActivity::class.java
        ).apply {

            putExtra(
                EXTRA_OPERADOR_ID,
                operadorId
            )

            putExtra(
                EXTRA_OPERADOR_NOMBRE,
                nombre
            )

            putExtra(
                EXTRA_OPERADOR_ROL,
                rol
            )

            if (context !is android.app.Activity) {

                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }
        }
    }


    // =================================================
    // ABRIR MÓDULO DEL OPERADOR
    // =================================================

    fun abrirModulo(
        context: Context,
        operadorId: String,
        nombre: String,
        rol: String
    ): ResultadoAccesoOperador {

        val validacion = validarAcceso(
            operadorId = operadorId,
            nombre = nombre,
            rol = rol
        )

        if (
            validacion !is
                    ResultadoAccesoOperador.AccesoPermitido
        ) {
            return validacion
        }

        return try {

            val intent = crearIntent(
                context = context,
                operadorId = operadorId.trim(),
                nombre = nombre.trim(),
                rol = ROL_OPERADOR
            )

            context.startActivity(intent)

            ResultadoAccesoOperador.AccesoPermitido

        } catch (e: Exception) {

            ResultadoAccesoOperador.AccesoDenegado(
                "No fue posible abrir el módulo del operador."
            )
        }
    }
}