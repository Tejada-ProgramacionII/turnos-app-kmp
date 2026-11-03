package ar.edu.um.programacion2.turnosapp.catalogo

import kotlinx.serialization.Serializable

/** Resultado de la búsqueda de profesionales. */
@Serializable
data class Profesional(
    val id: Long,
    val categoriaId: Long,
    val nombre: String,
    val apellido: String,
    val habilitado: Boolean,
)
