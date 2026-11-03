package ar.edu.um.programacion2.turnosapp.turnos

import kotlinx.serialization.Serializable

/** Cuenta del usuario con sesión (`GET /api/account`). */
@Serializable
data class Cuenta(
    val login: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val imageUrl: String? = null,
    val langKey: String,
    val activated: Boolean,
    val authorities: Set<String> = emptySet(),
)
