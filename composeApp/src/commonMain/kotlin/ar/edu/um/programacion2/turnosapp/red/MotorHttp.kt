package ar.edu.um.programacion2.turnosapp.red

import io.ktor.client.engine.HttpClientEngine

/** Motor HTTP de cada plataforma. */
expect fun motorHttp(): HttpClientEngine
