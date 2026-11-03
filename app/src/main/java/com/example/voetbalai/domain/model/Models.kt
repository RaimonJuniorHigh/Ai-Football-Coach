package com.example.voetbalai.domain.model

import java.util.Calendar

// UITLEG: Dit zijn de basisonderdelen van de app: wat is een speler, een team, een positie.
// Er staat niks in over Android of de database. Dit is gewoon het voetbalwereldje.

// Een enum is een vaste keuzelijst. Een speler kan maar op één van deze vier posities spelen.
enum class Position { KEEPER, DEFENCE, MIDFIELD, ATTACK }

// Hiermee houd ik bij of de speler er op de training is: aanwezig, afwezig of geblesseerd.
enum class Attendance { PRESENT, ABSENT, INJURED }

/** Trainingsdagen die de app ondersteunt. [calendarDay] is de bijbehorende `Calendar`-constante. */
// Elke dag heeft ook de Android/Java-dagcode erbij, zodat ik later kan uitrekenen welke dag het eerst komt.
enum class Day(val calendarDay: Int) {
    MON(Calendar.MONDAY),
    TUE(Calendar.TUESDAY),
    WED(Calendar.WEDNESDAY),
    THU(Calendar.THURSDAY),
    FRI(Calendar.FRIDAY)
}

// Een data class is een klasse die vooral gegevens bewaart. Kotlin maakt er vanzelf copy(),
// equals() en toString() bij.
// Dit is een speler. Standaard is een speler aanwezig, dus een nieuwe speler begint als "PRESENT".
data class Player(
    val id: Long,
    val name: String,
    val position: Position,
    val attendance: Attendance = Attendance.PRESENT
)

// Dit is een team met zijn trainingsdagen, hoe lang een training duurt en de code voor de uitnodigingslink.
data class Team(
    val id: Long,
    val name: String,
    val trainingDays: Set<Day>,
    val sessionMinutes: Int,
    val inviteCode: String
)

/** Een speler uit de (fictieve) clublijst waaruit de trainer kan importeren. */
data class ClubPlayer(val name: String, val position: Position)

// Dit is een team samen met zijn spelers in één pakketje.
data class TeamWithPlayers(val team: Team, val players: List<Player>)
