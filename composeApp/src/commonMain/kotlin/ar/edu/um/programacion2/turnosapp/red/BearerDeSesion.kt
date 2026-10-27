package ar.edu.um.programacion2.turnosapp.red

import ar.edu.um.programacion2.turnosapp.sesion.SessionStore
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.bearerAuth

/** Agrega `Authorization: Bearer <token>` a cada pedido mientras haya sesión. */
fun bearerDeSesion(sesion: SessionStore) = createClientPlugin("BearerDeSesion") {
    onRequest { request, _ ->
        sesion.token?.let { request.bearerAuth(it) }
    }
}
