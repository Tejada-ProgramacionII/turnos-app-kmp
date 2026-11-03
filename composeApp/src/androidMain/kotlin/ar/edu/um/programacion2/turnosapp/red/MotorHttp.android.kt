package ar.edu.um.programacion2.turnosapp.red

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun motorHttp(): HttpClientEngine = OkHttp.create()
