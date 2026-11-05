package com.duoc.appmultitienda.gestion.validation

object DecisionValidator {
    fun errorMotivo(motivo: String): String? = when {
        motivo.trim().length < 5 -> "El motivo debe tener al menos 5 caracteres"
        motivo.trim().length > 200 -> "El motivo no puede superar 200 caracteres"
        else -> null
    }
}
