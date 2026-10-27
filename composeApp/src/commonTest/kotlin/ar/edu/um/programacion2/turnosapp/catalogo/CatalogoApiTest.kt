package ar.edu.um.programacion2.turnosapp.catalogo

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
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class CatalogoApiTest {

    private val sesion = SessionStore().apply { iniciar("token-123") }
    private var pedido: HttpRequestData? = null

    private fun api(status: HttpStatusCode, cuerpo: String, tipo: String = "application/json") =
        CatalogoApi(
            crearHttpClient(
                MockEngine { request ->
                    pedido = request
                    respond(cuerpo, status, headersOf(HttpHeaders.ContentType, tipo))
                },
                "http://catalogo",
                sesion,
            ),
        )

    private fun problema(status: HttpStatusCode, detalle: String) =
        api(status, """{"title":"x","status":${status.value},"detail":"$detalle"}""", "application/problem+json")

    @Test
    fun buscarConTodosLosFiltros() = runTest {
        val cuerpo = """
            {"contenido":[{"id":7,"categoriaId":2,"nombre":"Ana","apellido":"Gómez","habilitado":true}],
             "numero":1,"tamanio":5,"totalElementos":6,"totalPaginas":2}
        """.trimIndent()

        val resultado = api(HttpStatusCode.OK, cuerpo).buscarProfesionales(
            categoriaId = 2,
            nombre = "ana",
            habilitado = true,
            fecha = LocalDate(2026, 11, 3),
            page = 1,
            size = 5,
        )

        val url = pedido!!.url
        assertEquals("/api/profesionales", url.encodedPath)
        assertEquals("2", url.parameters["categoriaId"])
        assertEquals("ana", url.parameters["nombre"])
        assertEquals("true", url.parameters["habilitado"])
        assertEquals("2026-11-03", url.parameters["fecha"])
        assertEquals("1", url.parameters["page"])
        assertEquals("5", url.parameters["size"])
        assertEquals("Bearer token-123", pedido!!.headers[HttpHeaders.Authorization])
        val pagina = assertIs<ResultadoApi.Exito<Pagina<Profesional>>>(resultado).valor
        assertEquals(listOf(Profesional(7, 2, "Ana", "Gómez", true)), pagina.contenido)
        assertEquals(2, pagina.totalPaginas)
    }

    @Test
    fun buscarSinFiltrosNoMandaLosNulos() = runTest {
        val cuerpo = """{"contenido":[],"numero":0,"tamanio":20,"totalElementos":0,"totalPaginas":0}"""

        val resultado = api(HttpStatusCode.OK, cuerpo).buscarProfesionales()

        assertEquals(setOf("page", "size"), pedido!!.url.parameters.names())
        assertEquals("0", pedido!!.url.parameters["page"])
        assertEquals("20", pedido!!.url.parameters["size"])
        assertEquals(emptyList(), assertIs<ResultadoApi.Exito<Pagina<Profesional>>>(resultado).valor.contenido)
    }

    @Test
    fun buscarConCategoriaInexistente() = runTest {
        val resultado = problema(HttpStatusCode.BadRequest, "la categoría 99 no existe").buscarProfesionales(categoriaId = 99)

        assertEquals(ResultadoApi.Invalido("la categoría 99 no existe", emptyList()), resultado)
    }

    @Test
    fun buscarConElCatalogoNoDisponible() = runTest {
        val resultado = problema(HttpStatusCode.ServiceUnavailable, "el catálogo todavía no está disponible")
            .buscarProfesionales()

        assertEquals(ResultadoApi.NoDisponible("el catálogo todavía no está disponible"), resultado)
    }

    @Test
    fun buscarSinSesionValida() = runTest {
        val resultado = api(HttpStatusCode.Unauthorized, "").buscarProfesionales()

        assertEquals(ResultadoApi.NoAutorizado(null), resultado)
    }

    @Test
    fun consultarAgenda() = runTest {
        val cuerpo = """
            {"id":7,"categoriaId":2,"nombre":"Ana","apellido":"Gómez","habilitado":true,
             "horarios":[{"diaSemana":"MONDAY","horaInicio":"09:00:00","horaFin":"12:00:00","duracionTurnoMinutos":30}],
             "versionCatalogo":4}
        """.trimIndent()

        val resultado = api(HttpStatusCode.OK, cuerpo).consultarAgenda(7)

        assertEquals("/api/profesionales/7", pedido!!.url.encodedPath)
        val agenda = assertIs<ResultadoApi.Exito<AgendaProfesional>>(resultado).valor
        assertEquals(
            listOf(HorarioSemanal(DayOfWeek.MONDAY, LocalTime(9, 0), LocalTime(12, 0), 30)),
            agenda.horarios,
        )
        assertEquals(4, agenda.versionCatalogo)
    }

    @Test
    fun consultarAgendaDeUnDeshabilitadoDevuelveExito() = runTest {
        val cuerpo = """{"id":8,"categoriaId":2,"nombre":"Luis","apellido":"Paz","habilitado":false,"horarios":[],"versionCatalogo":4}"""

        val resultado = api(HttpStatusCode.OK, cuerpo).consultarAgenda(8)

        assertFalse(assertIs<ResultadoApi.Exito<AgendaProfesional>>(resultado).valor.habilitado)
    }

    @Test
    fun consultarAgendaDeUnProfesionalInexistente() = runTest {
        val resultado = problema(HttpStatusCode.NotFound, "no existe el profesional 99").consultarAgenda(99)

        assertEquals(ResultadoApi.NoEncontrado("no existe el profesional 99"), resultado)
    }

    @Test
    fun consultarAgendaConElCatalogoNoDisponible() = runTest {
        val resultado = problema(HttpStatusCode.ServiceUnavailable, "el catálogo todavía no está disponible")
            .consultarAgenda(7)

        assertEquals(ResultadoApi.NoDisponible("el catálogo todavía no está disponible"), resultado)
    }
}
