package com.example.voetbalai.domain.rules

import com.example.voetbalai.domain.model.Player
import com.example.voetbalai.domain.model.Position

// UITLEG: Hier staan de regels voor een geldig team. Een voetbalteam heeft elf spelers in het veld
// plus wissels, dus ik eis minimaal 13 spelers en van elke positie een minimum.
// "object" betekent dat er maar één van is; ik gebruik hem direct, zonder hem aan te maken.

/** Regels waaraan een selectie moet voldoen om als echt team te tellen (elf-tegen-elf met wissels). */
object TeamRules {
    const val MIN_PLAYERS = 13

    /** Minimaal aantal spelers per positie; samen minder dan [MIN_PLAYERS], de rest is vrij te verdelen. */
    val MIN_PER_POSITION: Map<Position, Int> = mapOf(
        Position.KEEPER to 1,
        Position.DEFENCE to 3,
        Position.MIDFIELD to 3,
        Position.ATTACK to 2
    )

    // Nederlandse namen voor in de foutmelding ("Minimaal 3 verdedigers nodig").
    private val positionName = mapOf(
        Position.KEEPER to "keeper",
        Position.DEFENCE to "verdediger",
        Position.MIDFIELD to "middenvelder",
        Position.ATTACK to "aanvaller"
    )

    /** Lijst met wat er nog ontbreekt, in het Nederlands; leeg als de selectie voldoet. */
    fun problems(players: List<Player>): List<String> = buildList {
        // Eerst kijk ik of er in totaal genoeg spelers zijn.
        if (players.size < MIN_PLAYERS) {
            add("Minimaal $MIN_PLAYERS spelers nodig, er zijn er ${players.size}.")
        }
        // Daarna kijk ik of er genoeg per positie zijn: ik tel hoeveel er op die positie spelen.
        MIN_PER_POSITION.forEach { (position, min) ->
            val count = players.count { it.position == position }
            if (count < min) {
                add("Minimaal $min ${positionName.getValue(position)}${if (min > 1) "s" else ""} nodig, er zijn er $count.")
            }
        }
    }

    // Het team is geldig als er geen problemen zijn.
    fun isValid(players: List<Player>) = problems(players).isEmpty()
}
