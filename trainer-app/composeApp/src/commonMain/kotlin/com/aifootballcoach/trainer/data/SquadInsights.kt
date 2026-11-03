package com.aifootballcoach.trainer.data

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * Dingen die ik in de app uit de stats van de selectie bepaal, zonder nieuwe vraag aan de database:
 * wie aandacht nodig heeft, wat we kunnen trainen en wanneer de volgende training is.
 */
object SquadInsights {
    /** Een stat van 4 of lager is rood (zie [RatingColors]). */
    private const val WeakStat = 4

    /** Een stat van 8 of lager is rood of oranje. */
    private const val LowStat = 8

    /** Spelers met een rode stat, de laagste eerst (hoogstens vijf). */
    fun needsYou(players: List<Player>, stats: Map<String, List<PlayerStat>>): List<Attention> =
        players
            .mapNotNull { player ->
                val weakest = stats[player.id].orEmpty().minByOrNull { it.rating }
                if (weakest != null && weakest.rating <= WeakStat) player to weakest else null
            }
            .sortedBy { (_, weakest) -> weakest.rating }
            .take(5)
            .map { (player, weakest) -> Attention(player, "${weakest.stat.label}: ${weakest.rating} van 20 — zwakste punt") }

    /** De drie stats waar de meeste spelers laag op scoren, als thema's voor de volgende training. */
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

    /** De eerste trainingsdag vanaf [today] (vandaag telt mee), of null als het team geen trainingsdagen heeft. */
    fun nextTrainingDay(today: LocalDate, days: Set<Weekday>): LocalDate? =
        (0..6).map { today.plus(it, DateTimeUnit.DAY) }.firstOrNull { date ->
            // Weekday kent alleen maandag tot en met vrijdag, dus zaterdag en zondag zijn nooit een trainingsdag.
            Weekday.entries.getOrNull(date.dayOfWeek.ordinal) in days
        }

    fun nextSession(today: LocalDate, team: Team): PlannedSession {
        val date = nextTrainingDay(today, team.trainingDays)
            ?: return PlannedSession("Geen trainingsdag", "Kies trainingsdagen bij je team", team.sessionMinutes)
        return PlannedSession(date.dayName(), date.shortLabel(), team.sessionMinutes)
    }
}
