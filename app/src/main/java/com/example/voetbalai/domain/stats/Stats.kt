package com.example.voetbalai.domain.stats

import com.example.voetbalai.domain.model.Position

// UITLEG: Alles rond de spelersstatistieken (het voetbalmanager-gedeelte).
// - Een stat is een cijfer van 1 tot 20 (20 is het best), net als in een voetbalmanager-spel.
// - Het totaalcijfer van een speler is een cijfer van 1 tot 99, zoals een FIFA-kaartje. Dat cijfer hangt af van
//   zijn positie: voor een spits telt afwerken zwaar, voor een keeper telt keepen zwaar.
// - De cijfers worden NIET in de app uitgerekend maar door de database (views player_stats en player_cards).
//   De app haalt ze alleen op. Zo gebruikt de website precies dezelfde cijfers.
// - Een stat komt uit trainingsresultaten: een speler doet 100 schoten en 70 gaan erin, dan is het slagingspercentage 70%.

/** De acht stats. Dezelfde codes staan in de database (tabel stat_types). */
enum class StatType(val label: String, val drill: String) {
    FINISHING("Afwerken", "Schoten op doel: aantal schoten en aantal goals"),
    PASSING("Passen", "Passes naar een medespeler: aantal passes en aantal aangekomen"),
    DRIBBLING("Dribbelen", "Dribbels langs een pion: aantal pogingen en aantal gelukt"),
    DEFENDING("Verdedigen", "Duels: aantal duels en aantal gewonnen"),
    PACE("Snelheid", "Sprints: aantal sprints en aantal binnen de tijd"),
    STAMINA("Conditie", "Intervallopen: aantal rondes en aantal binnen de tijd"),
    STRENGTH("Kracht", "Lichaamsduels: aantal duels en aantal gewonnen"),
    GOALKEEPING("Keepen", "Schoten op de keeper: aantal schoten en aantal gestopt")
}

/** Eén stat van een speler: [rating] is een cijfer van 1 tot 20. */
data class PlayerStat(val stat: StatType, val rating: Int)

/** Het "kaartje" van een speler. [overall] is 1 tot 99, of null zolang er nog geen trainingsresultaten zijn. */
data class PlayerCard(val playerId: Long, val name: String, val position: Position, val overall: Int?)

/** Eén trainingsresultaat: [successes] van de [attempts] pogingen lukten. [recordedOn] is een datum als "2026-11-03". */
data class DrillResult(
    val stat: StatType,
    val drill: String,
    val attempts: Int,
    val successes: Int,
    val recordedOn: String
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
    val assists: Int
)

/** Alles wat je ziet als je op een speler klikt: kaartje, stats, laatste oefeningen en wedstrijden. */
data class PlayerProfile(
    val card: PlayerCard,
    val stats: List<PlayerStat>,
    val drills: List<DrillResult>,
    val matches: List<MatchAppearance>
)
