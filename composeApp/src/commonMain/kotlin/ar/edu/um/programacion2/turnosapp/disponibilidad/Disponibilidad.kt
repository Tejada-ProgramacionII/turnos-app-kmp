package ar.edu.um.programacion2.turnosapp.disponibilidad

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/** Turnos libres de un profesional en una fecha (`GET /api/disponibilidad`). */
@Serializable
data class Disponibilidad(
    val profesionalId: Long,
    val fecha: LocalDate,
    val turnos: List<TurnoLibre> = emptyList(),
)

@Serializable
data class TurnoLibre(val horaInicio: LocalTime, val horaFin: LocalTime)
