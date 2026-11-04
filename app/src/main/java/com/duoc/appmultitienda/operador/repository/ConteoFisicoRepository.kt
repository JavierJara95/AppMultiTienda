package com.duoc.appmultitienda.operador.repository

import com.duoc.appmultitienda.operador.model.ConteoFisicoOperador
import com.duoc.appmultitienda.operador.model.EstadoConteoFisico

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import java.time.Instant
import java.util.UUID


// Define las operaciones disponibles para los conteos físicos.
interface ConteoFisicoRepository {

    suspend fun registrarConteo(
        conteo: ConteoFisicoOperador
    ): ConteoFisicoOperador

    suspend fun obtenerConteosPendientes(
        sucursalId: String
    ): List<ConteoFisicoOperador>
}


// Repositorio de demostración.
// Guarda datos temporalmente en memoria.
class FakeConteoFisicoRepository : ConteoFisicoRepository {

    private val conteos = mutableListOf<ConteoFisicoOperador>()

    private val mutex = Mutex()


    // REGISTRAR UN CONTEO

    override suspend fun registrarConteo(
        conteo: ConteoFisicoOperador
    ): ConteoFisicoOperador {

        // Simulamos la espera de una respuesta.
        delay(500L)

        // Comprobamos que los datos sean válidos.
        require(conteo.stockRegistrado >= 0) {
            "El stock registrado no puede ser negativo."
        }

        require(conteo.cantidadContada >= 0) {
            "La cantidad contada no puede ser negativa."
        }

        require(conteo.sucursalId.isNotBlank()) {
            "Debes indicar una sucursal."
        }

        require(conteo.codigoProducto.isNotBlank()) {
            "Debes indicar un producto."
        }

        // Una diferencia debe tener una explicación.
        if (conteo.tieneDiferencia) {

            require(conteo.observacion.trim().length >= 5) {
                "Debes explicar la diferencia encontrada."
            }
        }

        // Creamos una nueva solicitud.
        val solicitud = conteo.copy(

            id = "DEMO-CON-" +
                    UUID.randomUUID()
                        .toString()
                        .take(8)
                        .uppercase(),

            estado = EstadoConteoFisico.PENDIENTE_APROBACION,

            fechaUtc = Instant.now().toString()
        )

        // Guardamos la solicitud en memoria.
        mutex.withLock {

            conteos.add(solicitud)
        }

        // Devolvemos el conteo registrado.
        return solicitud
    }


    // CONSULTAR LOS CONTEOS PENDIENTES DE UNA SUCURSAL

    override suspend fun obtenerConteosPendientes(
        sucursalId: String
    ): List<ConteoFisicoOperador> {

        return mutex.withLock {

            conteos.filter { conteo ->

                conteo.sucursalId == sucursalId &&

                        conteo.estado ==
                        EstadoConteoFisico.PENDIENTE_APROBACION
            }
        }
    }
}