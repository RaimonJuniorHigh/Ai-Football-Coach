package com.aifootballcoach.trainer.ui.sessions

import com.aifootballcoach.trainer.data.SessionDraft
import com.aifootballcoach.trainer.data.SquadInsights
import com.aifootballcoach.trainer.data.Team
import kotlinx.datetime.LocalDate

enum class SessionStatus(val label: String) {
    Empty("Leeg"),
    Draft("Concept"),
    Sent("Verstuurd"),
}

/** One row under "Komend" on the Trainingen tab. */
data class UpcomingSession(
    val date: LocalDate,
    val title: String,
    val minutes: Int,
    val status: SessionStatus,
)

/** One row under "Eerder": a session that was sent and whose day has passed. */
data class PastSession(
    val date: LocalDate,
    val title: String,
    val players: Int?,
)

data class SessionList(val upcoming: List<UpcomingSession>, val past: List<PastSession>)

/**
 * Builds the Trainingen tab from what the app knows now: the training days of the team, the open draft
 * and the sessions that were sent. There's no table for sessions in the database yet, so this is it for now.
 */
object SessionLists {
    const val UpcomingCount = 2

    fun build(today: LocalDate, team: Team, draft: SessionDraft?, sent: List<SessionDraft>): SessionList {
        val planned = (sent + listOfNotNull(draft)).filter { it.date != null }.associateBy { it.date }
        val upcoming = SquadInsights.upcomingTrainingDays(today, team.trainingDays, UpcomingCount).map { date ->
            val session = planned[date]
            UpcomingSession(
                date = date,
                title = session?.theme?.ifBlank { null } ?: if (session == null) "Nog niet gepland" else "Training",
                minutes = session?.minutes ?: team.sessionMinutes,
                status = when {
                    session == null -> SessionStatus.Empty
                    session.sentTo != null -> SessionStatus.Sent
                    else -> SessionStatus.Draft
                },
            )
        }
        val past = sent
            .filter { it.date != null && it.date < today }
            .sortedByDescending { it.date }
            .map { PastSession(it.date!!, it.theme.ifBlank { "Training" }, it.sentTo) }
        return SessionList(upcoming, past)
    }
}
