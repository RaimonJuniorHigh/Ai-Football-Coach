package com.aifootballcoach.trainer.data

import kotlin.math.ceil

// The football-manager part: every stat is a number from 1 to 20 (20 is best), every player has an
// overall from 1 to 99 (like a FIFA card) that depends on his position. The database calculates these
// numbers (the views player_stats and player_cards in supabase/migrations); the app only reads them,
// so the website shows exactly the same numbers.

/** The eight stats. The same codes are in the database (table stat_types). */
enum class StatType(val label: String, val drill: String) {
    FINISHING("Afwerken", "Schoten op doel: aantal schoten en aantal goals"),
    PASSING("Passen", "Passes naar een medespeler: aantal passes en aantal aangekomen"),
    DRIBBLING("Dribbelen", "Dribbels langs een pion: aantal pogingen en aantal gelukt"),
    DEFENDING("Verdedigen", "Duels: aantal duels en aantal gewonnen"),
    PACE("Snelheid", "Sprints: aantal sprints en aantal binnen de tijd"),
    STAMINA("Conditie", "Intervallopen: aantal rondes en aantal binnen de tijd"),
    STRENGTH("Kracht", "Lichaamsduels: aantal duels en aantal gewonnen"),
    GOALKEEPING("Keepen", "Schoten op de keeper: aantal schoten en aantal gestopt"),
}

/** One stat of a player: [rating] is 1 to 20. */
data class PlayerStat(val stat: StatType, val rating: Int)

/** One training result: [successes] of the [attempts] worked. [recordedOn] is a date like "2026-11-03". */
data class DrillResult(
    val stat: StatType,
    val drill: String,
    val attempts: Int,
    val successes: Int,
    val recordedOn: String,
)

/** What a player did in one match, with the score of that match. */
data class MatchAppearance(
    val playedOn: String,
    val opponent: String,
    val home: Boolean,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val minutes: Int,
    val goals: Int,
    val assists: Int,
)

/** Everything on the player page. [overall] is 1 to 99, or null while there are no training results. */
data class PlayerProfile(
    val overall: Int?,
    val stats: List<PlayerStat>,
    val drills: List<DrillResult>,
    val matches: List<MatchAppearance>,
)

/** Five levels from low to high; the colour is an ARGB number so this file needs nothing from Compose. */
enum class RatingTier(val argb: Long, val label: String) {
    VeryLow(0xFFE53935, "Zwak"),
    Low(0xFFFB8C00, "Onder gemiddeld"),
    Average(0xFFFDD835, "Gemiddeld"),
    Good(0xFF7CB342, "Goed"),
    Excellent(0xFF2E7D32, "Uitstekend"),
}

object RatingColors {
    /** Stat 1 to 20: 1-4 red, 5-8 orange, 9-12 yellow, 13-16 light green, 17-20 dark green. */
    fun tierForStat(rating: Int): RatingTier = when {
        rating <= 4 -> RatingTier.VeryLow
        rating <= 8 -> RatingTier.Low
        rating <= 12 -> RatingTier.Average
        rating <= 16 -> RatingTier.Good
        else -> RatingTier.Excellent
    }

    /** Overall 1 to 99 is converted to the 1 to 20 scale so it uses the same colours. */
    fun tierForOverall(overall: Int): RatingTier = tierForStat(ceil(overall * 20.0 / 99).toInt())
}
