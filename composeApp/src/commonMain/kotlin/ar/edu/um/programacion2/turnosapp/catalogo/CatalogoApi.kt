package ar.edu.um.programacion2.turnosapp.catalogo

import ar.edu.um.programacion2.turnosapp.red.ResultadoApi
import ar.edu.um.programacion2.turnosapp.red.llamar
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.LocalDate

/** Endpoints de catalogo-sincronizacion-service. El cliente ya manda el Bearer de la sesión. */
class CatalogoApi(private val cliente: HttpClient) {

    /** Todos los filtros son opcionales; los nulos no se mandan. */
    suspend fun buscarProfesionales(
        categoriaId: Long? = null,
        nombre: String? = null,
        habilitado: Boolean? = null,
        fecha: LocalDate? = null,
        page: Int = 0,
        size: Int = 20,
    ): ResultadoApi<Pagina<Profesional>> =
        llamar({
            cliente.get("/api/profesionales") {
                parameter("categoriaId", categoriaId)
                parameter("nombre", nombre)
                parameter("habilitado", habilitado)
                parameter("fecha", fecha)
                parameter("page", page)
                parameter("size", size)
            }
        }) { it.body() }

    suspend fun consultarAgenda(id: Long): ResultadoApi<AgendaProfesional> =
        llamar({ cliente.get("/api/profesionales/$id") }) { it.body() }
}
