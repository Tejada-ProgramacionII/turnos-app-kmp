package ar.edu.um.programacion2.turnosapp.red

import ar.edu.um.programacion2.turnosapp.sesion.SessionStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Los servicios pueden sumar campos sin romper la app. */
val jsonApi = Json { ignoreUnknownKeys = true }

fun crearHttpClient(motor: HttpClientEngine, baseUrl: String, sesion: SessionStore): HttpClient =
    HttpClient(motor) {
        install(ContentNegotiation) {
            json(jsonApi)
        }
        install(bearerDeSesion(sesion))
        defaultRequest {
            url(baseUrl)
        }
    }
