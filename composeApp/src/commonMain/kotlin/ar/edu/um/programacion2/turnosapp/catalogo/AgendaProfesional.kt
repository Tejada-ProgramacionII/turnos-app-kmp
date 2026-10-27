package ar.edu.um.programacion2.turnosapp.catalogo

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/** Un profesional con sus horarios semanales habilitados (`GET /api/profesionales/{id}`). */
@Serializable
data class AgendaProfesional(
    val id: Long,
    val categoriaId: Long,
    val nombre: String,
    val apellido: String,
    val habilitado: Boolean,
    val horarios: List<HorarioSemanal> = emptyList(),
    val versionCatalogo: Long,
)

@Serializable
data class HorarioSemanal(
    val diaSemana: DayOfWeek,
    val horaInicio: LocalTime,
    val horaFin: LocalTime,
    val duracionTurnoMinutos: Int,
)
