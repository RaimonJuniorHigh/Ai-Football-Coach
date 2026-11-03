package com.aifootballcoach.trainer.data

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * Things the app works out from the squad's stats, without a call to the database:
 * who needs attention, what to train, and when the next training is.
 */
object SquadInsights {
    /** A stat of 4 or lower is red (see [RatingColors]). */
    private const val WeakStat = 4

    /** A stat of 8 or lower is red or orange. */
    private const val LowStat = 8

    /** Players whose weakest stat is red, the lowest first (at most five). */
    fun needsYou(players: List<Player>, stats: Map<String, List<PlayerStat>>): List<Attention> =
        players
            .mapNotNull { player ->
                val weakest = stats[player.id].orEmpty().minByOrNull { it.rating }
                if (weakest != null && weakest.rating <= WeakStat) player to weakest else null
            }
            .sortedBy { (_, weakest) -> weakest.rating }
            .take(5)
            .map { (player, weakest) -> Attention(player, "${weakest.stat.label}: ${weakest.rating} van 20 — zwakste punt") }

    /** The three stats where most players score low, as themes for the next session. */
    fun focusSuggestions(stats: Map<String, List<PlayerStat>>): List<FocusSuggestion> =
        StatType.entries
            .map { type -> type to stats.values.count { list -> list.any { it.stat == type && it.rating <= LowStat } } }
            .filter { (_, count) -> count > 0 }
            .sortedByDescending { (_, count) -> count }
            .take(3)
            .map { (type, count) ->
                FocusSuggestion(
                    id = type.name,
                    title = type.label,
                    reason = if (count == 1) "1 speler onder de 9 van 20" else "$count spelers onder de 9 van 20",
                )
            }

    /** The first training day from [today] on (today included), or null when the team has no training days. */
    fun nextTrainingDay(today: LocalDate, days: Set<Weekday>): LocalDate? =
        (0..6).map { today.plus(it, DateTimeUnit.DAY) }.firstOrNull { date ->
            // Weekday only knows Monday to Friday, so Saturday and Sunday are never training days.
            Weekday.entries.getOrNull(date.dayOfWeek.ordinal) in days
        }

    fun nextSession(today: LocalDate, team: Team): PlannedSession {
        val date = nextTrainingDay(today, team.trainingDays)
            ?: return PlannedSession("Geen trainingsdag", "Kies trainingsdagen bij je team", team.sessionMinutes)
        return PlannedSession(date.dayName(), date.shortLabel(), team.sessionMinutes)
    }
}
