package ar.edu.um.programacion2.turnosapp.turnos

import ar.edu.um.programacion2.turnosapp.red.ResultadoApi
import ar.edu.um.programacion2.turnosapp.red.llamar
import ar.edu.um.programacion2.turnosapp.sesion.SessionStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Endpoints de usuarios de turnos-reservas-service. */
class TurnosApi(private val cliente: HttpClient, private val sesion: SessionStore) {

    suspend fun registrar(registro: Registro): ResultadoApi<Unit> =
        llamar({
            cliente.post("/api/register") {
                contentType(ContentType.Application.Json)
                setBody(registro)
            }
        }) {}

    /** Si las credenciales son válidas, el token queda guardado en la sesión. */
    suspend fun iniciarSesion(username: String, password: String): ResultadoApi<Unit> =
        llamar({
            cliente.post("/api/authenticate") {
                contentType(ContentType.Application.Json)
                setBody(Credenciales(username, password))
            }
        }) { respuesta ->
            sesion.iniciar(respuesta.body<TokenRespuesta>().idToken)
        }

    suspend fun cuenta(): ResultadoApi<Cuenta> =
        llamar({ cliente.get("/api/account") }) { it.body() }

    @Serializable
    private data class Credenciales(val username: String, val password: String)

    @Serializable
    private data class TokenRespuesta(@SerialName("id_token") val idToken: String)
}
