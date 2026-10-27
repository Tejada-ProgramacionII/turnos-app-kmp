package ar.edu.um.programacion2.turnosapp.disponibilidad

import ar.edu.um.programacion2.turnosapp.red.ResultadoApi
import ar.edu.um.programacion2.turnosapp.red.crearHttpClient
import ar.edu.um.programacion2.turnosapp.sesion.SessionStore
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class DisponibilidadApiTest {

    private val sesion = SessionStore().apply { iniciar("token-123") }
    private val fecha = LocalDate(2026, 11, 3)
    private var pedido: HttpRequestData? = null

    private fun api(status: HttpStatusCode, cuerpo: String, tipo: String = "application/json") =
        DisponibilidadApi(
            crearHttpClient(
                MockEngine { request ->
                    pedido = request
                    respond(cuerpo, status, headersOf(HttpHeaders.ContentType, tipo))
                },
                "http://turnos",
                sesion,
            ),
        )

    private fun problema(status: HttpStatusCode, detalle: String) =
        api(status, """{"title":"x","status":${status.value},"detail":"$detalle"}""", "application/problem+json")

    @Test
    fun consultarDevuelveLosTurnosLibres() = runTest {
        val cuerpo = """
            {"profesionalId":7,"fecha":"2026-11-03",
             "turnos":[{"horaInicio":"09:00:00","horaFin":"09:30:00"},{"horaInicio":"10:00:00","horaFin":"10:30:00"}]}
        """.trimIndent()

        val resultado = api(HttpStatusCode.OK, cuerpo).consultar(7, fecha)

        assertEquals("/api/disponibilidad", pedido!!.url.encodedPath)
        assertEquals("7", pedido!!.url.parameters["profesionalId"])
        assertEquals("2026-11-03", pedido!!.url.parameters["fecha"])
        assertEquals("Bearer token-123", pedido!!.headers[HttpHeaders.Authorization])
        assertEquals(
            ResultadoApi.Exito(
                Disponibilidad(
                    7,
                    fecha,
                    listOf(
                        TurnoLibre(LocalTime(9, 0), LocalTime(9, 30)),
                        TurnoLibre(LocalTime(10, 0), LocalTime(10, 30)),
                    ),
                ),
            ),
            resultado,
        )
    }

    @Test
    fun sinTurnosLibresDevuelveListaVacia() = runTest {
        val resultado = api(HttpStatusCode.OK, """{"profesionalId":7,"fecha":"2026-11-03","turnos":[]}""").consultar(7, fecha)

        assertEquals(ResultadoApi.Exito(Disponibilidad(7, fecha, emptyList())), resultado)
    }

    @Test
    fun fechaPasada() = runTest {
        val resultado = problema(HttpStatusCode.BadRequest, "la fecha ya pasó").consultar(7, fecha)

        assertEquals(ResultadoApi.Invalido("la fecha ya pasó", emptyList()), resultado)
    }

    @Test
    fun profesionalInexistente() = runTest {
        val resultado = problema(HttpStatusCode.NotFound, "no existe el profesional 99").consultar(99, fecha)

        assertEquals(ResultadoApi.NoEncontrado("no existe el profesional 99"), resultado)
    }

    @Test
    fun profesionalDeshabilitado() = runTest {
        val resultado = problema(HttpStatusCode.Conflict, "el profesional 8 está deshabilitado").consultar(8, fecha)

        assertEquals(ResultadoApi.Conflicto("el profesional 8 está deshabilitado"), resultado)
    }

    @Test
    fun catalogoOCatedraNoDisponibles() = runTest {
        val resultado = problema(HttpStatusCode.ServiceUnavailable, "La disponibilidad no se puede consultar ahora")
            .consultar(7, fecha)

        assertEquals(ResultadoApi.NoDisponible("La disponibilidad no se puede consultar ahora"), resultado)
    }

    @Test
    fun errorDeIntegracion() = runTest {
        val resultado = problema(HttpStatusCode.BadGateway, "Error de integración al consultar la disponibilidad")
            .consultar(7, fecha)

        assertEquals(ResultadoApi.ErrorDeIntegracion("Error de integración al consultar la disponibilidad"), resultado)
    }

    @Test
    fun sinSesionValida() = runTest {
        val resultado = api(HttpStatusCode.Unauthorized, "").consultar(7, fecha)

        assertEquals(ResultadoApi.NoAutorizado(null), resultado)
    }
}
