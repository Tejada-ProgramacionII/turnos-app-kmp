package ar.edu.um.programacion2.turnosapp.catalogo

import kotlinx.serialization.Serializable

/** Una página de resultados; `numero` empieza en 0. */
@Serializable
data class Pagina<T>(
    val contenido: List<T>,
    val numero: Int,
    val tamanio: Int,
    val totalElementos: Long,
    val totalPaginas: Int,
)
