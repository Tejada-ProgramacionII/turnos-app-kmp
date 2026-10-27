package ar.edu.um.programacion2.turnosapp.sesion

import kotlin.concurrent.Volatile

/** Sesión del usuario: el JWT vive solo en memoria y se pierde al cerrar la app. */
class SessionStore {
    @Volatile
    var token: String? = null
        private set

    fun iniciar(token: String) {
        this.token = token
    }

    fun cerrar() {
        token = null
    }
}
