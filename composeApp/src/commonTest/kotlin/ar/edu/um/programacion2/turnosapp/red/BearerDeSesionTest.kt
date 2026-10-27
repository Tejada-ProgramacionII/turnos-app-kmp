package ar.edu.um.programacion2.turnosapp.red

import ar.edu.um.programacion2.turnosapp.sesion.SessionStore
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BearerDeSesionTest {

    private val sesion = SessionStore()
    private var authorization: String? = null
    private val cliente = crearHttpClient(
        MockEngine { request ->
            authorization = request.headers[HttpHeaders.Authorization]
            respondOk()
        },
        "http://servicio",
        sesion,
    )

    @Test
    fun sinSesionNoMandaAuthorization() = runTest {
        cliente.get("/api/algo")

        assertNull(authorization)
    }

    @Test
    fun conSesionMandaElBearer() = runTest {
        sesion.iniciar("token-de-prueba")

        cliente.get("/api/algo")

        assertEquals("Bearer token-de-prueba", authorization)
    }

    @Test
    fun alCerrarLaSesionDejaDeMandarlo() = runTest {
        sesion.iniciar("token-de-prueba")
        sesion.cerrar()

        cliente.get("/api/algo")

        assertNull(authorization)
    }
}
