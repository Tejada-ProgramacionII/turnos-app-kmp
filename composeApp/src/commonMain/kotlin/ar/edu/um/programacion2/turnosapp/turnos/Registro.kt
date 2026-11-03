package ar.edu.um.programacion2.turnosapp.turnos

import kotlinx.serialization.Serializable

/** Datos de alta de un usuario (`POST /api/register`). */
@Serializable
data class Registro(
    val login: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val imageUrl: String? = null,
    val langKey: String,
)
