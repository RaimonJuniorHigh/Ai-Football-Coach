package com.aifootballcoach.trainer.data

import kotlin.math.ceil

// Dit is het voetbalmanager-gedeelte. Elke stat is een cijfer van 1 tot 20 (20 is het best) en elke speler
// heeft een totaalcijfer van 1 tot 99, zoals een FIFA-kaartje. Dat totaalcijfer hangt af van zijn positie.
// Ik reken die cijfers niet in de app uit maar in de database (de views player_stats en player_cards in
// supabase/migrations). De app haalt ze alleen op, dus de website laat precies dezelfde cijfers zien.

/** De acht stats. Dezelfde codes staan in de database (tabel stat_types). */
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

/** Eén stat van een speler: [rating] is een cijfer van 1 tot 20. */
data class PlayerStat(val stat: StatType, val rating: Int)

/** Eén oefening: [successes] van de [attempts] pogingen lukten. [recordedOn] is een datum zoals "2026-11-03". */
data class DrillResult(
    val stat: StatType,
    val drill: String,
    val attempts: Int,
    val successes: Int,
    val recordedOn: String,
)

/** Wat een speler in één wedstrijd deed, met de uitslag van die wedstrijd. */
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

/** Alles op de spelerspagina. [overall] is 1 tot 99, of null zolang er nog geen oefeningen zijn. */
data class PlayerProfile(
    val overall: Int?,
    val stats: List<PlayerStat>,
    val drills: List<DrillResult>,
    val matches: List<MatchAppearance>,
)

/** Vijf niveaus van laag naar hoog. De kleur is een ARGB-getal, zodat dit bestand niets van Compose hoeft te weten. */
enum class RatingTier(val argb: Long, val label: String) {
    VeryLow(0xFFE53935, "Zwak"),
    Low(0xFFFB8C00, "Onder gemiddeld"),
    Average(0xFFFDD835, "Gemiddeld"),
    Good(0xFF7CB342, "Goed"),
    Excellent(0xFF2E7D32, "Uitstekend"),
}

object RatingColors {
    /** Stat van 1 tot 20: 1-4 rood, 5-8 oranje, 9-12 geel, 13-16 lichtgroen, 17-20 donkergroen. */
    fun tierForStat(rating: Int): RatingTier = when {
        rating <= 4 -> RatingTier.VeryLow
        rating <= 8 -> RatingTier.Low
        rating <= 12 -> RatingTier.Average
        rating <= 16 -> RatingTier.Good
        else -> RatingTier.Excellent
    }

    /** Het totaalcijfer van 1 tot 99 reken ik om naar de schaal van 1 tot 20, zodat het dezelfde kleuren krijgt. */
    fun tierForOverall(overall: Int): RatingTier = tierForStat(ceil(overall * 20.0 / 99).toInt())
}
