package ar.edu.um.programacion2.turnosapp.red

import ar.edu.um.programacion2.turnosapp.sesion.SessionStore
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LlamarTest {

    private suspend fun resultadoPara(status: HttpStatusCode): ResultadoApi<Unit> {
        val cliente = crearHttpClient(
            MockEngine {
                respond(
                    """{"title":"x","status":${status.value},"detail":"motivo"}""",
                    status,
                    headersOf(HttpHeaders.ContentType, "application/problem+json"),
                )
            },
            "http://servicio",
            SessionStore(),
        )
        return llamar({ cliente.get("/api/algo") }) {}
    }

    @Test
    fun cadaStatusDeErrorTieneSuResultado() = runTest {
        assertEquals(ResultadoApi.Invalido("motivo", emptyList()), resultadoPara(HttpStatusCode.BadRequest))
        assertEquals(ResultadoApi.NoAutorizado("motivo"), resultadoPara(HttpStatusCode.Unauthorized))
        assertEquals(ResultadoApi.NoEncontrado("motivo"), resultadoPara(HttpStatusCode.NotFound))
        assertEquals(ResultadoApi.Conflicto("motivo"), resultadoPara(HttpStatusCode.Conflict))
        assertEquals(ResultadoApi.NoDisponible("motivo"), resultadoPara(HttpStatusCode.ServiceUnavailable))
        assertEquals(ResultadoApi.ErrorDeIntegracion("motivo"), resultadoPara(HttpStatusCode.BadGateway))
        assertEquals(ResultadoApi.ErrorHttp(403, "motivo"), resultadoPara(HttpStatusCode.Forbidden))
    }
}
