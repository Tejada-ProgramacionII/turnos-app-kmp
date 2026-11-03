package ar.edu.um.programacion2.turnosapp.red

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

/** Hace el pedido y traduce la respuesta (o la falla de red) a un [ResultadoApi]. */
suspend fun <T> llamar(
    pedido: suspend () -> HttpResponse,
    alSalirBien: suspend (HttpResponse) -> T,
): ResultadoApi<T> =
    try {
        val respuesta = pedido()
        if (respuesta.status.isSuccess()) ResultadoApi.Exito(alSalirBien(respuesta)) else rechazo(respuesta)
    } catch (e: ContentConvertException) {
        ResultadoApi.RespuestaInesperada(e.message)
    } catch (e: IOException) {
        ResultadoApi.SinConexion(e.message)
    }

private suspend fun rechazo(respuesta: HttpResponse): ResultadoApi<Nothing> {
    val cuerpo = leerCuerpoDeError(respuesta.bodyAsText())
    return when (respuesta.status) {
        HttpStatusCode.BadRequest -> ResultadoApi.Invalido(
            cuerpo?.detail,
            cuerpo?.errors.orEmpty().map { ErrorDeCampo(it.field, it.defaultMessage) },
        )
        HttpStatusCode.Unauthorized -> ResultadoApi.NoAutorizado(cuerpo?.detail)
        else -> ResultadoApi.ErrorHttp(respuesta.status.value, cuerpo?.detail)
    }
}

/** El cuerpo puede venir vacío, como ProblemDetail (`detail`) o como error de validación (`errors`). */
private fun leerCuerpoDeError(texto: String): CuerpoDeError? =
    try {
        jsonApi.decodeFromString<CuerpoDeError>(texto)
    } catch (_: SerializationException) {
        null
    }

@Serializable
private data class CuerpoDeError(
    val detail: String? = null,
    val errors: List<ErrorDeCampoDto> = emptyList(),
)

@Serializable
private data class ErrorDeCampoDto(val field: String, val defaultMessage: String? = null)
