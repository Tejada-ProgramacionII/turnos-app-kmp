package ar.edu.um.programacion2.turnosapp.turnos

import ar.edu.um.programacion2.turnosapp.red.ErrorDeCampo
import ar.edu.um.programacion2.turnosapp.red.ResultadoApi
import ar.edu.um.programacion2.turnosapp.red.crearHttpClient
import ar.edu.um.programacion2.turnosapp.red.jsonApi
import ar.edu.um.programacion2.turnosapp.sesion.SessionStore
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class TurnosApiTest {

    private val sesion = SessionStore()
    private var pedido: HttpRequestData? = null

    private fun api(responder: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        TurnosApi(
            crearHttpClient(
                MockEngine { request ->
                    pedido = request
                    responder(request)
                },
                "http://turnos",
                sesion,
            ),
            sesion,
        )

    private fun MockRequestHandleScope.json(status: HttpStatusCode, cuerpo: String, tipo: String = "application/json") =
        respond(cuerpo, status, headersOf(HttpHeaders.ContentType, tipo))

    private suspend fun cuerpoEnviado(): JsonObject =
        jsonApi.parseToJsonElement(pedido!!.body.toByteArray().decodeToString()).jsonObject

    private val registro = Registro(
        login = "jperez",
        password = "secreta123",
        firstName = "Juan",
        lastName = "Pérez",
        email = "jperez@example.com",
        langKey = "es",
    )

    @Test
    fun registrarOk() = runTest {
        val resultado = api { respond("", HttpStatusCode.Created) }.registrar(registro)

        assertEquals(ResultadoApi.Exito(Unit), resultado)
        assertEquals(HttpMethod.Post, pedido!!.method)
        assertEquals("/api/register", pedido!!.url.encodedPath)
        assertEquals("jperez", cuerpoEnviado()["login"]!!.jsonPrimitive.content)
    }

    @Test
    fun registrarConErroresDeValidacion() = runTest {
        val cuerpo = """
            {"timestamp":"2026-10-27T19:38:43.055Z","status":400,"error":"Bad Request","path":"/api/register",
             "errors":[
               {"objectName":"usuarioRequest","field":"email","code":"Email","defaultMessage":"must be a well-formed email address"},
               {"objectName":"usuarioRequest","field":"password","code":"Size","defaultMessage":"size must be between 4 and 100"}
             ]}
        """.trimIndent()

        val resultado = api { json(HttpStatusCode.BadRequest, cuerpo) }.registrar(registro)

        assertEquals(
            ResultadoApi.Invalido(
                detalle = null,
                errores = listOf(
                    ErrorDeCampo("email", "must be a well-formed email address"),
                    ErrorDeCampo("password", "size must be between 4 and 100"),
                ),
            ),
            resultado,
        )
    }

    @Test
    fun registrarConLoginYaUsado() = runTest {
        val cuerpo = """{"title":"Bad Request","status":400,"detail":"el login ya está en uso","instance":"/api/register"}"""

        val resultado = api { json(HttpStatusCode.BadRequest, cuerpo, "application/problem+json") }.registrar(registro)

        assertEquals(ResultadoApi.Invalido("el login ya está en uso", emptyList()), resultado)
    }

    @Test
    fun iniciarSesionOkGuardaElToken() = runTest {
        val resultado = api { json(HttpStatusCode.OK, """{"id_token":"token-123"}""") }
            .iniciarSesion("jperez", "secreta123")

        assertEquals(ResultadoApi.Exito(Unit), resultado)
        assertEquals("/api/authenticate", pedido!!.url.encodedPath)
        assertEquals("jperez", cuerpoEnviado()["username"]!!.jsonPrimitive.content)
        assertEquals("token-123", sesion.token)
    }

    @Test
    fun iniciarSesionConCredencialesInvalidas() = runTest {
        val cuerpo = """{"detail":"credenciales inválidas","instance":"/api/authenticate","status":401,"title":"Unauthorized"}"""

        val resultado = api { json(HttpStatusCode.Unauthorized, cuerpo, "application/problem+json") }
            .iniciarSesion("jperez", "mala")

        assertEquals(ResultadoApi.NoAutorizado("credenciales inválidas"), resultado)
        assertNull(sesion.token)
    }

    @Test
    fun cuentaMandaElBearerYLeeLaCuenta() = runTest {
        sesion.iniciar("token-123")
        val cuerpo = """
            {"login":"jperez","firstName":"Juan","lastName":"Pérez","email":"jperez@example.com",
             "imageUrl":null,"langKey":"es","activated":true,"authorities":["ROLE_USER"]}
        """.trimIndent()

        val resultado = api { json(HttpStatusCode.OK, cuerpo) }.cuenta()

        assertEquals("Bearer token-123", pedido!!.headers[HttpHeaders.Authorization])
        assertEquals("/api/account", pedido!!.url.encodedPath)
        val cuenta = assertIs<ResultadoApi.Exito<Cuenta>>(resultado).valor
        assertEquals("jperez", cuenta.login)
        assertEquals(setOf("ROLE_USER"), cuenta.authorities)
    }

    @Test
    fun cuentaSinSesionDevuelveNoAutorizado() = runTest {
        val resultado = api { respond("", HttpStatusCode.Unauthorized) }.cuenta()

        assertEquals(ResultadoApi.NoAutorizado(null), resultado)
    }

    @Test
    fun otroStatusDevuelveErrorHttp() = runTest {
        val resultado = api { respond("", HttpStatusCode.InternalServerError) }.cuenta()

        assertEquals(ResultadoApi.ErrorHttp(500, null), resultado)
    }

    @Test
    fun cuerpoIlegibleDevuelveRespuestaInesperada() = runTest {
        val resultado = api { json(HttpStatusCode.OK, """{"otra":"cosa"}""") }.cuenta()

        assertIs<ResultadoApi.RespuestaInesperada>(resultado)
    }

    @Test
    fun fallaDeRedDevuelveSinConexion() = runTest {
        val resultado = api { throw IOException("sin red") }.cuenta()

        assertEquals(ResultadoApi.SinConexion("sin red"), resultado)
    }
}
