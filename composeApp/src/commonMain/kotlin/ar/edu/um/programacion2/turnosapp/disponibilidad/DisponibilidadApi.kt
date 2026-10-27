package ar.edu.um.programacion2.turnosapp.disponibilidad

import ar.edu.um.programacion2.turnosapp.red.ResultadoApi
import ar.edu.um.programacion2.turnosapp.red.llamar
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.LocalDate

/** Disponibilidad de turnos-reservas-service: cruza la agenda del catálogo con las ocupaciones. */
class DisponibilidadApi(private val cliente: HttpClient) {

    suspend fun consultar(profesionalId: Long, fecha: LocalDate): ResultadoApi<Disponibilidad> =
        llamar({
            cliente.get("/api/disponibilidad") {
                parameter("profesionalId", profesionalId)
                parameter("fecha", fecha)
            }
        }) { it.body() }
}
