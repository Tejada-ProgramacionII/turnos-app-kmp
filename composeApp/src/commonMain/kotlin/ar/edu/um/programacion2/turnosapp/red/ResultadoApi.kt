package ar.edu.um.programacion2.turnosapp.red

/** Resultado de un pedido a un servicio: la UI nunca ve excepciones de Ktor. */
sealed interface ResultadoApi<out T> {
    data class Exito<T>(val valor: T) : ResultadoApi<T>

    /** 400: errores por campo (validación) o solo un detalle (ej. login ya usado). */
    data class Invalido(val detalle: String?, val errores: List<ErrorDeCampo>) : ResultadoApi<Nothing>

    /** 401: credenciales inválidas o sesión vencida. */
    data class NoAutorizado(val detalle: String?) : ResultadoApi<Nothing>

    data class ErrorHttp(val status: Int, val detalle: String?) : ResultadoApi<Nothing>

    /** El servicio contestó 2xx pero con un cuerpo que no se pudo leer. */
    data class RespuestaInesperada(val causa: String?) : ResultadoApi<Nothing>

    data class SinConexion(val causa: String?) : ResultadoApi<Nothing>
}

data class ErrorDeCampo(val campo: String, val mensaje: String?)
